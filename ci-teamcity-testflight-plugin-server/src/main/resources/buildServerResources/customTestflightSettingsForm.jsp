<%@ include file="/include.jsp" %>
<%@ include file="taglibs.jsp" %>

<c:url var="actionUrl" value="/testFlight.html"/>




<sf:form action="${actionUrl}" method="post" id="testflightForm" name="testflightForm">
    <input type="hidden" id="isCustomSettings" name="isCustomSettings" value="true">
    <input type="hidden" id="internalBuildId" name="internalBuildId" value="${customProfileSettings.internalBuildId}"/>
    <input type="hidden" id="buildId" name="buildId" value="${customProfileSettings.buildId}"/>
    <input type="hidden" id="projectId" name="projectId" value="${customProfileSettings.projectId}"/>

    <h3>Enter TestFlight Parameters</h3>
    <div class="fieldlabel" id="profileNameLbl">Profile Name:</div>
    <div class="fieldvalue"><input type="text" id="id" name="id" value="<c:out value="${customProfileSettings.id}"/>">
    </div>
    <div class="fieldlabel" id="apiTokenLbl">Test Flight API Token:</div>
    <div class="fieldvalue"><input type="text" id="apiToken" name="apiToken"
                                   value="<c:out value="${customProfileSettings.apiToken}"/>" size="100"></div>
    <div class="fieldlabel" id="teamTokenLbl">Test Flight Team Token:</div>
    <div class="fieldvalue"><input type="text" id="teamToken" name="teamToken"
                                   value="<c:out value="${customProfileSettings.teamToken}"/>" size="100"></div>
    <div class="fieldlabel" id="distroListLbl">Test Flight Distribution List:</div>
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
        <input type="button" value="Publish To TestFlight" onclick="validateForm();"
                <c:if test="${fn:length(mobileArtifacts) eq 0}"> disabled</c:if>>
    </div>
</sf:form>

<script type="text/javascript">
    function validateForm(){
        var hasErrors = false;
        var profileId = $F('id');
        if (!profileId){
            $('profileNameLbl').className = 'fieldError';
            $('id').className = 'error';
            hasErrors = true;
        }

        var apiToken = $F('apiToken');
        if (!apiToken){
            $('apiTokenLbl').className = 'fieldError';
            $('apiToken').className = 'error';
            hasErrors = true;
        }

        var teamToken = $F('teamToken');
        if (!teamToken){
            $('teamTokenLbl').className = 'fieldError';
            $('teamToken').className = 'error';
            hasErrors = true;
        }

        var distroList = $F('distroList');
        if (!distroList){
            $('distroListLbl').className = 'fieldError';
            $('distroList').className = 'error';
            hasErrors = true;
        }

        if (hasErrors){
            $('formValidationErrors').update('All form fields are required.');
            $('formValidationErrors').show();
        } else {
            $('testflightForm').submit();
        }
    }
</script>