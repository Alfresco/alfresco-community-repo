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
<%@ page import="org.alfresco.web.bean.coci.UploadNewVersionDialog"%>
<%@ page import="org.alfresco.web.app.servlet.FacesHelper"%>
<%@ page import="javax.faces.context.FacesContext"%>
<%@ page buffer="32kb" contentType="text/html;charset=UTF-8"%>


<%
   boolean fileUploaded = false;

	UploadNewVersionDialog dialog = (UploadNewVersionDialog) FacesHelper.getManagedBean(FacesContext.getCurrentInstance(), "UploadNewVersionDialog");
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
<a:booleanEvaluator value="#{empty DialogManager.bean.fileName}">
   <f:verbatim>
   <tr>
      <td class="wizardSectionHeading"></f:verbatim><h:outputText value="#{msg.local_copy_location}" /><f:verbatim></td>
   </tr>

   <tr>
      <td>
   </f:verbatim>
         <h:panelGrid id="upload_panel" columns="2" cellpadding="2" cellspacing="2" border="0" width="100%" columnClasses="panelGridLabelColumn,panelGridValueColumn" >
            <h:outputText value="#{msg.locate_content_upload}" style="padding-left:8px"/>
            <f:verbatim> <br/> </f:verbatim>

            <h:outputText id="out_schema" value="#{msg.file_location}:" style="padding-left:8px" />
            <h:column id="upload_empty" rendered="#{empty DialogManager.bean.fileName}">
               <r:upload id="uploader" value="#{DialogManager.bean.fileName}" framework="dialog"/>
            </h:column>
         </h:panelGrid>
   <f:verbatim>
      </td>
   </tr>
   </f:verbatim>
</a:booleanEvaluator>
<f:verbatim>
</table>
</f:verbatim>