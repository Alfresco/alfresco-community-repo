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
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>

<h:panelGrid id="grid-1" columns="1" cellpadding="2" cellpadding="2" width="100%">
   <%-- Template selection list --%>
   <h:outputText id="msg-select" styleClass="mainSubText" value="#{msg.website_select_templates}:" />
   <h:panelGroup id="grp-0">
      <a:selectList id="template-list" activeSelect="true" styleClass="selectListTable" itemStyleClass="selectListItem">
         <a:listItems id="items1" value="#{DialogManager.bean.templatesList}" />
         <h:commandButton id="cmd-add" value="#{msg.add_to_list_button}" styleClass="dialogControls" actionListener="#{DialogManager.bean.addTemplate}" />
      </a:selectList>
   </h:panelGroup>
   
   <f:verbatim><div style='padding:4px'></div></f:verbatim>
   <h:outputText id="msg-selected" styleClass="mainSubText" value="#{msg.website_selected_templates}:" />
   
   <h:panelGroup id="grp-1" rendered="#{DialogManager.bean.templatesDataModel.rowCount != 0}">
      <h:panelGrid id="grid-2" columns="2" cellspacing="2" width="100%">
         <h:dataTable id="templates" value="#{DialogManager.bean.templatesDataModel}" var="row" 
                      rowClasses="selectedItemsRow,selectedItemsRowAlt"
                      styleClass="selectedItems" headerClass="selectedItemsHeader"
                      cellspacing="0" cellpadding="4" width="100%">
            <h:column id="col1">
               <f:facet name="header">
                  <h:outputText id="head-1" value="#{msg.name}" />
               </f:facet>
               <f:verbatim>
                  <img style="float:left" src="<%=request.getContextPath()%>/images/icons/template_large.gif" />
               </f:verbatim>
            </h:column>
            <h:column id="col2">
               <h:outputText id="msg-01" value="#{row.title}" />
            </h:column>
            <h:column>
               <f:facet name="header">
                  <h:outputText id="head-2" value="#{msg.output_path_pattern}" />
               </f:facet>
               <h:inputText id="in-01" value="#{row.outputPathPattern}" size="70" maxlength="1024" />
            </h:column>
            <h:column>
               <a:actionLink id="act-01" actionListener="#{DialogManager.bean.removeTemplate}" image="/images/icons/delete.gif"
                             value="#{msg.remove}" showLink="false" style="padding:4px" />
            </h:column>
         </h:dataTable>
         <h:graphicImage id="img-help" value="/images/icons/Help_icon.gif" style="vertical-align:-20%;padding-left:8px;cursor:help" onclick="javascript:toggleOutputPathPatternHelp()" />
      </h:panelGrid>
      <f:verbatim>
         <jsp:directive.include file="/jsp/wcm/output-path-pattern-help.jsp"/>
      </f:verbatim>
   </h:panelGroup>
   
   <a:panel id="no-items" rendered="#{DialogManager.bean.templatesDataModel.rowCount == 0}">
      <h:panelGrid id="grid-none" width="100%" columns="1" cellpadding="2"
            styleClass="selectedItems" rowClasses="selectedItemsHeader,selectedItemsRow">
         <h:outputText id="no-items-name" value="#{msg.name}" />
         <h:outputText styleClass="selectedItemsRow" id="no-items-msg" value="#{msg.no_selected_items}" />
      </h:panelGrid>
   </a:panel>
   
</h:panelGrid>
