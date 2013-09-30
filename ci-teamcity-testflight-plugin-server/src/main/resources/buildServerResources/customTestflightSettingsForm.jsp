<%@ include file="/include.jsp" %>
<%@ include file="taglibs.jsp" %>


<c:url var="actionUrl" value="/testFlight.html"/>
<sf:form action="${actionUrl}" method="post">
    <input type="hidden" id="customSettings" name="customSettings" value="true">
    <input type="hidden" id="internalBuildId" name="internalBuildId" value="${customProfileSettings.internalBuildId}"/>
    <input type="hidden" id="buildId" name="buildId" value="${customProfileSettings.buildId}"/>
    <input type="hidden" id="projectId" name="projectId" value="${customProfileSettings.projectId}"/>

    <h3>Enter TestFlight Parameters</h3>

    <div class="fieldlabel">Profile Name:</div>
    <div class="fieldvalue"><input type="text" id="id" name="id" value="<c:out value="${customProfileSettings.id}"/>">
    </div>
    <div class="fieldlabel">Test Flight API Token:</div>
    <div class="fieldvalue"><input type="text" id="apiToken" name="apiToken"
                                   value="<c:out value="${customProfileSettings.apiToken}"/>" size="100"></div>
    <div class="fieldlabel">Test Flight Team Token:</div>
    <div class="fieldvalue"><input type="text" id="teamToken" name="teamToken"
                                   value="<c:out value="${customProfileSettings.teamToken}"/>" size="100"></div>
    <div class="fieldlabel">Test Flight Distribution List:</div>
    <div class="fieldvalue"><input type="text" id="distroList" name="distroList"
                                   value="<c:out value="${customProfileSettings.distroList}"/>"></div>
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
</sf:form>