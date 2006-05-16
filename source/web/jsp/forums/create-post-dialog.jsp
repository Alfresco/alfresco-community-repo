<%--
  Copyright (C) 2005 Alfresco, Inc.
 
  Licensed under the Mozilla Public License version 1.1 
  with a permitted attribution clause. You may obtain a
  copy of the License at
 
    http://www.alfresco.org/legal/license.txt
 
  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  either express or implied. See the License for the specific
  language governing permissions and limitations under the
  License.
--%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<script type="text/javascript">
   document.getElementById("dialog:dialog-body:message").focus();
   
   function checkButtonState()
   {
      if (document.getElementById("dialog:dialog-body:message").value.length == 0)
      {
         document.getElementById("dialog:finish-button").disabled = true;
      }
      else
      {
         document.getElementById("dialog:finish-button").disabled = false;
      }
   }
</script>

<h:panelGrid cellpadding="2" cellspacing="2" border="0" width="100%"
             rowClasses="wizardSectionHeading, paddingRow">
   <h:outputText value="#{msg.message}" />
   <h:panelGrid cellpadding="2" cellspacing="6" border="0" columns="3" 
                columnClasses="alignTop, alignTop, alignTop">
      <h:graphicImage value="/images/icons/required_field.gif" alt="Required Field" />
      <h:outputText value="#{msg.message}:" />
      <h:inputTextarea id="message" value="#{DialogManager.bean.content}" rows="6" cols="70" 
                          onkeyup="checkButtonState();" onchange="checkButtonState();" />
   </h:panelGrid>
</h:panelGrid>
    
