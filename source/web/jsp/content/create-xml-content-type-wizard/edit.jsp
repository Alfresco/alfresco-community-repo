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
<%@ page import="java.io.*" %>
<f:verbatim>
<script type="text/javascript">
function set_edit_mode(on)
{
  var iframe = document.getElementById("editor");
  editor.setAttribute("src", 
                      on ? "</f:verbatim><h:outputText value="not_implemented" escape="false"/><f:verbatim>" 
                         : "</f:verbatim><h:outputText value="#{WizardManager.bean.formURL}" escape="false"/><f:verbatim>");
}
</script>
<div>
</f:verbatim>
<a:actionLink id="edit" 
              value="#{msg.edit}"
              rendered="true"
	      onclick="javascript:set_edit_mode(true)"/>
<f:verbatim>&nbsp;|&nbsp;</f:verbatim>
<a:actionLink id="preview" 
              value="#{msg.preview}"
	      rendered="true"
	      onclick="javascript:set_edit_mode(false)"/>
<h:outputText value="</div>" escape="false"/>
<h:outputText value="<iframe id=\"editor\" 
              style=\"width: 100%; height: 360px\" src=\""
              escape="false"/>
<h:outputText value="#{WizardManager.bean.formURL}" escape="false"/>
<h:outputText value="\"/>" escape="false"/>
<h:outputText value="</iframe>" escape="false"/>
