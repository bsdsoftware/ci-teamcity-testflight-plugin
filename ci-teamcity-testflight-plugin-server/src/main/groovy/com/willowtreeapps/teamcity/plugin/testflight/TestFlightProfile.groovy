package com.willowtreeapps.teamcity.plugin.testflight


class TestFlightProfile {
    static final String API_TOKEN_KEY ='teamcity.testflight.ApiToken'
    static final String TEAM_TOKEN_KEY ='teamcity.testflight.TeamToken'
    static final String DISTRO_LIST_KEY ='teamcity.testflight.DistroList'

    String internalBuildId      // Team City internal build ID (ie. bt1)
    Long buildId                // Team City build ID
    String id                   // Maven profile ID.
    String apiToken             // test flight api token
    String teamToken            // test flight team token
    String distroList           // name of the test flight email distribution list
    String artifactToPublish    // name of the artifact selected to publish
    String projectId            // project ID from an SBuildType

    boolean isValid(){
        return !internalBuildId.isEmpty() && buildId != null && !id.isEmpty() && !apiToken.isEmpty() && !teamToken.isEmpty() && !distroList.isEmpty()
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
