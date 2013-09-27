package com.willowtreeapps.teamcity.plugin.testflight.uploader

import org.apache.http.HttpHost
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.entity.mime.content.StringBody
import org.apache.http.impl.client.DefaultHttpClient

class TestFlightUploader extends AbstractUploader {
    private static final String HOST = "testflightapp.com"

    private static final String POST = "/api/builds.json"

    public Map upload(UploadRequest ur) throws IOException, org.json.simple.parser.ParseException {

        DefaultHttpClient httpClient = new DefaultHttpClient()

        HttpHost targetHost = new HttpHost(HOST)
        HttpPost httpPost = new HttpPost(POST)
        FileBody fileBody = new FileBody(ur.file)

        MultipartEntity entity = new MultipartEntity()
        entity.addPart("api_token", new StringBody(ur.apiToken))
        entity.addPart("team_token", new StringBody(ur.teamToken))
        entity.addPart("notes", new StringBody(ur.buildNotes))
        entity.addPart("file", fileBody)

        if (ur.dsymFile != null) {
            FileBody dsymFileBody = new FileBody(ur.dsymFile)
            entity.addPart("dsym", dsymFileBody)
        }

        if (ur.lists.length() > 0) {
            entity.addPart("distribution_lists", new StringBody(ur.lists))
        }

        entity.addPart("notify", new StringBody(ur.notifyTeam ? "True" : "False"))
        entity.addPart("replace", new StringBody(ur.replace ? "True" : "False"))
        httpPost.setEntity(entity)

        return this.send(ur, httpClient, targetHost, httpPost)
    }
}
