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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>

<h:panelGrid columns="1" cellpadding="3" cellspacing="0" border="0" width="100%">
   <h:outputText value="#{msg.select_layout}" />
   
   <a:imagePickerRadioPanel id="layout-type" columns="4" spacing="4" value="#{WizardManager.bean.layout}"
                       onclick="javascript:itemSelected(this);" panelBorder="lbgrey" panelBgcolor="white">
      <a:listItems value="#{WizardManager.bean.layoutIcons}" />
   </a:imagePickerRadioPanel>
   
   <h:panelGroup>
      <f:verbatim>
      <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "white", "white"); %>
      <table border='0'>
         <tr>
            <td valign='top'>
               </f:verbatim>
               <h:graphicImage id="info-icon" url="/images/icons/info_icon.gif" />
               <f:verbatim>
            </td>
            <td valign='top' align='left'>
               </f:verbatim>
               <a:dynamicDescription selected="#{WizardManager.bean.layout}">
                  <a:descriptions value="#{WizardManager.bean.layoutDescriptions}" />
               </a:dynamicDescription>
               <f:verbatim>
            </td>
         </tr>
      </table>
      <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "white"); %>
      </f:verbatim>
   </h:panelGroup>
   
</h:panelGrid>
