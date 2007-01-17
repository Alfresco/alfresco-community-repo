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
<%@ page import="org.alfresco.web.bean.ImportBean" %>
<%@ page import="org.alfresco.web.app.servlet.FacesHelper" %>
<%@ page import="org.alfresco.web.app.Application" %>
<%@ page import="javax.faces.context.FacesContext" %>

<r:page titleId="title_import">

<f:view>
   
   <%-- load a bundle of properties with I18N strings --%>
   <f:loadBundle basename="alfresco.messages.webclient" var="msg"/>
   
   <h:form acceptCharset="UTF-8" id="import-form">
   
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
                              <h:graphicImage id="wizard-logo" url="/images/icons/import_large.gif" />
                           </td>
                           <td>
                              <div class="mainTitle"><h:outputText value="#{msg.import}" /> '<h:outputText value='#{BrowseBean.actionSpace.name}' />'</div>
                              <div class="mainSubText"><h:outputText value="#{msg.import_info}" /></div>
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
                           </h:form>
                        
                           <td width="100%" valign="top">
                              
                              <a:errors message="#{msg.error_import_all}" styleClass="errorMessage" />
                              
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "white", "white"); %>
                              <table cellpadding="2" cellspacing="2" border="0">
                                 <tr><td class="paddingRow"></td></tr>
                                 <tr>
                                    <td class="mainSubText">1. <h:outputText value="#{msg.locate_doc_upload}"/></td>
                                 </tr>
                                 <tr><td class="paddingRow"></td></tr>
                                 <r:uploadForm>
                                 <tr>
                                    <td>
                                       <h:outputText value="#{msg.location}"/>:<input style="margin-left:12px;" type="file" size="50" name="alfFileInput"/>
                                    </td>
                                 </tr>
                                 <tr><td class="paddingRow"></td></tr>
                                 <tr>
                                    <td class="mainSubText">2. <h:outputText value="#{msg.click_upload}"/></td>
                                 </tr>
                                 <tr>
                                    <td>
                                       <input style="margin-left:12px;" type="submit" value="<%=Application.getMessage(FacesContext.getCurrentInstance(), "upload")%>" />
                                    </td>
                                 </tr>
                                 </r:uploadForm>
                                 
                                 <h:form acceptCharset="UTF-8" id="import-upload-end">
                                 <tr><td class="paddingRow"></td></tr>
                                 <%
                                 ImportBean bean = (ImportBean)FacesHelper.getManagedBean(FacesContext.getCurrentInstance(), "ImportDialog");
                                 if (bean != null && bean.getFileName() != null) {
                                 %>
                                    <tr>
                                       <td>
                                          <img alt="" align="absmiddle" src="<%=request.getContextPath()%>/images/icons/info_icon.gif" />
                                          <%=bean.getFileUploadSuccessMsg()%>
                                       </td>
                                    </tr>
                                 <% } %>
                                 <tr><td class="paddingRow"></td></tr>
                                 <tr>
                                    <td>
                                       <h:selectBooleanCheckbox value="#{ImportDialog.runInBackground}" />&nbsp;
                                       <span style="vertical-align:20%">3. <h:outputText value="#{msg.run_import_in_background}"/></span>
                                    </td>
                                 </tr>
                                 <tr>
                                    <td>
                                       <div id="error-info" style="padding-left: 24px;">
                                          <h:graphicImage alt="" value="/images/icons/info_icon.gif" style="vertical-align: middle;"/>&nbsp;
                                          <h:outputText value="#{msg.import_error_info}"/>
                                       </div>
                                    </td>
                                 </tr>
                                 <tr><td class="paddingRow"></td></tr>
                              </table>
                              <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "white"); %>
                           </td>
                           <td valign="top">
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "lbgrey", "white"); %>
                              <table cellpadding="1" cellspacing="1" border="0">
                                 <tr>
                                    <td align="center">
                                       <h:commandButton id="ok-button" value="#{msg.ok}" action="#{ImportDialog.performImport}" 
                                                        styleClass="wizardButton" disabled="#{ImportDialog.fileName == null}"/>
                                    </td>
                                 </tr>
                                 <tr>
                                    <td align="center">
                                       <h:commandButton value="#{msg.cancel}" action="#{ImportDialog.cancel}" 
                                                        styleClass="wizardButton"/>
                                    </td>
                                 </tr>
                              </table>
                              <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "lbgrey"); %>
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