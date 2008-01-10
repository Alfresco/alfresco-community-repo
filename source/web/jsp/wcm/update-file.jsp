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

<f:verbatim>
<table cellpadding="2" cellspacing="2" border="0" width="100%">
   <tr>
      <td class="wizardSectionHeading"></f:verbatim><h:outputText value="#{msg.local_copy_location}" /><f:verbatim></td>
   </tr>
   <tr>
      <td></f:verbatim>
         <h:panelGrid id="upload_panel" columns="2" cellpadding="2" cellspacing="2" border="0" width="100%" columnClasses="panelGridLabelColumn,panelGridValueColumn">

            <h:outputText value="#{msg.locate_content_upload}" style="padding-left:8px"/>
            <f:verbatim/>

            <h:outputText id="out_schema" value="#{msg.file_location}:" style="padding-left:8px" />
            <h:column id="upload_empty" rendered="#{empty DialogManager.bean.fileName}">
               <r:upload id="uploader" value="#{DialogManager.bean.fileName}" framework="dialog"/>
            </h:column>
            <h:column id="upload_not_empty" rendered="#{!empty DialogManager.bean.fileName}">
               <h:outputText id="upload_name" value="#{DialogManager.bean.fileName}" style="padding-right:8px"/>
               <a:actionLink id="upload_remove" image="/images/icons/delete.gif" value="#{msg.remove}" action="#{DialogManager.bean.removeUploadedFile}" showLink="false"/>
            </h:column>
         </h:panelGrid>
      <f:verbatim></td>
   </tr>
</table>
</f:verbatim>
