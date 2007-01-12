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
<%@ taglib uri="/WEB-INF/wcm.tld" prefix="wcm" %>

<script type="text/javascript">
function _xforms_getSubmitButtons()
{
  return [ document.getElementById("wizard:next-button"),
           document.getElementById("wizard:finish-button") ];
}
function _xforms_getSaveDraftButtons()
{
  return [ document.getElementById("wizard:back-button") ];
}
</script>
<wcm:formProcessor id="form-data-renderer"
		   formProcessorSession="#{WizardManager.bean.formProcessorSession}" 
		   formInstanceData="#{WizardManager.bean.instanceDataDocument}" 
		   form="#{WizardManager.bean.form}"/>
