package com.willowtreeapps.teamcity.extension.testflight

import jetbrains.buildServer.serverSide.settings.ProjectSettings
import org.jdom.Element


class TestFlightSettings implements ProjectSettings {
    public static final String NAME = 'testflight_project_settings'

    public static final String CUSTOM_TEST_FLIGHT_SETTINGS_ELEMENT_NAME = 'customTestFlightSettings'
    public static final String ARTIFACT_RELATIVE_PATH = 'artifactRelativePath'
    public static final String BUILD_ID = 'buildId'
    public static final String BUILD_TYPE_ID = 'buildTypeId'
    public static final String IS_CUSTOM_SETTINGS = 'isCustomSettings'
    public static final String NOTES = 'notes'
    public static final String PROJECT_ID = 'projectId'
    public static final String PROFILE_ID = 'id'
    public static final String API_TOKEN = 'apiToken'
    public static final String TEAM_TOKEN = 'teamToken'
    public static final String DISTRO_LIST = 'distroLists'
    public static final String NOTIFY_DISTRO_LIST = 'notifyDistroList'

    private Map<String, TestFlightProfile> testflightProfiles = [:]

    public TestFlightProfile getProjectProfile(final String projectId, final long buildId) {
        TestFlightProfile profile = testflightProfiles.get(projectId)
        if (!profile) {
            profile = new TestFlightProfile()
        }

        profile.buildId = buildId
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
                            distroLists: e.getAttributeValue(DISTRO_LIST),
                            notifyDistroList: e.getAttributeValue(NOTIFY_DISTRO_LIST) != null ? String.valueOf(e.getAttributeValue(NOTIFY_DISTRO_LIST)) : false))
        }
    }

    public void writeTo(final Element parentElement) {
        testflightProfiles.each { key, value ->
            Element settings = new Element(CUSTOM_TEST_FLIGHT_SETTINGS_ELEMENT_NAME)
            settings.setAttribute(PROJECT_ID, key)
            settings.setAttribute(PROFILE_ID, value.id)
            settings.setAttribute(API_TOKEN, value.apiToken)
            settings.setAttribute(TEAM_TOKEN, value.teamToken)
            settings.setAttribute(DISTRO_LIST, value.distroLists)
            settings.setAttribute(NOTIFY_DISTRO_LIST, String.valueOf(value.notifyDistroList))
            parentElement.addContent(settings)
        }
    }
}
