<%--
  Copyright (C) 2005 Alfresco, Inc.
 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
--%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>

<r:page titleId="title_rules">

<f:view>
   
   <%-- load a bundle of properties with I18N strings --%>
   <f:loadBundle basename="alfresco.messages.webclient" var="msg"/>
   
   <h:form acceptCharset="UTF-8" id="document-details">
   
   <%-- Main outer table --%>
   <table cellspacing="0" cellpadding="2">
      
      <%-- Title bar --%>
      <tr>
         <td colspan="2">
            <%@ include file="../parts/titlebar.jsp" %>
         </td>
      </tr>
      
      <%-- Main area --%>
      <tr valign="top">
         <%-- Shelf --%>
         <td>
            <%@ include file="../parts/shelf.jsp" %>
         </td>
         
         <%-- Work Area --%>
         <td width="100%">
            <table cellspacing="0" cellpadding="0" width="100%">
               <%-- Breadcrumb --%>
               <%@ include file="../parts/breadcrumb.jsp" %>
               
               <%-- Status and Actions --%>
               <tr>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/statuspanel_4.gif)" width="4"></td>
                  <td bgcolor="#dfe6ed">
                  
                     <%-- Status and Actions inner contents table --%>
                     <%-- Generally this consists of an icon, textual summary and actions for the current object --%>
                     <table cellspacing="4" cellpadding="0" width="100%">
                        <tr>
                           <td width="32">
                              <h:graphicImage id="wizard-logo" url="/images/icons/rule_large.gif" />
                           </td>
                           <td>
                              <div class="mainTitle"><h:outputText value="#{msg.content_rules}" /></div>
                              <div class="mainSubText"><h:outputText value="#{msg.space_rules_description}" /></div>
                           </td>
                           <td align=right>
                              <%-- Current object actions --%>
                              <a:actionLink value="#{msg.create_rule}" image="/images/icons/new_rule.gif" padding="4" action="wizard:createRule" />
                           </td>                       
                           <td style="padding-left:4px" width=80>
                             <%-- More actions menu --%>
                             <a:menu id="actionsMenu" itemSpacing="4" label="#{msg.more_actions}" image="/images/icons/menu.gif" menuStyleClass="moreActionsMenu" style="white-space:nowrap">
                                <r:actions id="acts_rules" value="rules_actions_menu" context="#{RulesBean.space}" />
                             </a:menu>
                           </td>
                           <td class="separator" width=1><img src="<%=request.getContextPath()%>/images/parts/dotted_separator.gif" border=0 height=29 width=1></td>  
                           <td width="125" style="padding-left:2px">
                              <%-- Filters --%>
                              <a:modeList itemSpacing="3" iconColumnWidth="20" selectedStyleClass="statusListHighlight"
                                    value="#{RulesBean.viewMode}" actionListener="#{RulesBean.viewModeChanged}" menu="true" menuImage="/images/icons/menu.gif" styleClass="moreActionsMenu"
                                    selectedImage="/images/icons/filter.gif">
                                 <a:listItem value="inherited" label="#{msg.inherited}" />
                                 <a:listItem value="local" label="#{msg.local}" />
                              </a:modeList>
                           </td>
                        </tr>
                     </table>
                  </td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/statuspanel_6.gif)" width="4"></td>
               </tr>
               
               <%-- separator row with gradient shadow --%>
               <tr>
                  <td><img src="<%=request.getContextPath()%>/images/parts/statuspanel_7.gif" width="4" height="9"></td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/statuspanel_8.gif)"></td>
                  <td><img src="<%=request.getContextPath()%>/images/parts/statuspanel_9.gif" width="4" height="9"></td>
               </tr>
               
               <%-- infomation panel --%> 
               <tr>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width="4"></td> 
                  <td>
               
	               <h:panelGroup rendered="#{RulesBean.ignoreInheritedRules}">
	               <tr height='5'><td></td></tr>
	               <tr>
	                  <td>               
					   <f:verbatim>
					      <%PanelGenerator.generatePanelStart(out, request.getContextPath(), "yellowInner", "#ffffcc");%>
					      <table><tr><td>
					   </f:verbatim>
					   <h:graphicImage url="/images/icons/info_icon.gif" />
					   <f:verbatim>
					      </td><td>
					   </f:verbatim>
					   <h:outputText value="#{msg.inherited_rules_being_ignored}" />
					   <f:verbatim>
					      </td></tr></table>
					      <%PanelGenerator.generatePanelEnd(out, request.getContextPath(), "yellowInner");%>
					   </f:verbatim>
	   				  </td>
	               </tr>	               
	               <tr height='5'><td></td></tr>
	               </h:panelGroup>
               
                  </td>               
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_6.gif)" width="4"></td>
               </tr>
               
               <%-- Details --%>
               <tr valign=top>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width="4"></td>
                  <td>
                     <table cellspacing="0" cellpadding="3" border="0" width="100%">
                        <tr>
                           <td width="100%" valign="top">
                           
                              <%-- Rules List --%>
                              <a:panel id="rules-panel" border="white" bgcolor="white" titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" styleClass="mainSubTitle" label="#{msg.rules}">
                              
                              <a:richList id="rulesList" viewMode="details" value="#{RulesBean.rules}" var="r"
                                          styleClass="recordSet" headerStyleClass="recordSetHeader" rowStyleClass="recordSetRow" 
                                          altRowStyleClass="recordSetRowAlt" width="100%" pageSize="10"
                                          initialSortColumn="createdDate" initialSortDescending="false"
                                          binding="#{RulesBean.richList}">
                        
                                 <%-- Primary column for details view mode --%>
                                 <a:column primary="true" width="200" style="padding:2px;text-align:left">
                                    <f:facet name="header">
                                       <a:sortLink label="#{msg.title}" value="title" mode="case-insensitive" styleClass="header"/>
                                    </f:facet>
                                    <f:facet name="small-icon">
                                       <h:panelGroup>
                                          <a:booleanEvaluator value="#{r.local}">
                                             <a:actionLink value="#{r.title}" image="/images/icons/rule.gif" 
                                                           actionListener="#{RulesBean.setupRuleAction}" action="wizard:editRule"
                                                           showLink="false">
                                                <f:param name="id" value="#{r.id}" />
                                             </a:actionLink>
                                          </a:booleanEvaluator>
                                          <a:booleanEvaluator value="#{r.local == false}">
                                             <h:graphicImage value="/images/icons/rule.gif" title="#{r.title}" style="vertical-align: middle"/>
                                          </a:booleanEvaluator>
                                       </h:panelGroup>
                                    </f:facet>
                                    <a:booleanEvaluator value="#{r.local}">
                                       <a:actionLink value="#{r.title}" actionListener="#{RulesBean.setupRuleAction}" 
                                                     action="wizard:editRule">
                                          <f:param name="id" value="#{r.id}" />
                                       </a:actionLink>
                                    </a:booleanEvaluator>
                                    <a:booleanEvaluator value="#{r.local == false}">
                                       <h:outputText value="#{r.title}"/>
                                    </a:booleanEvaluator>
                                 </a:column>
                                 
                                 <%-- Description column --%>
                                 <a:column style="text-align:left">
                                    <f:facet name="header">
                                       <a:sortLink label="#{msg.description}" value="description" styleClass="header"/>
                                    </f:facet>
                                    <h:outputText value="#{r.description}" />
                                 </a:column>
                                 
                                 <%-- Column to show whether the rule is local --%>
                                 <a:column style="text-align:left">
                                    <f:facet name="header">
                                       <a:sortLink label="#{msg.local}" value="local" styleClass="header"/>
                                    </f:facet>
                                    <h:outputText value="#{r.local}" >
                                       <a:convertBoolean/>
                                    </h:outputText>
                                 </a:column>
                                 
                                 <%-- Created Date column for details view mode --%>
                                 <a:column style="text-align:left">
                                    <f:facet name="header">
                                       <a:sortLink label="#{msg.created_date}" value="createdDate" styleClass="header"/>
                                    </f:facet>
                                    <h:outputText value="#{r.createdDate}">
                                       <a:convertXMLDate type="both" dateStyle="long" timeStyle="short" />
                                    </h:outputText>
                                 </a:column>
                                 
                                 <%-- Modified Date column for details/icons view modes --%>
                                 <a:column style="text-align:left">
                                    <f:facet name="header">
                                       <a:sortLink label="#{msg.modified_date}" value="modifiedDate" styleClass="header"/>
                                    </f:facet>
                                    <h:outputText value="#{r.modifiedDate}">
                                       <a:convertXMLDate type="both" dateStyle="long" timeStyle="short" />
                                    </h:outputText>
                                 </a:column>
                                 
                                 <%-- Actions column --%>
                                 <a:column actions="true" style="text-align:left">
                                    <f:facet name="header">
                                       <h:outputText value="#{msg.actions}"/>
                                    </f:facet>
                                    <a:booleanEvaluator value="#{r.local}">
                                       <a:actionLink value="#{msg.delete}" image="/images/icons/delete.gif" showLink="false" 
                                                     styleClass="inlineAction"
                                                     actionListener="#{RulesBean.setupRuleAction}" action="deleteRule">
                                          <f:param name="id" value="#{r.id}" />
                                       </a:actionLink>
                                       <a:actionLink value="#{msg.change_details}" image="/images/icons/change_rule.gif" 
                                                     showLink="false" styleClass="inlineAction"
                                                     actionListener="#{RulesBean.setupRuleAction}" action="wizard:editRule">
                                          <f:param name="id" value="#{r.id}" />
                                       </a:actionLink>
                                    </a:booleanEvaluator>
                                 </a:column>
                                 
                                 <a:dataPager styleClass="pager" />
                              </a:richList>
                              <h:message for="rulesList" styleClass="statusMessage" />
                              </a:panel>
                           </td>
                           
                           <td valign="top">
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "greyround", "#F5F5F5"); %>
                              <table cellpadding="1" cellspacing="1" border="0">
                                 <tr>
                                    <td align="center">
                                       <h:commandButton value="#{msg.close}" action="dialog:close" styleClass="wizardButton" />
                                    </td>
                                 </tr>
                              </table>
                              <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "greyround"); %>
                           </td>
                        </tr>
                     </table>
                  </td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_6.gif)" width="4"></td>
               </tr>
               
               <%-- Error Messages --%>
               <tr valign="top">
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width="4"></td>
                  <td>
                     <%-- messages tag to show messages not handled by other specific message tags --%>
                     <h:messages globalOnly="true" styleClass="errorMessage" layout="table" />
                  </td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_6.gif)" width="4"></td>
               </tr>
               
               <%-- separator row with bottom panel graphics --%>
               <tr>
                  <td><img src="<%=request.getContextPath()%>/images/parts/whitepanel_7.gif" width="4" height="4"></td>
                  <td width="100%" align="center" style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_8.gif)"></td>
                  <td><img src="<%=request.getContextPath()%>/images/parts/whitepanel_9.gif" width="4" height="4"></td>
               </tr>
               
            </table>
          </td>
       </tr>
    </table>
    
    </h:form>
    
</f:view>

</r:page>