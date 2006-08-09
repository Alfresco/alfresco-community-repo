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
<%@ page import="org.alfresco.web.templating.*" %>
<%@ page import="javax.faces.context.FacesContext" %>
<%@ page import="org.alfresco.web.app.servlet.FacesHelper" %>
<%@ page import="org.alfresco.web.bean.content.CreateContentWizard" %>

<%
TemplatingService ts = TemplatingService.getInstance();
CreateContentWizard ccw = (CreateContentWizard)
    FacesHelper.getManagedBean(FacesContext.getCurrentInstance(), "CreateContentWizard");

TemplateType tt = ts.getTemplateType(ccw.getTemplateType());
final TemplateInputMethod tim = tt.getInputMethods().get(0);
String url = tim.getInputURL(tt.getSampleXml(tt.getName()), tt);
%>
<f:verbatim>
<div style='width:100%; height:360px'>
<iframe src="<%= url %>"
        style="overflow: auto;width: 100%; height: 100%">
</iframe>
</div>
</f:verbatim>