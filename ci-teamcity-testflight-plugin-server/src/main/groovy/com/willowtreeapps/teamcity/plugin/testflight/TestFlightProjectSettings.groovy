package com.willowtreeapps.teamcity.plugin.testflight

import jetbrains.buildServer.serverSide.settings.ProjectSettings
import org.jdom.Element


class TestFlightProjectSettings implements ProjectSettings {
    public static final String NAME = 'testflight_project_settings'

    public static final String CUSTOM_TEST_FLIGHT_SETTINGS_ELEMENT_NAME = 'customTestFlightSettings'
    public static final String PROJECT_ID = 'projectId'
    public static final String PROFILE_ID = 'profileId'
    public static final String API_TOKEN = 'apiToken'
    public static final String TEAM_TOKEN = 'teamToken'
    public static final String DISTRO_LIST = 'distroList'

    private Map<String, TestFlightProfile> testflightProfiles = [:]

    public TestFlightProfile getProjectProfile(final String projectId, final long buildId, final String internalBuildId) {
        TestFlightProfile profile = testflightProfiles.get(projectId)
        if (!profile){
            profile = new TestFlightProfile()
        }

        profile.buildId = buildId
        profile.internalBuildId = internalBuildId
        profile.projectId = projectId

        return profile
    }

    public void updateProfile(final TestFlightProfile profile) {
        testflightProfiles.put(profile.projectId, profile)
    }

    public void dispose() {
    }

    public void readFrom(final Element rootElement) {
        rootElement.getChildren(CUSTOM_TEST_FLIGHT_SETTINGS_ELEMENT_NAME).each {
            Element e = (Element) it
            testflightProfiles.put(e.getAttributeValue(PROJECT_ID),
                    new TestFlightProfile(id: e.getAttributeValue(PROFILE_ID),
                            apiToken: e.getAttributeValue(API_TOKEN),
                            teamToken: e.getAttributeValue(TEAM_TOKEN),
                            distroList: e.getAttributeValue(DISTRO_LIST)))
        }
    }

    public void writeTo(final Element parentElement) {
        testflightProfiles.each { key, value ->
            Element settings = new Element(CUSTOM_TEST_FLIGHT_SETTINGS_ELEMENT_NAME)
            settings.setAttribute(PROJECT_ID, key)
            settings.setAttribute(PROFILE_ID, value.id)
            settings.setAttribute(API_TOKEN, value.apiToken)
            settings.setAttribute(TEAM_TOKEN, value.teamToken)
            settings.setAttribute(DISTRO_LIST, value.distroList)
            parentElement.addContent(settings)
        }
    }
}
