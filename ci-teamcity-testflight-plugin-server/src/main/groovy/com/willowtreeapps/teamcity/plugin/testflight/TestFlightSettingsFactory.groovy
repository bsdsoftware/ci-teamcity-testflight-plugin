package com.willowtreeapps.teamcity.plugin.testflight

import jetbrains.buildServer.serverSide.settings.ProjectSettings
import jetbrains.buildServer.serverSide.settings.ProjectSettingsFactory
import org.jetbrains.annotations.NotNull

class TestFlightSettingsFactory implements ProjectSettingsFactory {

    @NotNull
    public ProjectSettings createProjectSettings(final String projectId) {
        return new TestFlightProjectSettings();
    }

}
