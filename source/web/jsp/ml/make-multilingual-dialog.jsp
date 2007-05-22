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


<h:panelGrid columns="1" cellpadding="2" style="padding-top: 4px; padding-bottom: 4px;" width="100%" rowClasses="wizardSectionHeading">
   <h:outputText value="#{msg.ml_common_content_properties}" escape="false" />
</h:panelGrid>


<h:panelGrid columns="3" cellpadding="3" cellspacing="3" border="0">
   <h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
   <h:outputText value="#{msg.author}:"/>
   <h:inputText id="author" value="#{DialogManager.bean.author}"  maxlength="1024" size="35" immediate="false" onkeyup="checkButtonState();" onchange="checkButtonState();" />
   
   <%-- language selection drop-down --%>   
   <h:graphicImage value="/images/icons/required_field.gif" alt="#{msg.required_field}" />
   <h:outputText value="#{msg.language}:"/>
   <h:selectOneMenu id="language" value="#{DialogManager.bean.language}" immediate="false" onchange="checkButtonState();" onkeydown="checkButtonState();" onkeyup="checkButtonState();">
      <f:selectItem  itemLabel="#{msg.select_language}"   itemValue="null"/>
      <f:selectItems value="#{DialogManager.bean.filterLanguages}"/>
   </h:selectOneMenu>
</h:panelGrid>

<h:panelGrid columns="1" cellpadding="3" cellspacing="3" border="0" style="padding-top: 4px;"
                  width="100%" rowClasses="wizardSectionHeading, paddingRow">
   <h:outputText value="#{msg.ml_other_options}" escape="false" />
</h:panelGrid>


<h:panelGrid style="padding-top: 2px;" columns="2">
   <h:selectBooleanCheckbox id="add_translation" value="#{DialogManager.bean.addTranslationAfter}" onchange="submit();"  immediate="false"/>
   <h:outputText value="#{msg.ml_add_trans_when_diag_close}" />
</h:panelGrid>

<h:panelGrid style="padding-top: 2px;" id="panel_adding_mode" >
   <h:panelGrid style="padding-top: 4px;padding-left: 15px;">
      <h:selectOneRadio required="false" value="#{DialogManager.bean.addingMode}" disabled="#{!DialogManager.bean.addTranslationAfter}" immediate="false" id="radioWithContent" layout="pageDirection">
         <f:selectItem itemValue="ADD_WITH_CONTENT"    itemLabel="#{msg.ml_with_content}" itemDisabled="#{!DialogManager.bean.addTranslationAfter}"/>
         <f:selectItem itemValue="ADD_WITHOUT_CONTENT" itemLabel="#{msg.ml_just_trans_info}" itemDisabled="#{!DialogManager.bean.addTranslationAfter}"/>
      </h:selectOneRadio>      
   </h:panelGrid>
</h:panelGrid>

<script type="text/javascript">
      
      function checkButtonState()
      {
                  
         if (document.getElementById("dialog:dialog-body:author").value.length == 0 || 
             document.getElementById("dialog:dialog-body:language").selectedIndex == 0 )
         {
            document.getElementById("dialog:finish-button").disabled = true;
         }
         else
         {
            document.getElementById("dialog:finish-button").disabled = false;
         }
      }            
   
</script>


