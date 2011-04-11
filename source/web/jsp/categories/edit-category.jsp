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
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<f:verbatim>
   <script type="text/javascript">
  
      addEventToElement(window, 'load', pageLoaded, false);
     
      function pageLoaded()
      {
         document.getElementById("dialog:dialog-body:name").focus();
         checkButtonState();
      }
     
      function checkButtonState()
      {
         if (document.getElementById("dialog:dialog-body:name").value.length == 0 )
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
         <td colspan="2" class="wizardSectionHeading">
            </f:verbatim><h:outputText value="#{msg.category_props}" /><f:verbatim>
         </td>
      </tr>
      <tr>
         <td></f:verbatim><h:outputText value="#{msg.name}" /><f:verbatim>:</td>
         <td>
            </f:verbatim>
            <h:inputText id="name" value="#{DialogManager.bean.name}" size="35" maxlength="1024"
                         onkeyup="javascript:checkButtonState();" onchange="javascript:checkButtonState();"/>
            <f:verbatim>&nbsp;*
         </td>
      </tr>
      <tr>
         <td></f:verbatim><h:outputText value="#{msg.description}" /><f:verbatim>:</td>
         <td>
            </f:verbatim><h:inputText value="#{DialogManager.bean.description}" size="35" maxlength="1024" /><f:verbatim>
         </td>
      </tr>
   </table>
</f:verbatim>