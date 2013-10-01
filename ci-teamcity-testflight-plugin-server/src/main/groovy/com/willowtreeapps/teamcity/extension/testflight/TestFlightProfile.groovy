package com.willowtreeapps.teamcity.extension.testflight


class TestFlightProfile {
    static final String API_TOKEN_KEY = 'teamcity.testflight.ApiToken'
    static final String TEAM_TOKEN_KEY = 'teamcity.testflight.TeamToken'
    static final String DISTRO_LIST_KEY = 'teamcity.testflight.DistroList'
    static final String NOTIFY_DISTRO_LIST_KEY = 'teamcity.testflight.NotifyDistroList'

    Long buildId                // Team City build ID
    String id                   // Maven profile ID.
    String apiToken             // test flight api token
    String teamToken            // test flight team token
    String distroLists          // name of the test flight email distribution list
    String projectId            // project ID from an SBuildType
    boolean notifyDistroList    // setting to tell Test Flight to notify the distribution list

    boolean isValid() {
        return buildId != null && !id.isEmpty() && !apiToken.isEmpty() && !teamToken.isEmpty()
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        TestFlightProfile that = (TestFlightProfile) o

        if (id != that.id) return false

        return true
    }

    int hashCode() {
        return (id != null ? id.hashCode() : 0)
    }
}
