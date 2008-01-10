<%--
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
--%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a"%>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r"%>

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8"%>
<%@ page isELIgnored="false"%>
<%@ page import="javax.faces.context.FacesContext"%>
<%@ page import="org.alfresco.web.app.Application"%>
<%@ page import="org.alfresco.web.bean.wcm.ImportWebsiteDialog"%>
<%@ page import="org.alfresco.web.app.servlet.FacesHelper"%>
<%@ page import="org.alfresco.web.ui.common.PanelGenerator"%>


<f:verbatim>
<table cellpadding="2" cellspacing="2" border="0" width="100%">
</f:verbatim>
   <%
      ImportWebsiteDialog bean = (ImportWebsiteDialog) FacesHelper.getManagedBean(FacesContext.getCurrentInstance(), "ImportWebsiteDialog");
      boolean foundFile = (bean != null && bean.getFileName() != null);
      if (foundFile == false)
      {
   %>
   <f:verbatim>
   <tr>
      <td colspan="3" class="wizardSectionHeading"></f:verbatim><h:outputText id="text0" value="#{msg.upload_content}" /><f:verbatim></td>
   </tr>
   <tr>
      <td></f:verbatim> 
         <h:panelGrid id="upload_panel" columns="2" cellpadding="2" cellspacing="2" border="0" width="100%" columnClasses="panelGridLabelColumn,panelGridValueColumn">

            <h:outputText value="#{msg.locate_content}" style="padding-left:8px"/>
            <f:verbatim/>

            <h:outputText id="out_schema" value="#{msg.file_location}:" style="padding-left:8px" />
            <r:upload id="uploader" value="#{DialogManager.bean.fileName}" framework="dialog"/>
         </h:panelGrid>
      <f:verbatim></td>
   </tr></f:verbatim>
   
      <% }
         if (foundFile)
         {
      %>
      <f:verbatim>
      <tr>
         <td>
         <table border="0" cellspacing="2" cellpadding="2" class="selectedItems">
            <tr>
               <td colspan="2" class="selectedItemsHeader"></f:verbatim><h:outputText id="text2" value="#{msg.uploaded_content}" /><f:verbatim>
               </th>
            </tr>
            <tr>
               <td class="selectedItemsRow"></f:verbatim><h:outputText id="text3" value="#{DialogManager.bean.fileName}" /><f:verbatim></td>
               <td></f:verbatim><a:actionLink image="/images/icons/delete.gif" value="#{msg.remove}" action="#{DialogManager.bean.removeUploadedFile}" showLink="false" id="link1" /><f:verbatim></td>
            </tr>
         </table>
         </td>
         <td width="100%" valign="middle" align="center">
         <div id="progress" style="display: none"><img src="<%=request.getContextPath()%>/images/icons/process_animation.gif" width=174 height=14></div>
         </td>
      </tr>
                                 <tr><td colspan="2" class="paddingRow"></td></tr>
                                 <tr>
                                    <td colspan="2"></f:verbatim>
                                       <h:selectBooleanCheckbox id="chkHighByte" value="#{ImportWebsiteDialog.highByteZip}" /><f:verbatim>&nbsp;
                                       <span style="vertical-align:20%"></f:verbatim><h:outputText id="msgHighByte" value="#{msg.import_high_byte_zip_file}"/><f:verbatim></span>
                                    </td>
                                 </tr>
                                 <tr><td colspan="2" class="paddingRow"></td></tr> 
      </f:verbatim>
      <%}%>
<f:verbatim>
</table>
</f:verbatim>