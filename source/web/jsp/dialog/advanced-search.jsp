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

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>

<r:page titleId="title_advanced_search">

<script language="JavaScript1.2">

   window.onload = pageLoaded;
   
   function pageLoaded()
   {
      document.getElementById("advsearch:search-text").focus();
   }
   
</script>

<f:view>
   
   <%-- load a bundle of properties with I18N strings --%>
   <f:loadBundle basename="alfresco.messages.webclient" var="msg"/>
   
   <%-- set the form name here --%>
   <h:form acceptCharset="UTF-8" id="advsearch">
   
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
                        <tr valign="top">
                           <td width=32>
                              <img src="<%=request.getContextPath()%>/images/icons/search_large.gif" width=32 height=32>
                           </td>
                           <td>
                              <div class="mainSubTitle"><h:outputText value='#{NavigationBean.nodeProperties.name}' /></div>
                              <div class="mainTitle"><h:outputText value="#{msg.advanced_search}" /></div>
                              <div class="mainSubText"><h:outputText value="#{msg.advancedsearch_description}" /></div>
                           </td>
                           <td bgcolor="#465F7D" width=1></td>
                           <td width=80 style="padding-left:2px">
                              <%-- Current object actions --%>
                              <h:outputText style="padding-left:20px" styleClass="mainSubTitle" value="#{msg.actions}" /><br>
                              <a:actionLink value="#{msg.resetall}" image="/images/icons/delete.gif" padding="4" actionListener="#{AdvancedSearchBean.reset}" />
                           </td>
                           <td bgcolor="#465F7D" width=1></td>
                           <td width=100 style="padding-left:2px">
                              <%-- Available Saved Searches --%>
                              <h:outputText style="padding-left:20px" styleClass="mainSubTitle" value="#{msg.saved_searches}" />
                              <div style="padding-top:4px;white-space:nowrap">
                                 <%-- Saved Searches drop-down selector --%>
                                 <%-- uses a nasty hack to execute an ActionListener for the drop-down
                                      tried using a valueChangedListener+formsubmit but the valueChangedListener
                                      is called too late in the lifecycle for the form controls to be managed --%>
                                 <h:selectOneMenu id="searches" value="#{AdvancedSearchBean.savedSearch}" onchange="document.forms['advsearch']['advsearch:act'].value='advsearch:show-search'; document.forms['advsearch'].submit(); return true;">
                                    <f:selectItems value="#{AdvancedSearchBean.savedSearches}" />
                                 </h:selectOneMenu>
                                 <div style="display:none"><a:actionLink id="show-search" value="Select" actionListener="#{AdvancedSearchBean.selectSearch}" /></div>
                              </div>
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
                              
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "white", "white"); %>
                              <table cellpadding="2" cellspacing="2" border="0" valign="top">
                                 
                                 <tr>
                                    <td colspan=2>
                                       <h:outputText value="#{msg.look_for}" style="font-weight:bold" />:&nbsp;
                                       <h:inputText id="search-text" value="#{AdvancedSearchBean.text}" size="48" maxlength="1024" />
                                    </td>
                                 </tr>
                                 
                                 <tr>
                                    <td valign="top">
                                       <a:panel label="#{msg.show_results_for}" id="restrict-panel" style="font-weight:bold" border="white" bgcolor="white" progressive="true"
                                             expanded='#{AdvancedSearchBean.panels["restrict-panel"]}' expandedActionListener="#{AdvancedSearchBean.expandPanel}">
                                          <table cellpadding="2" cellspacing="2" border="0">
                                             <tr>
                                                <td>
                                                   <h:selectOneRadio value="#{AdvancedSearchBean.mode}" layout="pageDirection" id="radioMode" >
                                                      <f:selectItem itemValue="all" itemLabel="#{msg.all_items}" id="selectAll" />
                                                      <f:selectItem itemValue="files_text" itemLabel="#{msg.file_names_contents}" id="selectFilesText" />
                                                      <f:selectItem itemValue="files" itemLabel="#{msg.file_names}" id="selectFiles" />
                                                      <f:selectItem itemValue="folders" itemLabel="#{msg.space_names}" id="selectFolders" />
                                                   </h:selectOneRadio>
                                                </td>
                                             </tr>
                                          </table>
                                       </a:panel>
                                       
                                       <div style="padding:3px"></div>
                                       
                                       <a:panel label="#{msg.look_in}" id="location-panel" style="font-weight:bold" border="white" bgcolor="white" progressive="true"
                                             expanded='#{AdvancedSearchBean.panels["location-panel"]}' expandedActionListener="#{AdvancedSearchBean.expandPanel}">
                                          <table cellpadding="2" cellspacing="2" border="0">
                                             <tr>
                                                <td>
                                                   <h:selectOneRadio value="#{AdvancedSearchBean.lookin}" layout="pageDirection" id="radioLookin">
                                                      <f:selectItem itemValue="all" itemLabel="#{msg.all_spaces}" id="spacesAll" />
                                                      <f:selectItem itemValue="other" itemLabel="#{msg.specify_space}:" id="spacesOther" />
                                                   </h:selectOneRadio>
                                                </td>
                                             </tr>
                                             <tr>
                                                <td style="padding-left:26px">
                                                   <r:spaceSelector label="#{msg.select_space_prompt}" value="#{AdvancedSearchBean.location}" initialSelection="#{NavigationBean.currentNodeId}" styleClass="selector" id="spaceSelector" />
                                                </td>
                                             </tr>
                                             <tr>
                                                <td style="padding-left:22px">
                                                   <h:selectBooleanCheckbox value="#{AdvancedSearchBean.locationChildren}" id="chkLocation" />
                                                   <span style="vertical-align:20%"><h:outputText value="#{msg.include_child_spaces}" id="incchild" /></span>
                                                </td>
                                             </tr>
                                          </table>
                                       </a:panel>
                                       
                                       <div style="padding:3px"></div>
                                       
                                       <a:panel label="#{msg.show_results_categories}" id="categories-panel" style="font-weight:bold" border="white" bgcolor="white" progressive="true"
                                             expanded='#{AdvancedSearchBean.panels["categories-panel"]}' expandedActionListener="#{AdvancedSearchBean.expandPanel}">
                                          <table cellpadding="2" cellspacing="2" border="0">
                                             <tr>
                                                <td style="padding-left:8px;padding-top:8px">
                                                   <r:categorySelector id="catSelector" label="#{msg.select_category_prompt}" styleClass="selector" />
                                                </td>
                                             </tr>
                                             <tr>
                                                <td style="padding-left:4px">
                                                   <h:selectBooleanCheckbox id="chkCatChildren" />
                                                   <span style="vertical-align:20%"><h:outputText value="#{msg.include_sub_categories}" id="incCats" /></span>
                                                </td>
                                             </tr>
                                             <tr>
                                                <td style="padding-left:4px">
                                                   <h:commandButton id="btnAddCat" value="#{msg.add_to_list_button}" actionListener="#{AdvancedSearchBean.addCategory}" styleClass="wizardButton" />
                                                </td>
                                             </tr>
                                             <tr>
                                                <td style="padding-left:4px">
                                                   <h:dataTable value="#{AdvancedSearchBean.categoriesDataModel}" var="row" id="catTable"
                                                                rowClasses="selectedItemsRow,selectedItemsRowAlt"
                                                                styleClass="selectedItems" headerClass="selectedItemsHeader"
                                                                cellspacing="0" cellpadding="4" 
                                                                rendered="#{AdvancedSearchBean.categoriesDataModel.rowCount != 0}">
                                                      <h:column id="col1">
                                                         <f:facet name="header">
                                                            <h:outputText value="#{msg.category}" id="tblCatNameHead" />
                                                         </f:facet>
                                                         <h:outputText value="#{row.name}" id="tblCatName" />
                                                      </h:column>
                                                      <h:column id="col2">
                                                         <f:facet name="header">
                                                            <h:outputText value="#{msg.include_sub_categories}" id="tblCatIncHead" />
                                                         </f:facet>
                                                         <h:outputText value="#{row.includeChildren}" id="tblCatInc" >
                                                            <a:convertBoolean/>
                                                         </h:outputText>
                                                      </h:column>
                                                      <h:column id="col3">
                                                         <a:actionLink actionListener="#{AdvancedSearchBean.removeCategory}" image="/images/icons/delete.gif"
                                                                       value="#{msg.remove}" showLink="false" style="padding-left:6px" id="tblCatAdd" />
                                                      </h:column>
                                                   </h:dataTable>
                                                   
                                                   <a:panel id="no-items" rendered="#{AdvancedSearchBean.categoriesDataModel.rowCount == 0}">
                                                      <table cellspacing='0' cellpadding='2' border='0' class='selectedItems'>
                                                         <tr>
                                                            <td colspan='2' class='selectedItemsHeader'><h:outputText id="no-items-category" value="#{msg.category}" /></td>
                                                         </tr>
                                                         <tr>
                                                            <td class='selectedItemsRow'><h:outputText id="no-items-msg" value="#{msg.no_selected_items}" /></td>
                                                         </tr>
                                                      </table>
                                                   </a:panel>
                                                </td>
                                             </tr>
                                          </table>
                                       </a:panel>
                                       
                                    </td>
                                    
                                    <td valign="top">                                   
                                       
                                       <a:panel label="#{msg.also_search_results}" id="attrs-panel" style="font-weight:bold" border="white" bgcolor="white" progressive="true"
                                             expanded='#{AdvancedSearchBean.panels["attrs-panel"]}' expandedActionListener="#{AdvancedSearchBean.expandPanel}">
                                          <table cellpadding="2" cellspacing="2" border="0">
                                             <tr>
                                                <td>
                                                   <table cellpadding="2" cellspacing="2" border="0">
                                                      <tr>
                                                         <td style="padding-left:8px"><h:outputText value="#{msg.content_type}" id="contentType" />:</td>
                                                         <td>
                                                            <h:selectOneMenu value="#{AdvancedSearchBean.contentType}" id="selectContentType">
                                                               <f:selectItems value="#{AdvancedSearchBean.contentTypes}" id="contentTypes" />
                                                            </h:selectOneMenu>
                                                         </td>
                                                      </tr>
                                                      <tr>
                                                         <td style="padding-left:8px"><h:outputText value="#{msg.content_format}" id="contentFormat" />:</td>
                                                         <td>
                                                            <h:selectOneMenu value="#{AdvancedSearchBean.contentFormat}" id="selectContentFormat">
                                                               <f:selectItems value="#{AdvancedSearchBean.contentFormats}" id="contentFormats" />
                                                            </h:selectOneMenu>
                                                         </td>
                                                      </tr>
                                                      <tr>
                                                         <td style="padding-left:8px"><h:outputText value="#{msg.title}" id="title" />:</td><td><h:inputText value="#{AdvancedSearchBean.title}" size="28" maxlength="1024" id="txtTitle" /></td>
                                                      </tr>
                                                      <tr>
                                                         <td style="padding-left:8px"><h:outputText value="#{msg.description}" id="desc" />:</td><td><h:inputText value="#{AdvancedSearchBean.description}" size="28" maxlength="1024" id="txtDesc" /></td>
                                                      </tr>
                                                      <tr>
                                                         <td style="padding-left:8px"><h:outputText value="#{msg.author}" id="author" />:</td><td><h:inputText value="#{AdvancedSearchBean.author}" size="28" maxlength="1024" id="txtAuthor" /></td>
                                                      </tr>
                                                   </table>
                                                   <table cellpadding="1" cellspacing="0" border="0">
                                                      <tr><td colspan=2 class="paddingRow"></td></tr>
                                                      <tr>
                                                         <td colspan=2><h:selectBooleanCheckbox value="#{AdvancedSearchBean.modifiedDateChecked}" id="chkModDate" /><span style="vertical-align:20%"><h:outputText value="#{msg.modified_date}" id="modDate" />:</span></td>
                                                      </tr>
                                                      <tr>
                                                         <td style="padding-left:8px"><h:outputText value="#{msg.from}" id="modDateFrom" />:</td><td><a:inputDatePicker value="#{AdvancedSearchBean.modifiedDateFrom}" yearCount="30" id="dateModFrom" /></td>
                                                      </tr>
                                                      <tr>
                                                         <td style="padding-left:8px"><h:outputText value="#{msg.to}" id="modDateTo" />:</td><td><a:inputDatePicker value="#{AdvancedSearchBean.modifiedDateTo}" yearCount="30" id="dateModTo" /><td>
                                                      </tr>
                                                      
                                                      <tr>
                                                         <td colspan=2><h:selectBooleanCheckbox value="#{AdvancedSearchBean.createdDateChecked}" id="chkCreateDate" /><span style="vertical-align:20%"><h:outputText value="#{msg.created_date}" id="createDate" />:</span></td>
                                                      </tr>
                                                      <tr>
                                                         <td style="padding-left:8px"><h:outputText value="#{msg.from}" id="createDateFrom" />:</td><td><a:inputDatePicker value="#{AdvancedSearchBean.createdDateFrom}" yearCount="30" id="dateCreatedFrom" /></td>
                                                      </tr>
                                                      <tr>
                                                         <td style="padding-left:8px"><h:outputText value="#{msg.to}" id="createDateTo" />:</td><td><a:inputDatePicker value="#{AdvancedSearchBean.createdDateTo}" yearCount="30" id="dateCreatedTo" /><td>
                                                      </tr>
                                                   </table>
                                                   <div style="padding:4px"></div>
                                                   <a:panel label="#{msg.additional_options}" id="custom-panel" style="font-weight:bold" progressive="true"
                                                         expanded='#{AdvancedSearchBean.panels["custom-panel"]}' expandedActionListener="#{AdvancedSearchBean.expandPanel}">
                                                      <r:searchCustomProperties id="customProps" bean="AdvancedSearchBean" var="customProperties" style="padding-left:12px;padding-top:4px" />
                                                   </a:panel>
                                                </td>
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
                                       <h:commandButton id="search-button" value="#{msg.search}" action="#{AdvancedSearchBean.search}" styleClass="wizardButton" />
                                    </td>
                                 </tr>
                                 <tr>
                                    <td align="center">
                                       <h:commandButton value="#{msg.close}" action="browse" styleClass="wizardButton" />
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