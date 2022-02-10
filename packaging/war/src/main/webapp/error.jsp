<%--
  #%L
  Alfresco Repository WAR Community
  %%
  Copyright (C) 2005 - 2021 Alfresco Software Limited
  %%
  This file is part of the Alfresco software.
  If the software was purchased under a paid Alfresco license, the terms of
  the paid license agreement will prevail.  Otherwise, the software is
  provided under the following open source license terms:

  Alfresco is free software: you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  Alfresco is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
  #L%
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
<%@ page import="java.util.Calendar" %>

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
   <link rel="stylesheet" type="text/css" href="/<%=sysAdminParams.getAlfrescoContext()%>/css/reset.css" />
   <link rel="stylesheet" type="text/css" href="/<%=sysAdminParams.getAlfrescoContext()%>/css/alfresco.css" />
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
            <p id ="errorMessage">An error was encountered, please contact your system administrator.</p>
         </div>

      </div>
      <div class="push"></div>
   </div>
   <div class="footer">
       Alfresco Software, Inc. &copy; 2005-<%=Calendar.getInstance().get(Calendar.YEAR)%> All rights reserved.
   </div>
</body>
</html>
