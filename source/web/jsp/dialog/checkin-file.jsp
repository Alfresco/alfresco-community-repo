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
<%@ page import="org.alfresco.web.app.portlet.AlfrescoFacesPortlet" %>
<%@ page import="org.alfresco.web.bean.CheckinCheckoutBean" %>
<%@ page import="org.alfresco.web.app.Application" %>
<%@ page import="javax.faces.context.FacesContext" %>

<r:page titleId="title_checkin_file">

<f:view>
   
   <%-- load a bundle of properties with I18N strings --%>
   <f:loadBundle basename="alfresco.messages.webclient" var="msg"/>
   
   <%-- set the form name here --%>
   <h:form acceptCharset="UTF-8" id="checkin-file1">
   
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
                           <td width="32">
                              <h:graphicImage id="wizard-logo" url="/images/icons/check_in_large.gif" />
                           </td>
                           <td>
                              <div class="mainSubTitle"><h:outputText value="#{NavigationBean.nodeProperties.name}" /></div>
                              <div class="mainTitle"><h:outputText value="#{msg.check_in}" /> '<h:outputText value="#{CheckinCheckoutBean.document.name}" />'</div>
                              <div class="mainSubText"><h:outputText value="#{msg.checkinfile_description}" /></div>
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
                  <td style="padding-top: 4px;">
                     <table cellspacing="0" cellpadding="0" border="0" width="100%">
                        <tr>
                           <td width="7"><img src='<%=request.getContextPath()%>/images/parts/white_01.gif' width=7 height=7 alt=''></td>
                           <td width="100%" background='<%=request.getContextPath()%>/images/parts/white_02.gif'><img src='<%=request.getContextPath()%>/images/parts/white_02.gif' width=7 height=7 alt=''></td>
                           <td width="7"><img src='<%=request.getContextPath()%>/images/parts/white_03.gif' width=7 height=7 alt=''></td>
                           <td rowspan="4" valign="top" style="padding-left:6px;">
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "blue", "#D3E6FE"); %>
                              <table cellpadding="1" cellspacing="1" border="0">
                                 <tr>
                                    <td align="center">
                                       <h:commandButton value="#{msg.check_in}" action="#{CheckinCheckoutBean.checkinFileOK}" styleClass="dialogControls" />
                                    </td>
                                 </tr>
                                 <tr><td class="dialogButtonSpacing"></td></tr>
                                 <tr>
                                    <td align="center">
                                       <h:commandButton value="#{msg.cancel}" action="#{CheckinCheckoutBean.cancel}" styleClass="dialogControls" />
                                    </td>
                                 </tr>
                              </table>
                              <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "blue"); %>
                           </td>
                        </tr>
                        <tr>
                           <td background='<%=request.getContextPath()%>/images/parts/white_04.gif'><img src='<%=request.getContextPath()%>/images/parts/white_04.gif' width=7 height=7 alt=''></td>
                           <td>
                              <table cellpadding="2" cellspacing="2" border="0" width="100%">
                                 <tr>
                                    <td class="wizardSectionHeading"><h:outputText value="#{msg.checkin_options}" /></td>
                                 </tr>
                                 <tr>
                                    <td>
                                       <h:outputText value="#{msg.version_notes}<br/>" escape="false" rendered="#{CheckinCheckoutBean.versionable}" />
                                       <h:inputTextarea value="#{CheckinCheckoutBean.versionNotes}" rendered="#{CheckinCheckoutBean.versionable}" rows="2" cols="50" />
                                    </td>
                                 </tr>
                                 <tr>
                                    <td>
                                       <h:selectBooleanCheckbox value="#{CheckinCheckoutBean.minorChange}" />
                                       <span style="vertical-align:20%"><h:outputText value="#{msg.minor_change}" /></span>
                                    </td>
                                 </tr>
                                 <tr>
                                    <td>
                                       <h:selectBooleanCheckbox value="#{CheckinCheckoutBean.keepCheckedOut}" />
                                       <span style="vertical-align:20%"><h:outputText value="#{msg.checkin_changes_info}" /></span>
                                    </td>
                                 </tr>
                                 <tr><td class="paddingRow"></td></tr>
                                 <tr>
                                    <td class="wizardSectionHeading"><h:outputText value="#{msg.workingcopy_location}" /></td>
                                 </tr>
                                 <tr>
                                    <td>
                                       <h:outputText value="#{msg.which_copy_checkin}" />
                                    </td>
                                 </tr>
                                 
                                 <tr>
                                    <td>
                                       <h:selectOneRadio value="#{CheckinCheckoutBean.copyLocation}" layout="pageDirection">
                                          <f:selectItem itemValue="current" itemDisabled="#{CheckinCheckoutBean.fileName != null}" itemLabel="#{msg.which_copy_current}" />
                                          <f:selectItem itemValue="other" itemLabel="#{msg.which_copy_other}" />
                                       </h:selectOneRadio>
                                    </td>
                                 </tr>
                              </table>
                           </td>
                           <td background='<%=request.getContextPath()%>/images/parts/white_06.gif'><img src='<%=request.getContextPath()%>/images/parts/white_06.gif' width=7 height=7 alt=''></td>
                        </tr>
                           
                        </h:form>
                        
                        <tr>
                           <td background='<%=request.getContextPath()%>/images/parts/white_04.gif'><img src='<%=request.getContextPath()%>/images/parts/white_04.gif' width=7 height=7 alt=''></td>
                           <td>
                              <r:uploadForm>
                              <table cellpadding="2" cellspacing="2" border="0" width="100%" style="padding-left: 27px;">
                                 <tr>
                                    <td>1. <h:outputText value="#{msg.locate_doc_upload}" /></td>
                                 </tr>
                                 <tr><td class="paddingRow"></td></tr>
                                 <tr>
                                    <td>
                                       <h:outputText value="#{msg.file_location}" />:<input style="margin-left:12px;" type="file" size="50" name="alfFileInput"/>
                                    </td>
                                 </tr>
                                 <tr><td class="paddingRow"></td></tr>
                                 <tr>
                                    <td class="mainSubText">2. <h:outputText value="#{msg.click_upload}" /></td>
                                 </tr>
                                 <tr>
                                    <td>
                                       <input style="margin-left:12px;" type="submit" value="<%=Application.getMessage(FacesContext.getCurrentInstance(), "upload")%>" />
                                    </td>
                                 </tr>
                                 <%
                                 CheckinCheckoutBean bean = (CheckinCheckoutBean)session.getAttribute(AlfrescoFacesPortlet.MANAGED_BEAN_PREFIX + "CheckinCheckoutBean");
                                 if (bean == null)
                                 {
                                    bean = (CheckinCheckoutBean)session.getAttribute("CheckinCheckoutBean");
                                 }
                                 if (bean != null && bean.getFileName() != null) {
                                 %>
                                    <tr><td class="paddingRow"></td></tr>
                                    <tr>
                                       <td>
                                          <img alt="" align="absmiddle" src="<%=request.getContextPath()%>/images/icons/info_icon.gif" />
                                          <%=bean.getFileUploadSuccessMsg()%>
                                       </td>
                                    </tr>
                                 <% } %>
                              </table>
                              </r:uploadForm>
                           </td>
                           <td background='<%=request.getContextPath()%>/images/parts/white_06.gif'><img src='<%=request.getContextPath()%>/images/parts/white_06.gif' width=7 height=7 alt=''></td>
                        </tr>
                        <tr>
                           <td width="7"><img src='<%=request.getContextPath()%>/images/parts/white_07.gif' width=7 height=7 alt=''></td>
                           <td width="100%" background='<%=request.getContextPath()%>/images/parts/white_08.gif'><img src='<%=request.getContextPath()%>/images/parts/white_08.gif' width=7 height=7 alt=''></td>
                           <td width="7"><img src='<%=request.getContextPath()%>/images/parts/white_09.gif' width=7 height=7 alt=''></td>
                        </tr>
                     </table>
                  </td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_6.gif)" width="4"></td>
               </tr>
               
               <h:form acceptCharset="UTF-8" id="checkin-file2">
               
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
               
               </h:form>
               
            </table>
          </td>
       </tr>
    </table>
    
</f:view>

</r:page>