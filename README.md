ci-teamcity-testflight-plugin
=============================

This plugin is an extension to the <a href="http://www.jetbrains.com/teamcity/>Team City Continuous Integration Server</a> build results page.  It was build with Team City version 8.0.4.

It gives you the ability to publish mobile application build artifacts to <a href="https://testflightapp.com">TestFlight</a>.

The purpose for this plugin is to give you the ability to manually publish a build artifact from Team City to TestFlight.  If you want to incorporate a plugin that will publish to TestFlight with every build then please check out the <a href="http://willowtreeapps.github.io/maven-testflight-plugin/index.html">maven-testflight-plugin</a>.

If you are using <a href="http://maven.apache.org/">Maven</a> to build your software then you can incorporate Maven profiles into your pom.xml file that contain the required TestFlight parameters which will be automatically discovered and presented to the user.

If you are not using Maven then you can still use this plugin and manually enter the required TestFlight parameters.  Note that when using it this way the parameters you enter will be persisted using Team City's project-specific configuration settings.  The settings will then be presented thereafter for your project.  You will also be able to edit the settings if you choose.

Maven configuration is easy.  Add as many profiles as you like similar to those shown at <a href="http://willowtreeapps.github.io/maven-testflight-plugin/example.html">http://willowtreeapps.github.io/maven-testflight-plugin/example.html</a>.

In addition to the profiles you need to configure artifacts in the Team City General Settings page.

The following two artifacts should be configured for an Android application:

**/*.apk
pom.xml

The following two artifacts should be configured for an iOS application:

**/*.ipa
pom.xml

To deploy this plugin copy the deploy/ci-teamcity-testflight-plugin.zip file into your [user home]/.BuildServer/plugins directory and restart your Team City application.

Please report issues to:  https://github.com/willowtreeapps/ci-teamcity-testflight-plugin/issues