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
<script type="text/javascript">
   addEventToElement(window, 'load', pageLoaded, false);
   
   function finishButton_click()
   {
      var disable = function()
      {
         document.getElementById('dialog:ok-button').disabled = true;
         document.getElementById('dialog:cancel-button').disabled = true;
      }
      disable.delay(50, this);
      document.getElementById('progress').style.display='inline';
   }
   
   function pageLoaded()
   {
      document.getElementById('dialog:ok-button').onclick = finishButton_click;
   }
</script>

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