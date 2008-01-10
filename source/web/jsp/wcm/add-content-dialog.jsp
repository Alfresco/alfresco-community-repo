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
<%@ page import="org.alfresco.web.bean.wcm.AddAvmContentDialog"%>
<%@ page import="org.alfresco.web.app.servlet.FacesHelper"%>
<%@ page import="org.alfresco.web.ui.common.PanelGenerator"%>


<%
   boolean fileUploaded = false;

   AddAvmContentDialog dialog = (AddAvmContentDialog) FacesHelper.getManagedBean(FacesContext.getCurrentInstance(), "AddAvmContentDialog");
   if (dialog != null && dialog.getFileName() != null)
   {
      fileUploaded = true;
   }
%>
<f:verbatim>
   <script type="text/javascript" src="<%=request.getContextPath()%>/scripts/validation.js"> </script>
   <%
               if (fileUploaded)
               {
                   PanelGenerator.generatePanelStart(out, request.getContextPath(), "yellowInner", "#ffffcc");
                   out.write("<img alt='' align='absmiddle' src='");
                   out.write(request.getContextPath());
                   out.write("/images/icons/info_icon.gif' />&nbsp;&nbsp;");
                   out.write(dialog.getFileUploadSuccessMsg());
                   PanelGenerator.generatePanelEnd(out, request.getContextPath(), "yellowInner");
                   out.write("<div style='padding:2px;'></div>");
               }
   %>
<table cellpadding="2" cellspacing="2" border="0" width="100%">
   </f:verbatim>
   <%
               if (fileUploaded == false)
               {
   %>
   <f:verbatim>
   <tr>
      <td colspan="3" class="wizardSectionHeading">
         </f:verbatim>
         <h:outputText id="text0" value="#{msg.upload_content}" />
         <f:verbatim>
      </td>
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
<%
   }
%>
<%
   if (fileUploaded)
      {
%>
         <f:verbatim>
   <tr>
      <td colspan="3">
         <table border="0" cellspacing="2" cellpadding="2" class="selectedItems">
            <tr>
               <td colspan="2" class="selectedItemsHeader"></f:verbatim><h:outputText id="text2" value="#{msg.uploaded_content}" /><f:verbatim>
               </td>
            </tr>
            <tr>
               <td class="selectedItemsRow"></f:verbatim><h:outputText id="text3" value="#{DialogManager.bean.fileName}" /><f:verbatim></td>
               <td></f:verbatim><a:actionLink image="/images/icons/delete.gif" value="#{msg.remove}" action="#{DialogManager.bean.removeUploadedFile}" showLink="false" id="link1" /><f:verbatim></td>
            </tr>
         </table>
      </td>
   </tr>
   <tr>
      <td class="paddingRow"></td>
   </tr>
   <tr>
      <td colspan="3" class="wizardSectionHeading">&nbsp;
         </f:verbatim>
         <h:outputText id="text4" value="#{msg.general_properties}" />
         <f:verbatim>
      </td>
   </tr>
   <tr>
      <td class="paddingRow"></td>
   </tr>
   <tr>
      <td align="middle">
         </f:verbatim>
         <h:graphicImage id="required_field" value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
         <f:verbatim>
      </td>
      <td>
         </f:verbatim>
         <h:outputText id="text5" value="#{msg.name}:" />
         <f:verbatim>
      </td>
      <td width="85%">
         </f:verbatim>
         <h:inputText id="file-name" value="#{DialogManager.bean.fileName}" maxlength="1024" size="35" onkeyup="checkButtonState();" onchange="checkButtonState();" />
         <f:verbatim>
      </td>
   </tr>
   <tr>
      <td></td>
      <td>
         </f:verbatim>
         <h:outputText id="text7" value="#{msg.content_type}:" />
         <f:verbatim>
      </td>
      <td>
         </f:verbatim>
         <r:mimeTypeSelector id="mime-type" value="#{DialogManager.bean.mimeType}" />
         <f:verbatim>
      </td>
   </tr>
   <tr>
      <td></td>
      <td>
         </f:verbatim>
         <h:outputText id="text8" value="#{msg.encoding}:" />
         <f:verbatim>
      </td>
      <td>
         </f:verbatim>
         <r:charsetSelector id="charset" value="#{DialogManager.bean.encoding}" />
         <f:verbatim>
      </td>
   </tr>
   </f:verbatim>
   <%
      }
   %>
   <f:verbatim>
</table>

      <script type="text/javascript">
      var finishButtonPressed = false;
      window.onload = pageLoaded;
      
      function pageLoaded()
      {
         document.getElementById("dialog:finish-button").onclick = function() {finishButtonPressed = true; clear_add_2Dcontent_2Dupload_2Dend();}
      }
      
      function checkButtonState()
      {
         if (document.getElementById("dialog:dialog-body:file-name").value.length == 0 )
         {
            document.getElementById("dialog:finish-button").disabled = true;
         }
         else
         {
            document.getElementById("dialog:finish-button").disabled = false;
         }
      }
      
      function validate()
      {
         if (finishButtonPressed)
         {
            finishButtonPressed = false;
            return validateName(document.getElementById("dialog:dialog-body:file-name"), 
                                '</f:verbatim><a:outputText id="text11" value="#{msg.validation_invalid_character}" /><f:verbatim>', true);
         }
         else
         {
            return true;
         }
      }
   
   </script>
</f:verbatim>