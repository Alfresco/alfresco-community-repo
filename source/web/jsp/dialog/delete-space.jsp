<%--
  Copyright (C) 2005 Alfresco, Inc.
 
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

<%@ page buffer="8kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>

<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>

<h:panelGroup rendered="#{BrowseBean.deleteMessage != null}">
   <f:verbatim>
   <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "yellowInner", "#ffffcc"); %>
   <table cellpadding="0" cellspacing="0" border="0" width="100%">
      <tr>
         <td valign=top style="padding-top:2px" width=20>
            </f:verbatim><h:graphicImage url="/images/icons/info_icon.gif" width="16" height="16"/><f:verbatim>
         </td>
         <td class="mainSubText">
            </f:verbatim><h:outputText value="#{BrowseBean.deleteMessage}" /><f:verbatim>
         </td>
      </tr>
   </table>
   <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "yellowInner"); %>
   <br/>
   </f:verbatim>
</h:panelGroup>

<h:outputText value="#{msg.select_delete_operation}" styleClass="mainSubTitle" />
<h:panelGrid columns="1" cellpadding="2" cellspacing="2" border="0">
   <h:selectOneRadio id="delete-operation" layout="pageDirection" value="#{DialogManager.bean.deleteMode}">
      <f:selectItem itemValue="all" itemLabel="#{msg.delete_op_all}" />
      <f:selectItem itemValue="files" itemLabel="#{msg.delete_op_files}" />
      <f:selectItem itemValue="folders" itemLabel="#{msg.delete_op_folders}" />
      <f:selectItem itemValue="contents" itemLabel="#{msg.delete_op_contents}" />
   </h:selectOneRadio>
</h:panelGrid>
