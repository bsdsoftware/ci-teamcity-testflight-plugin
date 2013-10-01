package com.willowtreeapps.teamcity.extension.testflight

class MobileBuildArtifacts {
    public static final String IOS_EXTENSION = '.ipa'
    public static final String ANDROID_EXTENSION = '.apk'

    String name, relativePath

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        MobileBuildArtifacts that = (MobileBuildArtifacts) o

        if (name != that.name) return false

        return true
    }

    int hashCode() {
        return (name != null ? name.hashCode() : 0)
    }
}
