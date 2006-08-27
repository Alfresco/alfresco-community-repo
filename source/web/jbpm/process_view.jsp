<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/jbpm.tld" prefix="jbpm" %>

<%@ page isELIgnored="false" %>

<% System.out.println(request.getParameter("taskInstanceId")); %>

<c:out value="${taskInstanceId}">
     default value 
</c:out>

Setting the value: "Hello World!"
   <c:set var="hello" value="Hello World!"/>
   <p/>
<c:out value="${hello}"/>
   
<%-- <jsp:include page="header1.jsp" /> --%>

Process View
<%-- <jsp:include page="header2.jsp" /> --%>

<jbpm:processimage task="${param[\"taskInstanceId\"]}"/>

<%-- <jsp:include page="footer.jsp" /> --%>
