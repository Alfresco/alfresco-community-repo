<%--
 Copyright (C) 2005 Alfresco, Inc.

 Licensed under the Alfresco Network License. You may obtain a
 copy of the License at

   http://www.alfrescosoftware.com/legal/

 Please view the license relevant to your network subscription.

 BY CLICKING THE "I UNDERSTAND AND ACCEPT" BOX, OR INSTALLING,  
 READING OR USING ALFRESCO'S Network SOFTWARE (THE "SOFTWARE"),  
 YOU ARE AGREEING ON BEHALF OF THE ENTITY LICENSING THE SOFTWARE    
 ("COMPANY") THAT COMPANY WILL BE BOUND BY AND IS BECOMING A PARTY TO 
 THIS ALFRESCO NETWORK AGREEMENT ("AGREEMENT") AND THAT YOU HAVE THE   
 AUTHORITY TO BIND COMPANY. IF COMPANY DOES NOT AGREE TO ALL OF THE   
 TERMS OF THIS AGREEMENT, DO NOT SELECT THE "I UNDERSTAND AND AGREE"   
 BOX AND DO NOT INSTALL THE SOFTWARE OR VIEW THE SOURCE CODE. COMPANY   
 HAS NOT BECOME A LICENSEE OF, AND IS NOT AUTHORIZED TO USE THE    
 SOFTWARE UNLESS AND UNTIL IT HAS AGREED TO BE BOUND BY THESE LICENSE  
 TERMS. THE "EFFECTIVE DATE" FOR THIS AGREEMENT SHALL BE THE DAY YOU  
 CHECK THE "I UNDERSTAND AND ACCEPT" BOX.
--%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>

<r:page titleId="title_add_user_group">

<f:view>
   
   <%-- load a bundle of properties with I18N strings --%>
   <f:loadBundle basename="alfresco.messages.webclient" var="msg"/>
   
   <h:form acceptCharset="UTF-8" id="add-user-group">
   
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
                  <td bgcolor="#EEEEEE">
                  
                     <%-- Status and Actions inner contents table --%>
                     <%-- Generally this consists of an icon, textual summary and actions for the current object --%>
                     <table cellspacing="4" cellpadding="0" width="100%">
                        <tr>
                           <td width="32">
                              <h:graphicImage id="logo" url="/images/icons/add_user_large.gif" />
                           </td>
                           <td>
                              <div class="mainSubTitle"><h:outputText value="#{GroupsBean.actionGroupName}" /></div>
                              <div class="mainTitle"><h:outputText value="#{msg.add_user}" /></div>
                              <div class="mainSubText"><h:outputText value="#{msg.add_user_group_description}" /></div>
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
               
               <%-- Details --%>
               <tr valign=top>                  
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width="4"></td>
                  <td>
                     <table cellspacing="0" cellpadding="3" border="0" width="100%">
                        <tr>
                           <td width="100%" valign="top">
                              
                              <a:errors message="#{msg.error_wizard}" styleClass="errorMessage" />
                              
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "white", "white"); %>
                              <table cellpadding="2" cellspacing="2" border="0" width="100%">
                                 <tr>
                                    <td class="mainSubTitle"><h:outputText value="#{msg.select_users}" /></td>
                                 </tr>
                                 <tr>
                                    <td><a:genericPicker id="picker" showFilter="false" queryCallback="#{GroupsBean.pickerCallback}"
                                             actionListener="#{GroupsBean.addSelectedUsers}" /></td>
                                 </tr>
                                 <tr><td class="paddingRow"></td></tr>
                                 <tr>
                                    <td class="mainSubTitle"><h:outputText value="#{msg.selected_users}" /></td>
                                 </tr>
                                 <tr>
                                    <td>
                                       <h:dataTable value="#{GroupsBean.usersDataModel}" var="row" 
                                                    rowClasses="selectedItemsRow,selectedItemsRowAlt"
                                                    styleClass="selectedItems" headerClass="selectedItemsHeader"
                                                    cellspacing="0" cellpadding="4" 
                                                    rendered="#{GroupsBean.usersDataModel.rowCount != 0}">
                                          <h:column>
                                             <f:facet name="header">
                                                <h:outputText value="#{msg.name}" />
                                             </f:facet>
                                             <h:outputText value="#{row.name}" />
                                          </h:column>
                                          <h:column>
                                             <a:actionLink actionListener="#{GroupsBean.removeUserSelection}" image="/images/icons/delete.gif"
                                                           value="#{msg.remove}" showLink="false" style="padding-left:6px" />
                                          </h:column>
                                       </h:dataTable>
                                       
                                       <a:panel id="no-items" rendered="#{GroupsBean.usersDataModel.rowCount == 0}">
                                          <table cellspacing='0' cellpadding='2' border='0' class='selectedItems'>
                                             <tr>
                                                <td colspan='2' class='selectedItemsHeader'><h:outputText id="no-items-name" value="#{msg.name}" /></td>
                                             </tr>
                                             <tr>
                                                <td class='selectedItemsRow'><h:outputText id="no-items-msg" value="#{msg.no_selected_items}" /></td>
                                             </tr>
                                          </table>
                                       </a:panel>
                                    </td>
                                 </tr>
                              </table>
                              <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "white"); %>
                           </td>
                           
                           <td valign="top">
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "blue", "#D3E6FE"); %>
                              <table cellpadding="1" cellspacing="1" border="0">
                                 <tr>
                                    <td align="center">
                                       <h:commandButton id="ok-button" value="#{msg.finish_button}" action="#{GroupsBean.finishAddUser}"
                                                        styleClass="wizardButton" />
                                    </td>
                                 </tr>
                                 <tr>
                                    <td align="center">
                                       <h:commandButton value="#{msg.cancel}" action="cancel" styleClass="wizardButton" />
                                    </td>
                                 </tr>
                              </table>
                              <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "blue"); %>
                           </td>
                        </tr>
                     </table>
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