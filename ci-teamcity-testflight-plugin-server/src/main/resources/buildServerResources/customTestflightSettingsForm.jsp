<%@ include file="/include.jsp" %>
<%@ include file="taglibs.jsp" %>

<c:url var="actionUrl" value="/testFlight.html"/>

<sf:form action="${actionUrl}" method="post" id="testflightForm" name="testflightForm">
    <input type="hidden" id="isCustomSettings" name="isCustomSettings" value="true">
    <input type="hidden" id="buildId" name="buildId" value="${customProfileSettings.buildId}"/>
    <input type="hidden" id="projectId" name="projectId" value="${customProfileSettings.projectId}"/>

    <h3>Enter TestFlight Parameters</h3>

    <div><em>*</em>&nbsp;Required field.</div>
    <div class="fieldlabel" id="profileNameLbl"><em>*</em>&nbsp;Profile Name:</div>
    <div class="fieldvalue"><input type="text" id="id" name="id" value="<c:out value="${customProfileSettings.id}"/>">
    </div>
    <div class="fieldlabel" id="apiTokenLbl"><em>*</em>&nbsp;Test Flight API Token:</div>
    <div class="fieldvalue"><input type="text" id="apiToken" name="apiToken"
                                   value="<c:out value="${customProfileSettings.apiToken}"/>" size="100"></div>
    <div class="fieldlabel" id="teamTokenLbl"><em>*</em>&nbsp;Test Flight Team Token:</div>
    <div class="fieldvalue"><input type="text" id="teamToken" name="teamToken"
                                   value="<c:out value="${customProfileSettings.teamToken}"/>" size="100"></div>
    <div class="fieldlabel" id="distroListLbl">Test Flight Distribution List:</div>
    <div class="fieldvalue"><input type="text" id="distroLists" name="distroLists"
                                   value="<c:out value="${customProfileSettings.distroLists}"/>"></div>


    <div class="fieldlabel" id="notifyDistroListLbl">Notify Test Flight Distribution List:</div>
    <div class="fieldvalue">
        Yes&nbsp;
        <input type="radio" id="notifyDistroListYes" name="notifyDistroList" value="true" <c:if
                test="${customProfileSettings.notifyDistroList eq true}"> checked</c:if>>
        &nbsp;&nbsp;No&nbsp;
        <input type="radio" id="notifyDistroListNo" name="notifyDistroList" value="false" <c:if
                test="${customProfileSettings.notifyDistroList eq false}"> checked</c:if>>
    </div>

    <div class="fieldlabel"><em>*</em>&nbsp;Select an artifact to publish:</div>
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
    function validateForm() {
        var hasErrors = false;

        if (!$F('id')) {
            $('profileNameLbl').className = 'fieldError';
            $('id').className = 'error';
            hasErrors = true;
        } else {
            $('profileNameLbl').className = 'fieldlabel';
            $('id').className = 'fieldValue';
        }

        if (!$F('apiToken')) {
            $('apiTokenLbl').className = 'fieldError';
            $('apiToken').className = 'error';
            hasErrors = true;
        } else {
            $('apiTokenLbl').className = 'fieldlabel';
            $('apiToken').className = 'fieldValue';
        }

        if (!$F('teamToken')) {
            $('teamTokenLbl').className = 'fieldError';
            $('teamToken').className = 'error';
            hasErrors = true;
        } else {
            $('teamTokenLbl').className = 'fieldlabel';
            $('teamToken').className = 'fieldValue';
        }

        if (!$F('distroLists') && $('notifyDistroListYes').checked) {
            $('distroListLbl').className = 'fieldError';
            $('distroLists').className = 'error';
            $('notifyDistroListLbl').className = 'fieldError';
            hasErrors = true;
        } else {
            $('distroListLbl').className = 'fieldlabel';
            $('distroLists').className = 'fieldValue';
            $('notifyDistroListLbl').className = 'fieldlabel';
        }

        if (hasErrors) {
            $('formValidationErrors').update('Enter values for the required fields.');
            $('formValidationErrors').show();
        } else {
            $('testflightForm').submit();
        }
    }
</script>