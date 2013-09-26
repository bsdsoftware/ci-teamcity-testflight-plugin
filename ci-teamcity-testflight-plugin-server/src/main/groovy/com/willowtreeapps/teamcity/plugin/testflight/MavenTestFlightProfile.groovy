package com.willowtreeapps.teamcity.plugin.testflight

class MavenTestFlightProfile {
    static final String API_TOKEN_KEY ='teamcity.testflight.ApiToken'
    static final String TEAM_TOKEN_KEY ='teamcity.testflight.TeamToken'
    static final String DISTRO_LIST_KEY ='teamcity.testflight.DistroList'

    Long buildId        // Team City build ID
    String id           // Maven profile ID.
    String apiToken     // test flight api token
    String teamToken    // test flight team token
    String distroList   // name of the test flight email distribution list

    boolean isValid(){
        return buildId != null && !id.isEmpty() && !apiToken.isEmpty() && !teamToken.isEmpty() && !distroList.isEmpty()
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        MavenTestFlightProfile that = (MavenTestFlightProfile) o

        if (id != that.id) return false

        return true
    }

    int hashCode() {
        return (id != null ? id.hashCode() : 0)
    }
}
