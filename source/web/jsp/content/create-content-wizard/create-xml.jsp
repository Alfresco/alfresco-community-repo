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
<%@ page import="org.alfresco.web.app.Application" %>
<%@ page import="org.alfresco.web.templating.*" %>
<%@ page import="org.alfresco.web.bean.content.CreateContentWizard" %>
<%@ page import="org.w3c.dom.Document" %>

<%
final CreateContentWizard wiz = (CreateContentWizard)
  Application.getWizardManager().getBean();
TemplateType tt = wiz.getTemplateType();
TemplateInputMethod tim = tt.getInputMethods().get(0);
final TemplatingService ts = TemplatingService.getInstance();
final InstanceData instanceData = new InstanceData() {

    public Document getContent()
    { 
        try
	{
            return (wiz.getContent() != null 
	            ? ts.parseXML(wiz.getContent()) 
		    : null);
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
<script type="text/javascript">
dojo.addOnLoad(function()
{
//alert('foo');
var b = document.getElementById("wizard:next-button");
var baseOnClick = b.onclick;
b.onclick = function()
{
 if (!document.submitTrigger.done)
 {
   document.submitTrigger.buttonClick(); 
   return false;
 }
 else
 {	
   return baseOnClick();
 }
}
});
function doSubmit()
{
var b = document.getElementById("wizard:next-button");
b.click();
}
	
</script>
