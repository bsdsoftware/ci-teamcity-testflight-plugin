package com.willowtreeapps.teamcity.plugin.testflight

import com.willowtreeapps.teamcity.plugin.testflight.uploader.TestFlightUploader
import com.willowtreeapps.teamcity.plugin.testflight.uploader.UploadRequest
import jetbrains.buildServer.controllers.BaseController
import jetbrains.buildServer.controllers.SimpleView
import jetbrains.buildServer.serverSide.BuildsManager
import jetbrains.buildServer.serverSide.SBuild
import jetbrains.buildServer.serverSide.SBuildServer
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactHolder
import jetbrains.buildServer.serverSide.artifacts.BuildArtifacts
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactsViewMode
import jetbrains.buildServer.web.openapi.WebControllerManager
import org.apache.commons.io.IOUtils
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import org.springframework.web.servlet.ModelAndView

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class TestFlightController extends BaseController {

    private final WebControllerManager myManager
    private final BuildsManager buildsManager

    private final String TAG_NAME = 'SUBMITTED_TO_TEST_FLIGHT'

    public TestFlightController(
            @NotNull final SBuildServer sBuildServer,
            @NotNull final WebControllerManager manager,
            @NotNull BuildsManager buildsManager
    ) {
        super(sBuildServer)
        myManager = manager
        this.buildsManager = buildsManager
    }

    public void register() {
        myManager.registerController("/testFlight.html", this)
    }

    @Nullable
    @Override
    protected ModelAndView doHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Exception {
        boolean testFlightSuccess = false
        String error = ''
        Map uploadResults = [:]

        String artifactRelativePath = request.getParameter('artifactRelativePath')
        String notes = request.getParameter('notes')
        MavenTestFlightProfile mavenTestFlightProfile = new MavenTestFlightProfile(
                internalBuildId: request.getParameter('internalBuildId'),
                buildId: Long.parseLong(request.getParameter('buildId')),
                id: request.getParameter('id'),
                apiToken: request.getParameter('apiToken'),
                teamToken: request.getParameter('teamToken'),
                distroList: request.getParameter('distroList')
        )

        if (mavenTestFlightProfile.isValid() && !artifactRelativePath.isEmpty()) {

            // TODO
            // Send an email to the submitter
            // post a confirmation message to the overview page if we can, or show a new html page

            SBuild build = buildsManager.findBuildInstanceById(mavenTestFlightProfile.buildId)

            try {
                TestFlightUploader uploader = new TestFlightUploader()
                UploadRequest testflightUploadRequest = new UploadRequest(apiToken: mavenTestFlightProfile.apiToken,
                        teamToken: mavenTestFlightProfile.teamToken, buildNotes: notes,
                        lists: mavenTestFlightProfile.distroList, notifyTeam: true, replace: true,
                dsymFile: null)

                BuildArtifacts buildArtifacts = build.getArtifacts(BuildArtifactsViewMode.VIEW_ALL)

                BuildArtifactHolder artifactHolder = buildArtifacts.findArtifact(artifactRelativePath)

                if (!artifactHolder.isAvailable()) {
                    error += "The artifact was not found on disk for the relative path: ${artifactRelativePath}"
                } else {
                    final File tempFile = createTempFile(getFilePrefix(artifactHolder.getName()), getFileSuffix(artifactHolder.getName()))
                    tempFile.deleteOnExit()

                    FileOutputStream out = new FileOutputStream(tempFile)
                    IOUtils.copy(artifactHolder.getArtifact().getInputStream(), out)

                    testflightUploadRequest.file = tempFile
                    assert testflightUploadRequest.file != null

                    uploadResults.putAll(uploader.upload(testflightUploadRequest))

                    testFlightSuccess = true
                }
            } catch (Exception e) {
                error += 'The TestFlight upload could not be completed.'
            }

//            TODO:  Got an unmodifiable collections error when adding a tag.
//            if (testFlightSuccess) {
//                List<String> tags = build.getTags()
//                if (!tags.contains(TAG_NAME)) {
//                    tags.add(TAG_NAME)
//                }
//                build.setTags(build.getOwner(), tags)
//            }
        }

        ModelAndView mav = SimpleView.createTextView(uploadResults.toMapString())
        return mav
    }

    public File createTempFile(String prefix, String suffix){
        String fileName = prefix + suffix
        String tempDirPath = System.getProperty("java.io.tmpdir")

        File fileCheck = new File("${tempDirPath}/${fileName}")
        if(fileCheck.exists() && fileCheck.isFile()){
            fileCheck.delete()
        }
        return new File(tempDirPath, fileName)
    }

    // Assuming only ipa's and apk's are supported.
    static String getFilePrefix(String fileName) {
        return fileName.substring(0, (fileName.length() - getFileSuffix(fileName).length()))
    }

    static String getFileSuffix(String fileName) {
        return fileName.endsWith(MobileBuildArtifacts.IOS_EXTENSION) ? MobileBuildArtifacts.IOS_EXTENSION : MobileBuildArtifacts.ANDROID_EXTENSION
    }
}
