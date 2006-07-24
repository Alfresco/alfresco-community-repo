<%--
  Copyright (C) 2006 Alfresco, Inc.
 
  Licensed under the Mozilla Public License version 1.1 
  with a permitted attribution clause. You may obtain a
  copy of the License at
 
    http://www.alfresco.org/legal/license.txt
 
  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  either express or implied. See the License for the specific
  language governing permissions and limitations under the
  License.
--%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>

<f:verbatim>
<% PanelGenerator.generatePanelStart(out, request.getContextPath(), "yellow", "#ffffcc"); %>
</f:verbatim>
<h:panelGrid columns="3" cellpadding="2" cellspacing="2" border="0" width="100%" style="background-colour:##ffffcc">
   <h:outputText style="font-size: 11px; font-weight:bold; color:#4272B4" value="Alfresco" />
   <h:outputText style="font-size: 11px; font-weight:bold; color:#4272B4" value="Common Tasks" />
   <h:outputText style="font-size: 11px; font-weight:bold; color:#4272B4" value="Working with Alfresco" />
   
   <h:panelGrid columns="2" cellpadding="2" cellspacing="2" border="0">
      <h:graphicImage value="/images/logo/AlfrescoLogo32.gif" width="32" height="32" />
      <h:panelGroup>
         <h:outputText style="font-weight:bold" value="Feature Tour" />
         <f:verbatim><br></f:verbatim>
         <h:outputText value="Lorem ipsum dolor sit amet, consectetuer adipiscing elit." />
      </h:panelGroup>
   </h:panelGrid>
   
   <h:panelGrid columns="2" cellpadding="2" cellspacing="2" border="0">
      <h:graphicImage value="/images/icons/Details.gif" width="32" height="32" />
      <h:panelGroup>
         <h:outputText style="font-weight:bold" value="Browse items in your home space" />
         <f:verbatim><br></f:verbatim>
         <h:outputText value="Lorem ipsum dolor sit amet, consectetuer adipiscing elit." />
      </h:panelGroup>
   </h:panelGrid>
   
   <h:panelGrid columns="1" cellpadding="2" cellspacing="2" border="0">
      <h:outputText style="font-weight:bold" value="Inviting users to a space" />
      <h:outputText value="Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Lorem ipsum dolor sit amet, consectetuer adipiscing elit." />
   </h:panelGrid>
</h:panelGrid>
<f:verbatim>
<% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "yellow"); %>
</f:verbatim>
