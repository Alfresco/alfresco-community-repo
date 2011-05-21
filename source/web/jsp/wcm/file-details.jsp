<%--
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a"%>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r"%>

<%@ page import="org.alfresco.web.ui.common.PanelGenerator"%>

<f:verbatim>
<table cellspacing="0" cellpadding="3" border="0" width="100%">
   <tr>
      <td width="100%" valign="top"></f:verbatim>
      <a:panel label="#{msg.view_links}" id="links-panel" progressive="true" border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" expanded='#{DialogManager.bean.panels["links-panel"]}'
         expandedActionListener="#{DialogManager.bean.expandPanel}"><f:verbatim>
         <table width="100%" cellspacing="2" cellpadding="2" border="0" align="center">
            <tr>
               <td></f:verbatim><a:actionLink value="#{msg.view_in_browser}" href="#{DialogManager.bean.browserUrl}" target="new" id="link1" /><f:verbatim></td>
               <td></f:verbatim><a:actionLink value="#{msg.download_content}" href="#{DialogManager.bean.downloadUrl}" target="new" id="link2" /><f:verbatim></td>
               <td></f:verbatim><a:actionLink value="#{msg.file_preview}" href="#{DialogManager.bean.previewUrl}" target="new" id="link3" /><f:verbatim></td>
               <td><a id="link4" href='</f:verbatim><a:outputText value="#{DialogManager.bean.nodeRefUrl}" id="out3" /><f:verbatim>' onclick="return false;"></f:verbatim><a:outputText value="#{msg.noderef_link}" id="out4" /><f:verbatim></a></td>
            </tr>
         </table></f:verbatim>
      </a:panel>

      <f:verbatim><div style="padding: 4px"></div></f:verbatim>

      <h:panelGroup id="props-panel-facets">
         <f:facet name="title">
            <r:permissionEvaluator id="eval1" value="#{DialogManager.bean.document}" allow="Write">
               <r:actionInstanceEvaluator id="acEv1" value="#{DialogManager.bean.avmNode}" evaluatorClassName="org.alfresco.web.action.evaluator.WCMWorkflowEvaluator">
                  <a:actionLink id="titleLink1" value="#{msg.modify}" showLink="false" image="/images/icons/Change_details.gif" action="dialog:editAvmFileProperties" />
               </r:actionInstanceEvaluator>
            </r:permissionEvaluator>
         </f:facet>
      </h:panelGroup> 
      
      <a:panel label="#{msg.properties}" id="properties-panel" facetsId="dialog:dialog-body:props-panel-facets" progressive="true" border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" expanded='#{DialogManager.bean.panels["properties-panel"]}'
         expandedActionListener="#{DialogManager.bean.expandPanel}"><f:verbatim>
         <table cellspacing="0" cellpadding="0" border="0" width="100%">
            <tr>
               <td width=80 align=center>
               
               <%-- icon image for the doc --%>
               <table cellspacing=0 cellpadding=0 border=0>
                  <tr>
                     <td>
                     <div style="border: thin solid #CCCCCC; padding: 4px"></f:verbatim>
                        <a:actionLink id="doc-logo1" value="#{DialogManager.bean.name}" href="#{DialogManager.bean.browserUrl}" target="new" image="#{DialogManager.bean.fileType32}" showLink="false" /><f:verbatim>
                     </div>
                     </td>
                     <td><img src="<%=request.getContextPath()%>/images/parts/rightSideShadow42.gif" width=6 height=42></td>
                  </tr>
                  <tr>
                     <td colspan=2><img src="<%=request.getContextPath()%>/images/parts/bottomShadow42.gif" width=48 height=5></td>
                  </tr>
               </table>
               </td>
               <td></f:verbatim>
                  <%-- properties for the doc --%> 
                  <r:propertySheetGrid id="document-props" value="#{DialogManager.bean.document}" var="documentProps" columns="1" mode="view" labelStyleClass="propertiesLabel" externalConfig="true" /><f:verbatim>
               </td>
            </tr>
         </table></f:verbatim>
      </a:panel>

      <f:verbatim><div style="padding: 4px"></div></f:verbatim>

      <a:panel label="#{msg.version_history}" id="version-history-panel" progressive="true" border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" expanded='#{DialogManager.bean.panels["version-history-panel"]}'
         expandedActionListener="#{DialogManager.bean.expandPanel}">

         <a:richList id="version-history-list" viewMode="details" value="#{DialogManager.bean.versionHistory}" var="r" styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" altRowStyleClass="recordSetRowAlt" width="100%" pageSize="10" initialSortColumn="version"
            initialSortDescending="true">

            <%-- Primary column --%>
            <a:column id="col1" primary="true" width="100" style="padding:2px;text-align:left">
               <f:facet name="header">
                  <a:sortLink label="#{msg.version}" value="version" styleClass="header" />
               </f:facet>
               <a:actionLink id="label" value="#{r.version}" href="#{r.url}" target="new" />
            </a:column>

            <%-- Modified Date column --%>
            <a:column id="col2" style="text-align:left; white-space:nowrap">
               <f:facet name="header">
                  <a:sortLink label="#{msg.modified_date}" value="modifiedDate" styleClass="header" />
               </f:facet>
               <h:outputText id="date" value="#{r.modifiedDate}">
                  <a:convertXMLDate type="both" pattern="#{msg.date_time_pattern}" />
               </h:outputText>
            </a:column>

            <%-- actions --%>
            <a:column id="col3" style="text-align: left">
               <f:facet name="header">
                  <h:outputText id="outT3" value="#{msg.actions}" />
               </f:facet>
               <a:actionLink id="view-link" value="#{msg.view}" href="#{r.url}" target="new" image="#{r.fileType16}" style="padding-right:8px" />
               
               <r:permissionEvaluator id="eval2" value="#{DialogManager.bean.avmNode}" allow="Write">
                  <r:actionInstanceEvaluator id="acEv2" value="#{DialogManager.bean.avmNode}" evaluatorClassName="org.alfresco.web.action.evaluator.WCMWorkflowEvaluator">
                     <a:actionLink id="revert-link" value="#{msg.revert}" actionListener="#{DialogManager.bean.revertNode}" action="dialog:close" image="/images/icons/revert.gif">
                        <f:param name="version" value="#{r.strVersion}" />
                     </a:actionLink>
                  </r:actionInstanceEvaluator>
               </r:permissionEvaluator>
               
            </a:column>

            <a:dataPager id="pager" styleClass="pager" />
         </a:richList>
      </a:panel><f:verbatim>
      </td>

      <td valign="top"></f:verbatim>

      <%-- Document Actions --%> 
      <a:panel label="#{msg.actions}" id="actions-panel" border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" style="text-align:center" progressive="true" expanded='#{DialogManager.bean.panels["actions-panel"]}'
         expandedActionListener="#{DialogManager.bean.expandPanel}">
         <r:actions id="actions_doc" value="avm_file_details" context="#{FileDetailsBean.avmNode}" verticalSpacing="3" style="white-space:nowrap" />
      </a:panel><f:verbatim>
      </td>
   </tr>
</table></f:verbatim>