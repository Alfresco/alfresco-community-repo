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
<script language="javascript" type="text/javascript" src="<%=request.getContextPath()%>/scripts/tiny_mce/tiny_mce.js"></script>
<script language="javascript" type="text/javascript">

   <%-- Init the Tiny MCE in-line HTML editor --%>
   tinyMCE.init({
   	theme : "advanced",
   	mode : "exact",
   	elements : "editor",
   	save_callback : "saveContent",
		plugins : "table",
		theme_advanced_toolbar_location : "top",
		theme_advanced_toolbar_align : "left",
		theme_advanced_buttons1_add : "fontselect,fontsizeselect",
		theme_advanced_buttons2_add : "separator,forecolor,backcolor",
		theme_advanced_buttons3_add_before : "tablecontrols,separator",
		theme_advanced_disable: "styleselect",
		extended_valid_elements : "a[href|target|name],font[face|size|color|style],span[class|align|style]"
   });
   
   function saveContent(id, content)
   {
      document.getElementById("wizard:wizard-body:editor-output").value = content;
   }
   
   var isIE = (document.all);
   
</script>
</f:verbatim>
                              
<a:errors message="#{msg.error_wizard}" styleClass="errorMessage" />
         
<f:verbatim>                   
<div id='editor' style='width:100%; height:360px'>
   </f:verbatim>
   <h:outputText value="#{WizardManager.bean.content}" escape="false" />
   <f:verbatim>
</div>
</f:verbatim>
<h:inputHidden id="editor-output" value="#{WizardManager.bean.content}" />
