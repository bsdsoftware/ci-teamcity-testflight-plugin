<%@ include file="/include.jsp" %>
<%@ include file="taglibs.jsp" %>

<style>
    .fieldlabel {
        font-weight: bold;
    }

    .fieldvalue {
        margin-bottom: 5px;
    }

    hr {
        margin-top: 5px;
    }
</style>

<div style="border: 1px solid blue; background-color: #add8e6;">

    <h3>Push an artifact to TestFlight</h3>

    <c:if test="${fn:length(errors) gt 0}">
        <div id="errors" style="color: #ff0000; border: 1px solid #ff0000; background-color: #ffb6c1;">
            <p>
                <c:forEach items="${errors}" var="err">
                    <c:out value="${err}"/><br/>
                </c:forEach>
            </p>
        </div>
    </c:if>

    <c:url var="actionUrl" value="/testFlight.html"/>

    <hr/>
    <c:forEach items="${testflightOptions}" var="testflight">
        <sf:form action="${actionUrl}" method="post">
            <input type="hidden" id="internalBuildID" name="internalBuildID" value="${internalBuildID}"/>
            <input type="hidden" id="buildID" name="buildID" value="${testflight.buildId}"/>
            <input type="hidden" id="id" name="id" value="${testflight.id}"/>
            <input type="hidden" id="apiToken" name="apiToken" value="${testflight.apiToken}"/>
            <input type="hidden" id="teamToken" name="teamToken" value="${testflight.teamToken}"/>
            <input type="hidden" id="distroList" name="distroList" value="${testflight.distroList}"/>

            <div class="fieldlabel">Maven Profile ID:</div>
            <div class="fieldvalue"><c:out value="${testflight.id}"/></div>
            <div class="fieldlabel">Test Flight API Token:</div>
            <div class="fieldvalue"><c:out value="${testflight.apiToken}"/></div>
            <div class="fieldlabel">Test Flight Team Token:</div>
            <div class="fieldvalue"><c:out value="${testflight.teamToken}"/></div>
            <div class="fieldlabel">Test Flight Distribution List:</div>
            <div class="fieldvalue"><c:out value="${testflight.distroList}"/></div>
            <div>
                <input type="submit" value="Submit">
            </div>
        </sf:form>
        <hr/>
    </c:forEach>
</div>