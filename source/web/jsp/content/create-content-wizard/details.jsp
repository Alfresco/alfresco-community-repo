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

<f:verbatim>
<script type="text/javascript" src="<%=request.getContextPath()%>/scripts/validation.js"> </script>

<script type="text/javascript">
   var finishButtonPressed = false;
   window.onload = pageLoaded;
   
   function pageLoaded()
   {
      document.getElementById("wizard:wizard-body:file-name").focus();
      document.getElementById("wizard").onsubmit = validate;
      document.getElementById("wizard:next-button").onclick = function() {finishButtonPressed = true; clear_wizard();}
      document.getElementById("wizard:finish-button").onclick = function() {finishButtonPressed = true; clear_wizard();}
   }
   
   function checkButtonState()
   {
      if (document.getElementById("wizard:wizard-body:file-name").value.length == 0 )
      {
         document.getElementById("wizard:next-button").disabled = true;
         document.getElementById("wizard:finish-button").disabled = true;
      }
      else
      {
         document.getElementById("wizard:next-button").disabled = false;
         document.getElementById("wizard:finish-button").disabled = false;
      }
   }
   
   function validate()
   {
      if (finishButtonPressed)
      {
         finishButtonPressed = false;
         return validateName(document.getElementById("wizard:wizard-body:file-name"), 
                             '</f:verbatim><a:outputText value="#{msg.validation_invalid_character}" /><f:verbatim>',
                             true);
      }
      else
      {
         return true;
      }
   }

</script>
</f:verbatim>

<a:errors message="#{msg.error_wizard}" styleClass="errorMessage" />

<h:panelGrid columns="2" cellpadding="2" cellspacing="2" border="0">
   <h:outputText value="#{msg.file_name}:"/>
   <h:inputText id="file-name" value="#{WizardManager.bean.fileName}" 
                maxlength="1024" size="35" 
                onkeyup="checkButtonState();" 
                onchange="checkButtonState();" />
                
   <h:outputText value="#{msg.type}:"/>
   <h:selectOneMenu value="#{WizardManager.bean.objectType}">
      <f:selectItems value="#{WizardManager.bean.objectTypes}" />
   </h:selectOneMenu>
   
   <h:outputText value="#{msg.content_type}:"/>
   <h:selectOneMenu value="#{WizardManager.bean.mimeType}" 
                    valueChangeListener="#{WizardManager.bean.createContentChanged}">
      <f:selectItems value="#{WizardManager.bean.createMimeTypes}" />
   </h:selectOneMenu>
</h:panelGrid>

                              