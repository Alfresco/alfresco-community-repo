<%@ page import="java.io.*,
                 java.util.Enumeration,
                 java.text.DateFormat,
                 java.util.Date,
		 org.alfresco.web.bean.content.*,
		 org.alfresco.web.templating.*,
		 org.w3c.dom.*"%>
<%@ page session="true" %>
<%@ page errorPage="error.jsp" %>
<%
String url = request.getContextPath() + request.getServletPath() + '?' + request.getQueryString() + "&xxx=bla";
%>
<html xmlns:xforms="http://www.w3.org/2002/xforms">
<head>
<script type="text/javascript">
djConfig = { isDebug: true };
var xforms_url = "<%= url %>";
var contextPath = "<%= request.getContextPath() %>";
</script>
<script type="text/javascript" src="<%= request.getContextPath() %>/scripts/ajax/dojo.js">
</script>
<script type="text/javascript" src="<%= request.getContextPath() %>/jsp/content/xforms/xforms.js">
</script>
</head>
<body>
<form>
<div id="alf-ui" style="width: 100%; border: solid 1px orange;">
</div>
</form>
</body>
</html>