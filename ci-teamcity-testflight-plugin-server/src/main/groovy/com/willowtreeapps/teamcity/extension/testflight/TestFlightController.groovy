package com.willowtreeapps.teamcity.extension.testflight

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
        projectSettingsManager.registerSettingsFactory(TestFlightSettings.NAME, testFlightSettingsFactory);
    }

    @Nullable
    @Override
    protected ModelAndView doHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Exception {
        UploadResult uploadResult
        ModelAndView mav = new ModelAndView()

        String artifactRelativePath = request.getParameter(TestFlightSettings.ARTIFACT_RELATIVE_PATH)
        String notes = request.getParameter(TestFlightSettings.NOTES)
        customSettingsPosted = Boolean.valueOf(request.getParameter(TestFlightSettings.IS_CUSTOM_SETTINGS))

        TestFlightProfile testFlightProfile = new TestFlightProfile(
                buildId: Long.parseLong(request.getParameter(TestFlightSettings.BUILD_ID)),
                id: request.getParameter(TestFlightSettings.PROFILE_ID),
                apiToken: request.getParameter(TestFlightSettings.API_TOKEN),
                teamToken: request.getParameter(TestFlightSettings.TEAM_TOKEN),
                distroLists: request.getParameter(TestFlightSettings.DISTRO_LIST),
                notifyDistroList: Boolean.valueOf(request.getParameter(TestFlightSettings.NOTIFY_DISTRO_LIST)),
                projectId: request.getParameter(TestFlightSettings.PROJECT_ID)
        )

        if (testFlightProfile.isValid() && !artifactRelativePath.isEmpty()) {
            SBuild build = buildsManager.findBuildInstanceById(testFlightProfile.buildId)

            try {
                TestFlightUploader uploader = new TestFlightUploader()
                UploadRequest testflightUploadRequest = new UploadRequest(apiToken: testFlightProfile.apiToken,
                        teamToken: testFlightProfile.teamToken, buildNotes: notes,
                        distributionLists: testFlightProfile.distroLists,
                        notifyDistributionList: testFlightProfile.notifyDistroList)

                BuildArtifacts buildArtifacts = build.getArtifacts(BuildArtifactsViewMode.VIEW_ALL)

                BuildArtifactHolder artifactHolder = buildArtifacts.findArtifact(artifactRelativePath)

                final File tempFile = createTempFile(getFilePrefix(artifactHolder.getName()), getFileSuffix(artifactHolder.getName()))
                tempFile.deleteOnExit()

                FileOutputStream out = new FileOutputStream(tempFile)
                IOUtils.copy(artifactHolder.getArtifact().getInputStream(), out)

                testflightUploadRequest.file = tempFile

                uploadResult = uploader.upload(testflightUploadRequest)

                persistCustomTestFlightProfile(customSettingsPosted, testFlightProfile)

                deleteTempFile(getFilePrefix(artifactHolder.getName()), getFileSuffix(artifactHolder.getName()))
            } catch (Exception e) {
                e.printStackTrace()
                uploadResult.succeeded = false
                uploadResult.message = 'The TestFlight upload encountered an error.  Check the logs.'
            }
        }

        mav.viewName = makeReturnUrl(testFlightProfile, uploadResult)

        return mav
    }

    private String makeReturnUrl(final TestFlightProfile profile, final UploadResult uploadResult) {
        String redirectUrl = "redirect:viewLog.html?buildId=${profile.buildId}&tab=buildResultsDiv&buildTypeId=${buildsManager.findBuildInstanceById(profile.buildId).getBuildType().getExternalId()}&"

        redirectUrl += "${uploadResult.succeeded ? TestFlightBuildExtension.PARAM_MESSAGE : TestFlightBuildExtension.PARAM_ERROR}=${uploadResult.message}"

        return redirectUrl
    }

    // Custom temp file handling.
    private File createTempFile(final String prefix, final String suffix) {
        String fileName = prefix + suffix
        String tempDirPath = System.getProperty("java.io.tmpdir")

        // Delete the temp file if one already exists with the same name.
        deleteTempFile(prefix, suffix)

        return new File(tempDirPath, fileName)
    }

    private void deleteTempFile(final String prefix, final String suffix) {
        String fileName = prefix + suffix
        String tempDirPath = System.getProperty("java.io.tmpdir")

        File fileCheck = new File("${tempDirPath}/${fileName}")
        if (fileCheck.exists() && fileCheck.isFile()) {
            fileCheck.delete()
        }
    }

    static String getFilePrefix(final String fileName) {
        return fileName.substring(0, (fileName.length() - getFileSuffix(fileName).length()))
    }

    // Assuming only ipa's and apk's are supported.
    static String getFileSuffix(final String fileName) {
        return fileName.endsWith(MobileBuildArtifacts.IOS_EXTENSION) ? MobileBuildArtifacts.IOS_EXTENSION : MobileBuildArtifacts.ANDROID_EXTENSION
    }

    /**
     * Persist the custom test flight profile settings using TeamCity's persistence if
     * there are any to save or update.
     *
     * @param customSettingsPosted
     * @param profile
     */
    private void persistCustomTestFlightProfile(final boolean customSettingsPosted, final TestFlightProfile profile) {
        if (customSettingsPosted) {
            TestFlightSettings settings = (TestFlightSettings) projectSettingsManager.getSettings(profile.projectId, TestFlightSettings.NAME)
            settings.updateProfile(profile)
            SProject project = buildsManager.findBuildInstanceById(profile.buildId).buildType.project
            project.persist()
        }
    }
}
