<%@ include file="/include.jsp" %>
<%@ include file="taglibs.jsp" %>

<style>
    .fieldlabel {
        font-weight: bold;
    }

    .fieldvalue {
        margin-bottom: 5px;
    }

    .errors {
        color: #ff0000;
        border: 1px solid #ff0000;
        background-color: #ffb6c1;
        text-align: center;
    }

    .success{
        background-color: #90EE90;
        border: 1px solid #006400;
        color: #006400;
        text-align: center;
    }

    .testflightTab{
        padding: 1em;
        background-color: #ADD8E6;
        border: 1px solid #00008B;
    }

    hr {
        margin-top: 5px;
    }
</style>

<div class="testflightTab">

    <h2>Push an artifact to TestFlight</h2>

    <c:if test="${fn:length(errors) gt 0}">
        <div class="errors">
                <c:forEach items="${errors}" var="err">
                    <c:out value="${err}"/><br/>
                </c:forEach>
        </div>
    </c:if>
    <c:if test="${not empty testflightUploadSucceeded}">
        <div class="success">
             <c:out value="${testflightUploadSucceeded}"/>
        </div>
    </c:if>
    <hr/>
    <%--There are no Maven pom testflight profiles.  Show a form that allows the user to enter testflight params.--%>
    <c:if test="${!hasPomSettings}">
        <jsp:include page="customTestflightSettingsForm.jsp"/>
    </c:if>

    <%--Show a form for each test flight profile configured in a Maven pom.--%>
    <c:if test="${hasPomSettings}">
        <jsp:include page="testflightOptions.jsp"/>
    </c:if>
</div>