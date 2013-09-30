<%@ include file="/include.jsp" %>
<%@ include file="taglibs.jsp" %>
<c:url var="actionUrl" value="/testFlight.html"/>
<c:forEach items="${testflightOptions}" var="testflight">
    <sf:form action="${actionUrl}" method="post">
        <input type="hidden" id="customSettings" name="customSettings" value="false">
        <input type="hidden" id="internalBuildId" name="internalBuildId" value="${testflight.internalBuildId}"/>
        <input type="hidden" id="buildId" name="buildId" value="${testflight.buildId}"/>
        <input type="hidden" id="id" name="id" value="${testflight.id}"/>
        <input type="hidden" id="apiToken" name="apiToken" value="${testflight.apiToken}"/>
        <input type="hidden" id="teamToken" name="teamToken" value="${testflight.teamToken}"/>
        <input type="hidden" id="distroList" name="distroList" value="${testflight.distroList}"/>
        <input type="hidden" id="projectId" name="projectId" value="${testflight.projectId}"/>

        <div class="fieldlabel">Profile Name:</div>
        <div class="fieldvalue"><c:out value="${testflight.id}"/></div>
        <div class="fieldlabel">Test Flight API Token:</div>
        <div class="fieldvalue"><c:out value="${testflight.apiToken}"/></div>
        <div class="fieldlabel">Test Flight Team Token:</div>
        <div class="fieldvalue"><c:out value="${testflight.teamToken}"/></div>
        <div class="fieldlabel">Test Flight Distribution List:</div>
        <div class="fieldvalue"><c:out value="${testflight.distroList}"/></div>
        <div class="fieldlabel">Select an artifact to publish:</div>
        <div class="fieldvalue">
            <select name="artifactRelativePath" id="artifactRelativePath">
                <c:forEach items="${mobileArtifacts}" var="ma">
                    <option value="${ma.relativePath}"><c:out value="${ma.name}"/></option>
                </c:forEach>
            </select>
        </div>
        <div class="fieldlabel">Build Notes:</div>
        <div class="fieldvalue"><textarea name="notes" id="notes" cols="50" rows="3"></textarea></div>
        <div>
            <input type="submit" value="Publish To TestFlight">
        </div>
        <hr/>
    </sf:form>
</c:forEach>