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

<f:verbatim>
<script language="JavaScript1.2">

   window.onload = pageLoaded;
   
   function pageLoaded()
   {
      document.getElementById("dialog:dialog-body:password").focus();
      updateButtonState();
   }
   
   function updateButtonState()
   {
      if (document.getElementById("dialog:dialog-body:password").value.length == 0 ||
          document.getElementById("dialog:dialog-body:confirm").value.length == 0)
      {
         document.getElementById("dialog:finish-button").disabled = true;
      }
      else
      {
         document.getElementById("dialog:finish-button").disabled = false;
      }
   }
</script>

<table cellpadding="2" cellspacing="2" border="0" width="100%">
   <tr>
      <td colspan="2"></f:verbatim><h:outputText value="#{msg.change_password_instructions}" /><f:verbatim></td>
   </tr>
   <tr>
      <td colspan="2" class="paddingRow"></td>
   </tr>
   <tr>
      <td></f:verbatim><h:outputText value="#{msg.username}" /><f:verbatim>:</td>
      <td></f:verbatim><h:outputText value="#{UsersBeanProperties.person.properties.userName}" /><f:verbatim></td>
   </tr>
   <tr>
      <td></f:verbatim><h:outputText value="#{msg.password}" /><f:verbatim>:</td>
      <td>
         </f:verbatim>
            <h:inputSecret id="password" value="#{UsersBeanProperties.password}"
                           size="35" maxlength="1024" validator="#{LoginBean.validatePassword}"
                           onkeyup="updateButtonState();" onchange="updateButtonState();" /><f:verbatim>&nbsp;*
            &nbsp;</f:verbatim><h:message id="errors1" for="password" style="color:red" /><f:verbatim></td>
   </tr>
   <tr>
      <td></f:verbatim><h:outputText value="#{msg.confirm}" /><f:verbatim>:</td>
      <td>
         </f:verbatim>
            <h:inputSecret id="confirm" value="#{UsersBeanProperties.confirm}"
                           size="35" maxlength="1024" validator="#{LoginBean.validatePassword}"
                           onkeyup="updateButtonState();" onchange="updateButtonState();" /><f:verbatim>&nbsp;*
            &nbsp;</f:verbatim><h:message id="errors2" for="confirm" style="color:red" /><f:verbatim></td>
   </tr>
</table>

<script>
   updateButtonState();
</script>
</f:verbatim>
