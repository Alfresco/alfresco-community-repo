<%--
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
--%>

<%@ page import="org.springframework.web.context.WebApplicationContext" %>
<%@ page import="org.springframework.web.context.support.WebApplicationContextUtils" %>
<%@ page import="org.alfresco.repo.admin.SysAdminParams" %>
<%@ page import="org.alfresco.service.descriptor.DescriptorService" %>
<%@ page import="org.alfresco.service.transaction.TransactionService" %>
<%@ page import="org.alfresco.util.UrlUtil" %>
<%@ page import="org.alfresco.service.cmr.module.ModuleService" %>
<%@ page import="org.alfresco.service.cmr.module.ModuleDetails" %>
<%@ page import="org.alfresco.service.cmr.module.ModuleInstallState" %>

<!-- Enterprise index-jsp placeholder -->
<%
// route WebDAV requests
if (request.getMethod().equalsIgnoreCase("PROPFIND") || request.getMethod().equalsIgnoreCase("OPTIONS"))
{
   response.sendRedirect(request.getContextPath() + "/webdav/");
}
%>

<%
WebApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(session.getServletContext());
SysAdminParams sysAdminParams = (SysAdminParams)context.getBean("sysAdminParams");
DescriptorService descriptorService = (DescriptorService)context.getBean("descriptorComponent");
TransactionService transactionService = (TransactionService)context.getBean("transactionService");
ModuleService moduleService = (ModuleService) context.getBean("moduleService");
ModuleDetails shareServicesModule = moduleService.getModule("alfresco-share-services");
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
   <title>Alfresco</title>
   <link rel="stylesheet" type="text/css" href="./css/reset.css" />
   <link rel="stylesheet" type="text/css" href="./css/alfresco.css" />
</head>
<body>
   <div class="sticky-wrapper">
      <div class="index">
         
         <div class="title">
            <span class="logo"><a href="http://www.alfresco.com"><img src="./images/logo/logo.png" width="145" height="48" alt="" border="0" /></a></span>
            <span class="logo-separator">&nbsp;</span>
            <h1>Welcome to Alfresco</h1>
         </div>
         
         <div class="index-list">
            <h4><%=descriptorService.getServerDescriptor().getEdition()%>&nbsp;-&nbsp;<%=descriptorService.getServerDescriptor().getVersion()%></h4>
            <p></p>
            <p><a href="http://docs.alfresco.com/">Online Documentation</a></p>
            <p></p>
             <%
                 if (shareServicesModule != null && ModuleInstallState.INSTALLED.equals(shareServicesModule.getInstallState()))
                 {
             %>
                <p><a href="<%=UrlUtil.getShareUrl(sysAdminParams)%>">Alfresco Share</a></p>
                <p></p>
             <%
                }
             %>
            <p><a href="./webdav">Alfresco WebDav</a></p>
            <p></p>
            <p><a href="./s/index">Alfresco WebScripts Home</a> (admin only)</p>
<%
   if (descriptorService.getLicenseDescriptor() == null && transactionService.isReadOnly())
   {
%>
            <p>WARNING: The system is in Read Only mode, the License may have failed to deploy. Please visit the <a href="./s/enterprise/admin">Alfresco Administration Console</a> (admin only)</p>
<% 
   }
   if (descriptorService.getLicenseDescriptor() != null && descriptorService.getLicenseDescriptor().getLicenseMode().toString().equals("ENTERPRISE"))
   {
%>
            <p><a href="./s/enterprise/admin">Alfresco Administration Console</a> (admin only)</p>
            <p></p>
            <p><a href="http://support.alfresco.com">Alfresco Support</a></p>
<%
   }
   else
   {
%>
            <p><a href="./s/admin">Alfresco Administration Console</a> (admin only)</p>
            <p></p>
            <p><a href="http://forums.alfresco.com/">Alfresco Forums</a></p>
            <p><a href="http://issues.alfresco.com/">Alfresco JIRA</a></p>
<%
   }
%>
            <p></p>
            <p><a href="./api/-default-/public/cmis/versions/1.0/atom">CMIS 1.0 AtomPub Service Document</a></p>
            <p><a href="./cmisws/cmis?wsdl">CMIS 1.0 Web Services WSDL Document</a></p>
            <p><a href="./api/-default-/public/cmis/versions/1.1/atom">CMIS 1.1 AtomPub Service Document</a></p>
            <p><a href="./api/-default-/public/cmis/versions/1.1/browser">CMIS 1.1 Browser Binding URL</a></p>
         </div>
         
      </div>
      <div class="push"></div>
   </div>
   <div class="footer">
      Alfresco Software, Inc. &copy; 2005-2016 All rights reserved.
   </div>
</body>
</html>
