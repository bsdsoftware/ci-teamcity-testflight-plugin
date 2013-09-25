<%@ include file="/include.jsp" %>
<%@ include file="taglibs.jsp" %>

<c:url var="actionUrl" value="/testFlight.html"/>
<sf:form action="${actionUrl}" method="post" name="testflightForm" id="testflightForm">
               <h1>Wuhuu TestFlight form from groovy</h1>
    <p>
    build_config_name = (<c:out value="${build_config_name}"/>)
    </p>
</sf:form>