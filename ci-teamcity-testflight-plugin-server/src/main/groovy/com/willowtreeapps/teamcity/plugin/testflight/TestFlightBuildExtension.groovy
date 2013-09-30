package com.willowtreeapps.teamcity.plugin.testflight

import groovy.util.slurpersupport.GPathResult
import jetbrains.buildServer.serverSide.BuildsManager
import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.serverSide.SBuild
import jetbrains.buildServer.serverSide.SBuildType
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact
import jetbrains.buildServer.serverSide.artifacts.BuildArtifacts
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactsViewMode
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager
import jetbrains.buildServer.web.openapi.PagePlaces
import jetbrains.buildServer.web.openapi.PlaceId
import jetbrains.buildServer.web.openapi.PluginDescriptor
import jetbrains.buildServer.web.openapi.SimplePageExtension
import org.jetbrains.annotations.NotNull

import javax.servlet.http.HttpServletRequest

class TestFlightBuildExtension extends SimplePageExtension {
    private final ProjectManager projectManager
    private final BuildsManager buildsManager
    private ProjectSettingsManager projectSettingsManager

    public TestFlightBuildExtension(@NotNull PagePlaces pagePlaces,
                                    @NotNull PluginDescriptor descriptor,
                                    @NotNull ProjectManager projectManager,
                                    @NotNull BuildsManager buildsManager,
                                    @NotNull ProjectSettingsManager projectSettingsManager
    ) {
        super(pagePlaces, PlaceId.BUILD_RESULTS_FRAGMENT, 'testflightBuildExtension', descriptor.getPluginResourcesPath('testflightBuildExtension.jsp'))
        this.projectManager = projectManager
        this.buildsManager = buildsManager
        this.projectSettingsManager = projectSettingsManager
        register()
    }

    @Override
    public boolean isAvailable(@NotNull HttpServletRequest request) {
        return super.isAvailable(request)
    }

    @Override
    public void fillModel(@NotNull Map<String, Object> model, @NotNull HttpServletRequest request) {
        boolean hasPomSettings = false
        TestFlightProfile customProfileSettings = null
        Set<String> errors = []
        Set<TestFlightProfile> testflightOptions = []
        Set<MobileBuildArtifacts> mobileArtifacts = []

        String buildTypeId = request.getParameter('buildTypeId')
        if (buildTypeId != null) {
            try {
                SBuildType sBuildType = projectManager.findBuildTypeByExternalId(buildTypeId)
                Long buildId = Long.valueOf(request.getParameter('buildId'))
                SBuild build = buildsManager.findBuildInstanceById(buildId)

                BuildArtifacts buildArtifacts = build.getArtifacts(BuildArtifactsViewMode.VIEW_ALL)

                hasPomSettings = readPom(buildArtifacts, testflightOptions, buildId, sBuildType)

                if (!hasPomSettings) {
                    // look for custom settings
                    customProfileSettings = makeCustomSettings(buildId, sBuildType)
                }

                // Get the apk or ipa artifacts.
                BuildArtifact rootArtifactDirectory = buildArtifacts.getRootArtifact()
                searchForMobileArtifacts(rootArtifactDirectory, mobileArtifacts)

            } catch (Exception e) {
                errors << 'There was an error setting up the TestFlight form.'
                e.printStackTrace()
            }

            model.put('testflightOptions', testflightOptions)

            model.put('mobileArtifacts', mobileArtifacts)
            model.put('hasPomSettings', hasPomSettings)
            model.put('customProfileSettings', customProfileSettings)

            checkTestFlightUploadResults(model, request, errors)
            model.put('errors', errors)
        }

    }

    private TestFlightProfile makeCustomSettings(long buildId, SBuildType sBuildType) {
        TestFlightProjectSettings settings = (TestFlightProjectSettings) projectSettingsManager.getSettings(sBuildType.project.projectId, TestFlightProjectSettings.NAME)
        return settings.getProjectProfile(sBuildType.project.projectId, buildId, sBuildType.internalId)
    }

    private void checkTestFlightUploadResults(final Map<String, Object> model, final HttpServletRequest request, final Set<String> errors){
        if (request.getParameter('testflightUploadSucceeded')){
            boolean success = Boolean.valueOf(request.getParameter('testflightUploadSucceeded'))
            if (success){
                model.put('testflightUploadSucceeded', 'The TestFlight upload succeeded.')
            } else {
                errors << 'The TestFlight upload failed.'
            }
        }
    }

    /**
     * Recursive method that looks for apk and ipa files in all artifact directories.
     *
     * @param directory
     * @param mobileArtifacts
     */
    private void searchForMobileArtifacts(BuildArtifact directory, Set<MobileBuildArtifacts> mobileArtifacts) {
        for (BuildArtifact ba : directory.getChildren()) {
            if (ba.isFile() && (ba.getName().endsWith(MobileBuildArtifacts.IOS_EXTENSION) || ba.getName().endsWith(MobileBuildArtifacts.ANDROID_EXTENSION))) {
                mobileArtifacts << new MobileBuildArtifacts(relativePath: ba.getRelativePath(), name: ba.getName())
            } else if (ba.isDirectory()) {
                searchForMobileArtifacts(ba, mobileArtifacts)
            }
        }
    }

    /**
     * Try to read a Maven pom.xml file and if one exists attempt to get
     * the TestFlight parameters from it.
     *
     * @param buildArtifacts
     * @param testflightOptions
     * @param buildId
     * @param sBuildType
     */
    private boolean readPom(BuildArtifacts buildArtifacts, Set<TestFlightProfile> testflightOptions,
                         long buildId, SBuildType sBuildType) {
        // The pom.xml file must be configured as an artifact in the Team City Maven build step.
        BuildArtifact pomFile = buildArtifacts.getArtifact('pom.xml')
        if (pomFile != null && pomFile.isFile()) {
            GPathResult pom = new XmlSlurper().parse(pomFile.getInputStream())
            pom.profiles.profile.each { prof ->

                TestFlightProfile p = new TestFlightProfile(internalBuildId: sBuildType.getInternalId(),
                        buildId: buildId, id: prof.id.text(),
                        apiToken: prof.properties[TestFlightProfile.API_TOKEN_KEY],
                        teamToken: prof.properties[TestFlightProfile.TEAM_TOKEN_KEY],
                        distroList: prof.properties[TestFlightProfile.DISTRO_LIST_KEY],
                        projectId: sBuildType.project.projectId
                )

                if (p.isValid()) {
                    testflightOptions << p
                }
            }
        }
        return testflightOptions.size() > 0
    }

}