<%@ include file="/include.jsp" %>
<%@ include file="taglibs.jsp" %>

<h1>Test Flight Upload Results</h1>
<c:if test="${succeeded}">
    Succeeded
</c:if>
<c:if test="${!succeeded}">
    Failed
    <p>&nbsp;</p>
    <c:out value="${error}"/>
</c:if>