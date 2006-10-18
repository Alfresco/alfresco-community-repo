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
<%@ page import="org.alfresco.web.bean.wcm.AVMConstants" %>
<%@ page import="org.alfresco.web.app.Application" %>
<%@ page import="org.alfresco.web.templating.*" %>
<%@ page import="org.alfresco.web.bean.wcm.CreateWebContentWizard" %>
<%@ page import="org.w3c.dom.Document" %>

<script type="text/javascript">
function _xforms_getSubmitButtons()
{
  return [ document.getElementById("wizard:next-button"),
           document.getElementById("wizard:finish-button") ];
}
</script>
<%
final CreateWebContentWizard wiz = (CreateWebContentWizard)
    Application.getWizardManager().getBean();
TemplateType tt = wiz.getTemplateType();
TemplateInputMethod tim = tt.getInputMethods().get(0);
final TemplatingService ts = TemplatingService.getInstance();
final TemplateInputMethod.InstanceData instanceData = new TemplateInputMethod.InstanceData()
{
   public Document getContent()
   { 
      try
      {
         return (wiz.getContent() != null ? ts.parseXML(wiz.getContent()) : null);
      }
      catch (Exception e)
      {
         e.printStackTrace();
         return null;
      }
   }
    
   public void setContent(final Document d)
   {
      wiz.setContent(ts.writeXMLToString(d));
   }
};
tim.generate(instanceData, tt, out);
%>
