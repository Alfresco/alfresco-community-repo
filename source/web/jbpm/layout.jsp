<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-tiles" prefix="tiles" %>

<%@ page import="org.jbpm.webapp.bean.*" %>

<!-- HEADER -->
<html>
<head><title>JBoss jBPM</title>
<link rel="stylesheet" type="text/css" href="css/jbpm.css" />
</head>

<body>
<table border="0" cellpadding="0" cellspacing="0">
  <tr>
    <!-- next line must be put one (1!) line to overcome an IE-layouting bug -->
    <td><a href="http://www.jboss.com/"><img src="images/logo_green.gif" alt="JBoss Inc." border="0" /></a></td>
    <td width="100%" valign="top">
      <table width="100%" border="0" cellpadding="0" cellspacing="0">
        <tr>
          <td width="100%" height="22" bgcolor="#9BCD4B" align="center" nowrap="nowrap">
          </td>
        </tr>
        <tr>
          <td width="100%" align="right" valign="middle" nowrap="nowrap">
            <a class="ref" href="http://jbpm.org/docs">Docs</a> &nbsp;&nbsp;
            <a class="ref" href="http://jbpm.org/forums">Forums</a> &nbsp;&nbsp;
            <a class="ref" href="http://jbpm.org/wiki">Wiki</a> &nbsp;&nbsp;
            <a class="ref" href="http://jbpm.org/downloads">Download</a> &nbsp;&nbsp;
            <a class="ref" href="http://jbpm.org/contact">Contact</a> &nbsp;&nbsp; &nbsp;&nbsp;
          </td>
        </tr>
        <tr>
          <td width="100%" valign="top" nowrap="nowrap">
            <hr style="margin:0px; padding:0px;" size="1" width="100%" />
            <h2 style="padding:0px; margin:0px; border:0px;" >

<tiles:get name="title" flush="false"/>

<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>

            </h2>
          </td>
        </tr>
      </table>
    </td>
  </tr>
  <tr>
    <td width="100%" height="100%" colspan="2" valign="top">
      <table width="100%" height="100%" border="0" cellpadding="0" cellspacing="0" >
        <tr>
          <td valign="top">
            <table width="100%" height="100%" border="0" cellpadding="0" cellspacing="0" >
              <tr>
                <td width="217" valign="top"><img src="images/hdr_green_side.gif" alt="green side" /></td>
              </tr>

              <!-- next line must be put one (1!) line to overcome an IE-layouting bug -->
              <tr>
              <td width="175"><a  href="home.jsp"><div  class="nav" onmouseover="this.style.cursor='hand'; this.style.backgroundColor='#cccccc'; this.style.color='#000000';" onmouseout="this.style.cursor='default';this.style.backgroundColor='#5c5c4f'; this.style.color='#ffffff';">Home</div></a></td>
              </tr>

              <tr>
              <td width="175"><a  href="admin.jsp"><div  class="nav" onmouseover="this.style.cursor='hand'; this.style.backgroundColor='#cccccc'; this.style.color='#000000';" onmouseout="this.style.cursor='default';this.style.backgroundColor='#5c5c4f'; this.style.color='#ffffff';">Administration</div></a></td>
              </tr>

               <tr>
               <td width="175"><a  href="monitor.jsp"><div  class="nav" onmouseover="this.style.cursor='hand'; this.style.backgroundColor='#cccccc'; this.style.color='#000000';" onmouseout="this.style.cursor='default';this.style.backgroundColor='#5c5c4f'; this.style.color='#ffffff';">Monitoring</div></a></td>
               </tr>

              <tr>
                <td width="217" valign="top"><img style="margin-top:1px;" src="images/side_nav_green_btm.gif" alt="green side" /></td>
              </tr>
              <tr>
                <td height="100%">
                   
                </td>
              </tr>
              <tr>
                <td>
                  <img src="images/swoosh_green.gif" alt="swoosh" />
                </td>
              </tr>
            </table>
          </td>

          <td height="100%" width="100%" valign="top">

<!-- CONTENT -->

<tiles:get name="body" flush="false"/>

<!-- FOOTER -->
          </td>

        </tr>
      </table>
    </td>
  </tr>
</table>
</body>

</html>

