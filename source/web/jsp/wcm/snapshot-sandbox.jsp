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

<script type="text/javascript">
   window.onload = pageLoaded;
   
   function pageLoaded()
   {
      document.getElementById("dialog:dialog-body:label").focus();
      checkButtonState();
   }
   
   function checkButtonState()
   {
      if (document.getElementById("dialog:dialog-body:label").value.length == 0)
      {
         document.getElementById("dialog:finish-button").disabled = true;
      }
      else
      {
         document.getElementById("dialog:finish-button").disabled = false;
      }
   }
</script>
</f:verbatim>

<h:panelGrid columns="1" cellpadding="2" style="padding-top: 4px; padding-bottom: 4px;"
             width="100%" rowClasses="wizardSectionHeading">
   <h:outputText value="&nbsp;#{msg.snapshot_properties}" escape="false" />
</h:panelGrid>

<h:panelGrid columns="3" cellpadding="3" cellspacing="3" border="0">
   <h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
   <h:outputText value="#{msg.snapshot_label}:"/>
   <h:inputText id="label" value="#{DialogManager.bean.label}" 
                maxlength="256" size="35" 
                onkeyup="checkButtonState();" 
                onchange="checkButtonState();" />
   
   <f:verbatim />
   <h:outputText value="#{msg.description}:"/>
   <h:inputText id="desc" value="#{DialogManager.bean.description}" 
                maxlength="1024" size="35" />
</h:panelGrid>
