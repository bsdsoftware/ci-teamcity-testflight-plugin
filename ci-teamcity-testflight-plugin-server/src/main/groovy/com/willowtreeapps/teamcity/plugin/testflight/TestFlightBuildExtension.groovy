package com.willowtreeapps.teamcity.plugin.testflight

import groovy.util.slurpersupport.GPathResult
import jetbrains.buildServer.agent.LoggerFactory
import jetbrains.buildServer.serverSide.BuildStatistics
import jetbrains.buildServer.serverSide.BuildStatisticsOptions
import jetbrains.buildServer.serverSide.BuildsManager
import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.serverSide.SBuild
import jetbrains.buildServer.serverSide.SBuildType
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact
import jetbrains.buildServer.serverSide.artifacts.BuildArtifacts
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactsViewMode
import jetbrains.buildServer.users.SUser
import jetbrains.buildServer.web.openapi.PagePlaces
import jetbrains.buildServer.web.openapi.PlaceId
import jetbrains.buildServer.web.openapi.PluginDescriptor
import jetbrains.buildServer.web.openapi.SimplePageExtension
import org.apache.commons.compress.utils.IOUtils
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
        List<String> errors = [];
        Set<MavenTestFlightProfile> testflightOptions = new HashSet<MavenTestFlightProfile>()

        String buildTypeId = request.getParameter("buildTypeId")
        if (buildTypeId != null) {
            try {
                SBuildType sBuildType = projectManager.findBuildTypeByExternalId(buildTypeId)

                model.put("internalBuildID", sBuildType.getInternalId())

                Long buildId = Long.valueOf(request.getParameter("buildId"))
                SBuild build = buildsManager.findBuildInstanceById(buildId)

                BuildArtifacts buildArtifacts = build.getArtifacts(BuildArtifactsViewMode.VIEW_ALL)
                BuildArtifact pomFile = buildArtifacts.getArtifact('pom.xml')
                if (pomFile == null || !pomFile.isFile()){
                    errors << 'No pom.xml file was found in the build artifacts.'
                }

                GPathResult pom = new XmlSlurper().parse(pomFile.getInputStream())
                pom.profiles.profile.each{ prof ->

                    MavenTestFlightProfile p = new MavenTestFlightProfile(buildId: buildId, id: prof.id.text(),
                            apiToken: prof.properties[MavenTestFlightProfile.API_TOKEN_KEY],
                            teamToken: prof.properties[MavenTestFlightProfile.TEAM_TOKEN_KEY],
                            distroList: prof.properties[MavenTestFlightProfile.DISTRO_LIST_KEY],
                    )

                    if (p.isValid()){
                        testflightOptions << p
                    }
                }
            } catch (Exception e) {
                errors << e.message
            }

            model.put("testflightOptions", testflightOptions)
            model.put("errors", errors)

        }

    }
}