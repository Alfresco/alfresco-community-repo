<%@ page import="java.io.*,
                 java.util.Enumeration,
                 java.text.DateFormat,
                 java.util.Date,
		 org.alfresco.web.bean.content.*,
		 org.alfresco.web.bean.*"%>
<%@ page session="true" %>
<%@ page errorPage="error.jsp" %>
<%
CreateContentWizard wiz = (CreateContentWizard)session.getAttribute("CreateContentWizard");
CheckinCheckoutBean wiz2 = (CheckinCheckoutBean)session.getAttribute("CheckinCheckoutBean");
char[] readerBuffer = new char[request.getContentLength()];
BufferedReader bufferedReader = request.getReader();
StringBuffer sb = new StringBuffer();
do
{
    String s = bufferedReader.readLine();
    if (s == null)
        break;
    sb.append(s).append('\n');
}
while (true);
String xml = sb.toString();
wiz.setContent(xml);
if (wiz2 != null)
{
	System.out.println("saving " + xml + " to checkincheckout");
   wiz2.setEditorOutput(xml);
}
xml = xml.replaceAll("<", "&lt;");
%>
<html>
    <head>
        <title>Instance Data submitted</title>
        <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/forms/styles/chiba-styles.css"/>
     </head>
    <body>

        <center>
            <font face="sans-serif">XML submitted successfully!  you rock!  Click next!</font>
        </center>
<center><tt>
<%= xml %>
<tt></center>
    </body>
</html>
