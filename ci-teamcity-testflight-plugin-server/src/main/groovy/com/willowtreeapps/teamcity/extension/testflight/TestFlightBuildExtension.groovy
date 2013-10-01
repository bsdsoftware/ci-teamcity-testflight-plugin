package com.willowtreeapps.teamcity.extension.testflight

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
    public static final String MODEL_TEST_FLIGHT_OPTIONS = 'testflightOptions'
    public static final String MODEL_MOBILE_ARTIFACTS = 'mobileArtifacts'
    public static final String MODEL_HAS_POM_SETTINGS = 'hasPomSettings'
    public static final String MODEL_CUSTOM_PROFILE_SETTINGS = 'customProfileSettings'
    public static final String MODEL_ERRORS = 'errors'
    public static final String MODEL_MESSAGES = 'messages'
    public static final String PARAM_MESSAGE = 'message'
    public static final String PARAM_ERROR = 'error'

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
        Set<TestFlightProfile> testflightOptions = []
        Set<MobileBuildArtifacts> mobileArtifacts = []

        String buildTypeId = request.getParameter(TestFlightSettings.BUILD_TYPE_ID)
        if (buildTypeId != null) {
            try {
                SBuildType sBuildType = projectManager.findBuildTypeByExternalId(buildTypeId)
                Long buildId = Long.valueOf(request.getParameter(TestFlightSettings.BUILD_ID))
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

                if (!mobileArtifacts) {
                    addError('no_field', 'No Android or iOS artifacts were found.  Check your artifacts configuration.', model)
                }

            } catch (Exception e) {
                e.printStackTrace()
                addError('no_field', 'There was an error setting up the TestFlight form.  Check the logs.', model)
            }

            model.put(MODEL_TEST_FLIGHT_OPTIONS, testflightOptions)
            model.put(MODEL_MOBILE_ARTIFACTS, mobileArtifacts)
            model.put(MODEL_HAS_POM_SETTINGS, hasPomSettings)
            model.put(MODEL_CUSTOM_PROFILE_SETTINGS, customProfileSettings)

            // Mainly for redirects back to the build tab.
            checkTestFlightUploadResults(model, request)
        }

    }

    /**
     * Retrieves test flight settings from the Team City project configuration
     * if any were saved by the user.
     *
     * @param buildId
     * @param sBuildType
     * @return a profile object for use on the custom profile form
     */
    private TestFlightProfile makeCustomSettings(long buildId, final SBuildType sBuildType) {
        TestFlightSettings settings = (TestFlightSettings) projectSettingsManager.getSettings(sBuildType.project.projectId, TestFlightSettings.NAME)
        return settings.getProjectProfile(sBuildType.project.projectId, buildId)
    }

    public static void addError(final String field, final String errorMsg, final Map<String, Object> model) {
        Map<String, String> errors = model.get(MODEL_ERRORS)
        if (!errors) {
            errors = [:]
        }
        errors.put(field, errorMsg)
        model.put(MODEL_ERRORS, errors)
    }

    public static void addMessage(final String message, final Map<String, Object> model) {
        def msgs = model.get(MODEL_MESSAGES)
        if (!msgs) {
            msgs = []
        }
        msgs << message
        model.put(MODEL_MESSAGES, msgs)
    }

    private void checkTestFlightUploadResults(final Map<String, Object> model, final HttpServletRequest request) {
        if (request.getParameter(PARAM_MESSAGE)) {
            addMessage(request.getParameter(PARAM_MESSAGE), model)
        }
        if (request.getParameter(PARAM_ERROR)) {
            addError('no_field', request.getParameter(PARAM_ERROR), model)
        }
    }

    /**
     * Recursive method that looks for apk and ipa files in all artifact directories.
     *
     * @param directory
     * @param mobileArtifacts
     */
    private void searchForMobileArtifacts(final BuildArtifact directory, final Set<MobileBuildArtifacts> mobileArtifacts) {
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
     * @return true if there are any valid test flight configurations found in the pom
     */
    private boolean readPom(final BuildArtifacts buildArtifacts, final Set<TestFlightProfile> testflightOptions,
                            long buildId, SBuildType sBuildType) {
        // The pom.xml file must be configured as an artifact in the Team City Maven build step.
        BuildArtifact pomFile = buildArtifacts.getArtifact('pom.xml')
        if (pomFile != null && pomFile.isFile()) {
            GPathResult pom = new XmlSlurper().parse(pomFile.getInputStream())
            pom.profiles.profile.each { prof ->

                TestFlightProfile p = new TestFlightProfile(buildId: buildId, id: prof.id.text(),
                        apiToken: prof.properties[TestFlightProfile.API_TOKEN_KEY],
                        teamToken: prof.properties[TestFlightProfile.TEAM_TOKEN_KEY],
                        distroLists: prof.properties[TestFlightProfile.DISTRO_LIST_KEY],
                        notifyDistroList: prof.properties[TestFlightProfile.NOTIFY_DISTRO_LIST_KEY] ? Boolean.valueOf((String) prof.properties[TestFlightProfile.NOTIFY_DISTRO_LIST_KEY]) : false,
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