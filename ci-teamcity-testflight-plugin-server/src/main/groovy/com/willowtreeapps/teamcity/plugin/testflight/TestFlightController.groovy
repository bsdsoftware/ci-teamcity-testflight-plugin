package com.willowtreeapps.teamcity.plugin.testflight

import com.willowtreeapps.teamcity.plugin.testflight.uploader.TestFlightUploader
import com.willowtreeapps.teamcity.plugin.testflight.uploader.UploadRequest
import jetbrains.buildServer.controllers.BaseController
import jetbrains.buildServer.serverSide.BuildsManager
import jetbrains.buildServer.serverSide.SBuild
import jetbrains.buildServer.serverSide.SBuildServer
import jetbrains.buildServer.serverSide.SProject
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactHolder
import jetbrains.buildServer.serverSide.artifacts.BuildArtifacts
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactsViewMode
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager
import jetbrains.buildServer.web.openapi.WebControllerManager
import org.apache.commons.io.IOUtils
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import org.springframework.web.servlet.ModelAndView

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class TestFlightController extends BaseController {

    private final WebControllerManager webControllerManager
    private final BuildsManager buildsManager
    private final ProjectSettingsManager projectSettingsManager
    private boolean customSettingsPosted = false

    public TestFlightController(
            @NotNull final SBuildServer sBuildServer,
            @NotNull final WebControllerManager manager,
            @NotNull BuildsManager buildsManager,
            @NotNull ProjectSettingsManager projectSettingsManager
    ) {
        super(sBuildServer)
        this.webControllerManager = manager
        this.buildsManager = buildsManager
        this.projectSettingsManager = projectSettingsManager
    }

    public void register() {
        webControllerManager.registerController('/testFlight.html', this)

        TestFlightSettingsFactory testFlightSettingsFactory = new TestFlightSettingsFactory();
        projectSettingsManager.registerSettingsFactory(TestFlightProjectSettings.NAME, testFlightSettingsFactory);
    }

    @Nullable
    @Override
    protected ModelAndView doHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Exception {
        boolean success = false

        String artifactRelativePath = request.getParameter('artifactRelativePath')
        String notes = request.getParameter('notes')
        customSettingsPosted = Boolean.valueOf(request.getParameter('customSettings'))

        TestFlightProfile testFlightProfile = new TestFlightProfile(
                internalBuildId: request.getParameter('internalBuildId'),
                buildId: Long.parseLong(request.getParameter('buildId')),
                id: request.getParameter('id'),
                apiToken: request.getParameter('apiToken'),
                teamToken: request.getParameter('teamToken'),
                distroList: request.getParameter('distroList'),
                projectId: request.getParameter('projectId')
        )

        if (testFlightProfile.isValid() && !artifactRelativePath.isEmpty()) {

            // TODO
            // Send an email to the submitter

            SBuild build = buildsManager.findBuildInstanceById(testFlightProfile.buildId)

            try {
                TestFlightUploader uploader = new TestFlightUploader()
                UploadRequest testflightUploadRequest = new UploadRequest(apiToken: testFlightProfile.apiToken,
                        teamToken: testFlightProfile.teamToken, buildNotes: notes,
                        lists: testFlightProfile.distroList, notifyTeam: true, replace: true,
                        dsymFile: null)

                BuildArtifacts buildArtifacts = build.getArtifacts(BuildArtifactsViewMode.VIEW_ALL)

                BuildArtifactHolder artifactHolder = buildArtifacts.findArtifact(artifactRelativePath)

                final File tempFile = createTempFile(getFilePrefix(artifactHolder.getName()), getFileSuffix(artifactHolder.getName()))
                tempFile.deleteOnExit()

                FileOutputStream out = new FileOutputStream(tempFile)
                IOUtils.copy(artifactHolder.getArtifact().getInputStream(), out)

                testflightUploadRequest.file = tempFile

                uploader.upload(testflightUploadRequest)

                persistCustomTestFlightProfile(customSettingsPosted, testFlightProfile)

                success = true
            } catch (Exception e) {
                // TODO: figure out what to do with exceptions.
            }
        }

        return new ModelAndView(makeReturnUrl(testFlightProfile, success))
    }

    private String makeReturnUrl(final TestFlightProfile profile, final boolean result) {
        // redirect:
        return "redirect:viewLog.html?buildId=${profile.buildId}&tab=buildResultsDiv&buildTypeId=${buildsManager.findBuildInstanceById(profile.buildId).getBuildType().getExternalId()}&testflightUploadSucceeded=${result}"
    }

    private File createTempFile(String prefix, String suffix) {
        String fileName = prefix + suffix
        String tempDirPath = System.getProperty("java.io.tmpdir")

        File fileCheck = new File("${tempDirPath}/${fileName}")
        if (fileCheck.exists() && fileCheck.isFile()) {
            fileCheck.delete()
        }
        return new File(tempDirPath, fileName)
    }

    static String getFilePrefix(String fileName) {
        return fileName.substring(0, (fileName.length() - getFileSuffix(fileName).length()))
    }

    // Assuming only ipa's and apk's are supported.
    static String getFileSuffix(String fileName) {
        return fileName.endsWith(MobileBuildArtifacts.IOS_EXTENSION) ? MobileBuildArtifacts.IOS_EXTENSION : MobileBuildArtifacts.ANDROID_EXTENSION
    }

    private void persistCustomTestFlightProfile(final boolean customSettingsPosted, final TestFlightProfile profile) {
        if (customSettingsPosted) {
            TestFlightProjectSettings settings = (TestFlightProjectSettings) projectSettingsManager.getSettings(profile.projectId, TestFlightProjectSettings.NAME)
            settings.updateProfile(profile)
            SProject project = buildsManager.findBuildInstanceById(profile.buildId).getBuildType().getProject()
            project.persist()
        }
    }
}
