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
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<script type="text/javascript">
   
   window.onload = pageLoaded;
     
   function pageLoaded()
   {
      checkButtonState();
   }
   
   function checkButtonState()
   {
      if (document.getElementById("spaceSelector-value").value.length == 0)
      {
         document.getElementById("wizard:next-button").disabled = true;
      }
      else
      {
         document.getElementById("wizard:next-button").disabled = false;
      }
   }
</script>

<f:verbatim>
<table cellpadding="3" cellspacing="0" border="0" width="100%">
   <tr>
      <td class="wizardSectionHeading">
         </f:verbatim>
         <h:outputText value="#{msg.existing_space}"/>
         <f:verbatim>
      </td>
   </tr>
   <tr><td class="paddingRow"></td></tr>
   <tr>
      <td>
         </f:verbatim>
         <r:ajaxFolderSelector id="spaceSelector" label="#{msg.select_existing_space_prompt}" 
                               value="#{WizardManager.bean.existingSpaceId}" 
                               initialSelection="#{NavigationBean.currentNode.nodeRefAsString}"
                               styleClass="selector" />
         <f:verbatim>
      </td>
   </tr>
   <%-- TBD
   <tr><td class="paddingRow" /></tr>
   <tr>
      <td><h:outputText value="#{msg.copy_existing_space}"/></td>
   </tr>
   <tr>
      <td>
         <h:selectOneRadio value="#{NewSpaceWizard.copyPolicy}" layout="pageDirection">
            <f:selectItem itemValue="structure" itemLabel="#{msg.structure}" />
            <f:selectItem itemValue="contents" itemLabel="#{msg.structure_contents}" />
         </h:selectOneRadio>
      </td>
   </tr>
   --%>
   <tr><td class="paddingRow" /></tr>
   <tr>
      <td>
         </f:verbatim>
         <h:outputText value="#{msg.space_copy_note}"/>
         <f:verbatim>
      </td>
   </tr>
</table>
</f:verbatim>