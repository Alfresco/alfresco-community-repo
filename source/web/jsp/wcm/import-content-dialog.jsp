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
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ page import="javax.faces.context.FacesContext" %>
<%@ page import="org.alfresco.web.app.Application" %>
<%@ page import="org.alfresco.web.bean.wcm.ImportWebsiteDialog" %>
<%@ page import="org.alfresco.web.app.servlet.FacesHelper" %>
<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>


<r:page titleId="title_import_content">

<f:view>
   
   <%-- load a bundle of properties with I18N strings --%>
   <f:loadBundle basename="alfresco.messages.webclient" var="msg"/>
   
   <h:form acceptCharset="UTF-8" id="add-content-upload-start">
   
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
                              <h:graphicImage id="dialog-logo" url="/images/icons/import_website_large.gif" width="32" height="32" />
                           </td>
                           <td>
                              <div class="mainTitle"><h:outputText value="#{msg.import_website_content_title}" /></div>
                              <div class="mainSubText"><h:outputText value="#{msg.import_website_content_desc}" /></div>
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
               
               </h:form>
               
               <%-- Details --%>
               <tr valign=top>                  
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width="4"></td>
                  <td>
                     <table cellspacing="0" cellpadding="3" border="0" width="100%">
                        <tr>
                           <td width="100%" valign="top">
                              
                              <a:errors message="#{msg.error_dialog}" styleClass="errorMessage" />
                              
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "white", "white"); %>
                              
                              <table cellpadding="2" cellspacing="2" border="0" width="100%">
                                 <%
                                 ImportWebsiteDialog bean = (ImportWebsiteDialog)FacesHelper.getManagedBean(FacesContext.getCurrentInstance(), "ImportWebsiteDialog");
                                 boolean foundFile = (bean != null && bean.getFileName() != null);
                                 if (foundFile == false) {
                                 %>
                                 <tr>
                                    <td colspan="3" class="wizardSectionHeading"><h:outputText value="#{msg.upload_content}"/></td>
                                 </tr>
                                 <tr><td class="paddingRow"></td></tr>
                                 <tr>
                                    <td class="mainSubText" colspan="3">
                                       <h:outputText id="text1" value="1. #{msg.locate_content}"/>
                                    </td>
                                 </tr>
                                 <tr><td class="paddingRow"></td></tr>
                                 
                                 <r:uploadForm>
                                 <tr>
                                    <td colspan="3">
                                       <input style="margin-left:12px;" type="file" size="75" name="alfFileInput"/>
                                    </td>
                                 </tr>
                                 <tr><td class="paddingRow"></td></tr>
                                 <tr>
                                    <td class="mainSubText" colspan="3">
                                       2. <%=Application.getMessage(FacesContext.getCurrentInstance(), "click_upload")%>
                                    </td>
                                 </tr>
                                 <tr>
                                    <td colspan="3">
                                       <input style="margin-left:12px;" type="submit" value="<%=Application.getMessage(FacesContext.getCurrentInstance(), "upload")%>" />
                                    </td>
                                 </tr>
                                 </r:uploadForm>
                                 <% } %>
                                 
                                 <h:form acceptCharset="UTF-8" id="add-content-upload-end">
                                 <% if (foundFile) { %>
                                 <tr>
                                    <td>
                                       <table border="0" cellspacing="2" cellpadding="2" class="selectedItems">
                                          <tr>
                                             <td colspan="2" class="selectedItemsHeader">
                                                <h:outputText id="text2" value="#{msg.uploaded_content}" />
                                             </th>
                                          </tr>
                                          <tr>
                                             <td class="selectedItemsRow">
                                                <h:outputText id="text3" value="#{ImportWebsiteDialog.fileName}" />
                                             </td>
                                             <td>
                                                <a:actionLink image="/images/icons/delete.gif" value="#{msg.remove}" 
                                                              action="#{ImportWebsiteDialog.removeUploadedFile}" 
                                                              showLink="false" id="link1" />
                                             </td>
                                          </tr>
                                       </table>
                                    </td>
                                    <td width="100%" valign="middle" align="center">
                                       <div id="progress" style="display:none">
                                          <img src="<%=request.getContextPath()%>/images/icons/process_animation.gif" width=174 height=14>
                                       </div>
                                    </td>
                                 </tr>
                                 <% } %>
                                 
                              </table>
                              <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "white"); %>
                           </td>
                           
                           <td valign="top">
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "greyround", "#F5F5F5"); %>
                              <table cellpadding="1" cellspacing="1" border="0">
                                 <tr>
                                    <td align="center">
                                       <h:commandButton id="finish-button" styleClass="wizardButton"
                                                        value="#{msg.ok}" 
                                                        action="#{ImportWebsiteDialog.finish}"
                                                        onclick="javascript:document.getElementById('progress').style.display='inline';"
                                                        disabled="#{ImportWebsiteDialog.finishButtonDisabled}" />
                                    </td>
                                 </tr>
                                 <tr>
                                    <td align="center">
                                       <h:commandButton id="cancel-button" styleClass="wizardButton"
                                                        value="#{msg.cancel}" 
                                                        action="#{ImportWebsiteDialog.cancel}" />
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