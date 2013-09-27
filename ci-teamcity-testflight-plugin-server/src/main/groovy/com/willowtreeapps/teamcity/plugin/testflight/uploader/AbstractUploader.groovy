package com.willowtreeapps.teamcity.plugin.testflight.uploader

import org.apache.http.HttpEntity
import org.apache.http.HttpHost
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.DefaultHttpClient
import org.json.simple.parser.JSONParser
import org.json.simple.parser.ParseException

abstract class AbstractUploader {
    public abstract Map upload(UploadRequest ur) throws IOException, org.json.simple.parser.ParseException

    protected Map send(UploadRequest ur, DefaultHttpClient httpClient, HttpHost targetHost, HttpPost httpPost) throws IOException, ParseException {
        HttpResponse response = httpClient.execute(targetHost, httpPost)
        HttpEntity resEntity = response.getEntity()

        InputStream is = resEntity.getContent()

        int statusCode = response.getStatusLine().getStatusCode()

        if (statusCode != 200 && statusCode != 201) {
            String responseBody = new Scanner(is).useDelimiter("\\A").next()
            throw new UploadException(statusCode, responseBody, response)
        }

        JSONParser parser = new JSONParser()

        return (Map) parser.parse(new BufferedReader(new InputStreamReader(is)))
    }
}
