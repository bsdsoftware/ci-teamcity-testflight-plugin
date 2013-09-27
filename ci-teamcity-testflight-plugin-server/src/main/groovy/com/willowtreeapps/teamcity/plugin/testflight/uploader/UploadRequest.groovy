package com.willowtreeapps.teamcity.plugin.testflight.uploader

class UploadRequest {
    public String apiToken
    public String teamToken
    public Boolean notifyTeam
    public String buildNotes
    public File file
    public File dsymFile
    public String lists
    public Boolean replace
    public Boolean status
    public Boolean privateDownload
}
