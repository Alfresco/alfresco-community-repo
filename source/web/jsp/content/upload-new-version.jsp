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
   <a:booleanEvaluator value="#{empty DialogManager.bean.fileName}" ><f:verbatim> 
      <tr>
         <td class="wizardSectionHeading"></f:verbatim><h:outputText value="#{msg.local_copy_location}" /><f:verbatim></td>
      </tr>
   
      <tr>
         <td></f:verbatim>
            <h:panelGrid id="upload_panel" columns="2" cellpadding="2" cellspacing="2" border="0" width="100%" columnClasses="panelGridLabelColumn,panelGridValueColumn" >

               <h:outputText value="#{msg.locate_content_upload}" style="padding-left:8px"/>
               <f:verbatim/>

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
   <tr>
      <td>
      </f:verbatim>
         <%
      if (fileUploaded)
      {
   %>
         <a:booleanEvaluator value="#{DialogManager.bean.versionable}" > 
	        <f:verbatim>
            <table cellpadding="2" cellspacing="2" border="0" width="100%">
               <tr>
                  <td class="wizardSectionHeading"> </f:verbatim>
                     <h:outputText value="#{msg.version_info}" /> <f:verbatim>
                  </td>
               </tr>
               <tr>
                  <td> </f:verbatim>
                     <h:outputText value="#{msg.new_version_has}" escape="false" /> <f:verbatim>
                  </td>
               </tr>
               <tr>
                  <td> </f:verbatim>
                     <h:selectOneRadio id="vvvv"  value="#{CCProperties.minorChange}" required="true" layout="pageDirection" rendered="#{DialogManager.bean.versionable}">
                        <f:selectItem itemValue="#{true}" itemLabel="#{msg.minor_changes} (#{DialogManager.bean.minorNewVersionLabel})" />
                        <f:selectItem itemValue="#{false}" itemLabel="#{msg.major_changes} (#{DialogManager.bean.majorNewVersionLabel})" />
                     </h:selectOneRadio> 
                     <h:message for="vvvv"></h:message>

                     <f:verbatim>
                     </span> <br/>
                  </td>
               </tr>
               <tr>
                  <td> </f:verbatim>
                     <h:outputText value="#{msg.version_notes}" /> <f:verbatim>
                  </td>
               </tr>
               <tr>
                  <td> </f:verbatim>
                  <h:inputTextarea value="#{CCProperties.versionNotes}" rows="4" cols="50" /> <f:verbatim>
                  </span></td>
               </tr>
               <tr>
                  <td class="paddingRow"></td>
               </tr>
            </table> </f:verbatim>
         </a:booleanEvaluator>
      
      		 
      		<f:verbatim>
      </td>
      
   </tr> 
   </f:verbatim>
      
         <f:verbatim>
         <tr>
      	   <td class="wizardSectionHeading"></f:verbatim><h:outputText value="#{msg.other_options}" /><f:verbatim></td>
   	     </tr>
   	     <tr>
   	  	   <td>
   	  	      </f:verbatim>
   	  		  <h:selectBooleanCheckbox value="#{DialogManager.bean.finishedEditing}"/> <h:outputText value="#{msg.done_editing_file}"/> 
   	  		  <f:verbatim>
   	  	   </td>
   	     </tr>
   	     </f:verbatim>
   	  
   	      <% 
      }
          %>
   	  <f:verbatim>
</table>
</f:verbatim>
