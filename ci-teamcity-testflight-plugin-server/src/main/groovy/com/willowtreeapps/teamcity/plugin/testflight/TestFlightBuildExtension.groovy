package com.willowtreeapps.teamcity.plugin.testflight

import jetbrains.buildServer.serverSide.BuildsManager
import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.serverSide.SBuildType
import jetbrains.buildServer.web.openapi.PagePlaces
import jetbrains.buildServer.web.openapi.PlaceId
import jetbrains.buildServer.web.openapi.PluginDescriptor
import jetbrains.buildServer.web.openapi.SimplePageExtension
import org.jetbrains.annotations.NotNull

import javax.servlet.http.HttpServletRequest

class TestFlightBuildExtension extends SimplePageExtension {
    ProjectManager projectManager
    BuildsManager buildsManager

    public TestFlightBuildExtension(@NotNull PagePlaces pagePlaces,
                                    @NotNull PluginDescriptor descriptor,
                                    @NotNull ProjectManager projectManager,
                                    @NotNull BuildsManager buildsManager
    ) {
        super(pagePlaces, PlaceId.BUILD_RESULTS_FRAGMENT, "testflightBuildExtension", descriptor.getPluginResourcesPath("testflightBuildExtension.jsp"))
        this.projectManager = projectManager
        this.buildsManager = buildsManager
        register()
    }

    @Override
    public boolean isAvailable(@NotNull HttpServletRequest request) {
        return super.isAvailable(request)
    }

    @Override
    public void fillModel(@NotNull Map<String, Object> model, @NotNull HttpServletRequest request) {
        String buildTypeId = request.getParameter("buildTypeId")
        if (buildTypeId != null) {
            SBuildType sBuildType = projectManager.findBuildTypeByExternalId(buildTypeId)
            String buildConfigName = sBuildType.getProject().getName() + ": " + sBuildType.getName()
            model.put("build_config_name", buildConfigName)
        }

    }
}