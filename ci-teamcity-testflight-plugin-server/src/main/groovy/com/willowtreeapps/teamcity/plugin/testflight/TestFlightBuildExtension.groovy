package com.willowtreeapps.teamcity.plugin.testflight

import groovy.util.slurpersupport.GPathResult
import jetbrains.buildServer.serverSide.BuildsManager
import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.serverSide.SBuild
import jetbrains.buildServer.serverSide.SBuildType
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact
import jetbrains.buildServer.serverSide.artifacts.BuildArtifacts
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactsViewMode
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
        Set<String> errors = []
        Set<MavenTestFlightProfile> testflightOptions = []
        Set<MobileBuildArtifacts> mobileArtifacts = []

        String buildTypeId = request.getParameter("buildTypeId")
        if (buildTypeId != null) {
            try {
                SBuildType sBuildType = projectManager.findBuildTypeByExternalId(buildTypeId)

                Long buildId = Long.valueOf(request.getParameter("buildId"))
                SBuild build = buildsManager.findBuildInstanceById(buildId)

                BuildArtifacts buildArtifacts = build.getArtifacts(BuildArtifactsViewMode.VIEW_ALL)

                readPom(buildArtifacts, testflightOptions, buildId, sBuildType)

                // Get the apk or ipa artifacts.
                BuildArtifact rootArtifactDirectory = buildArtifacts.getRootArtifact()

                searchForMobileArtifacts(rootArtifactDirectory, mobileArtifacts)


            } catch (Exception e) {
                errors << e.message
            }

            model.put("testflightOptions", testflightOptions)
            model.put("errors", errors)
            model.put("mobileArtifacts", mobileArtifacts)
            model.put("testFlightEnabled", testflightOptions.size() > 0)

        }

    }

    private void searchForMobileArtifacts(BuildArtifact directory, Set<MobileBuildArtifacts> mobileArtifacts) {
        for (BuildArtifact ba : directory.getChildren()) {
            if (ba.isFile() && (ba.getName().endsWith(MobileBuildArtifacts.IOS_EXTENSION) || ba.getName().endsWith(MobileBuildArtifacts.ANDROID_EXTENSION))) {
                mobileArtifacts << new MobileBuildArtifacts(relativePath: ba.getRelativePath(), name: ba.getName())
            } else if (ba.isDirectory()) {
                searchForMobileArtifacts(ba, mobileArtifacts)
            }
        }
    }

    private void readPom(BuildArtifacts buildArtifacts, Set<MavenTestFlightProfile> testflightOptions, long buildId, sBuildType) {
        // The pom.xml file must be configured as an artifact in the Team City Maven build step.
        BuildArtifact pomFile = buildArtifacts.getArtifact('pom.xml')
        if (pomFile != null && pomFile.isFile()) {
            GPathResult pom = new XmlSlurper().parse(pomFile.getInputStream())
            pom.profiles.profile.each { prof ->

                MavenTestFlightProfile p = new MavenTestFlightProfile(internalBuildId: sBuildType.getInternalId(),
                        buildId: buildId, id: prof.id.text(),
                        apiToken: prof.properties[MavenTestFlightProfile.API_TOKEN_KEY],
                        teamToken: prof.properties[MavenTestFlightProfile.TEAM_TOKEN_KEY],
                        distroList: prof.properties[MavenTestFlightProfile.DISTRO_LIST_KEY],
                )

                if (p.isValid()) {
                    testflightOptions << p
                }
            }
        }
    }
}