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
<%@ page import="org.alfresco.web.bean.FileUploadBean" %>

<f:verbatim>
<script type="text/javascript">
   function upload_file(el)
   {
     el.form.method = "post";
     el.form.enctype = "multipart/form-data";
     el.form.action = "<%= request.getContextPath() %>/uploadFileServlet";
     el.form.submit();
   }
</script>
</f:verbatim>

<h:panelGrid columns="1" cellpadding="2" style="padding-top: 4px; padding-bottom: 4px;"
             width="100%" rowClasses="wizardSectionHeading">
   <h:outputText value="&nbsp;#{msg.general_properties}" escape="false" />
</h:panelGrid>

<h:panelGrid id="panel_grid_3"
             columns="3" cellpadding="3" cellspacing="3" border="0"
             width="100%">
<%--
   <h:graphicImage value="/images/icons/required_field.gif" alt="Required Field" />
   <h:outputText id="panel_grid_3_output_text_1"
                 value="Presentation Template Type:" escape="false" />
   <h:selectOneRadio value="#{WizardManager.bean.presentationTemplateType}">
     <f:selectItems value="#{WizardManager.bean.createPresentationTemplateTypes}"/>
   </h:selectOneRadio>
--%>

   <h:graphicImage id="required_image_pt"
                   value="/images/icons/required_field.gif" alt="Required Field" />
   <h:outputText id="output_text_pt"
                 value="Presentation Template:"/>
   <h:column id="column_pt">
<%
FileUploadBean upload = (FileUploadBean)session.getAttribute(FileUploadBean.getKey("pt"));
if (upload == null || upload.getFile() == null)
{
%>
   <f:verbatim>
     <input type="hidden" name="upload-id" value="pt"/>
     <input type="hidden" name="return-page" value="<%= request.getContextPath() %>/faces<%= request.getServletPath() %>"/>
     <input id="wizard:wizard-body:file-input" type="file" size="35" name="alfFileInput" onchange="javascript:upload_file(this)"/>
   </f:verbatim>
<%
} else {
%>
   <h:outputText id="output_text_schema_name"
                 value="#{WizardManager.bean.presentationTemplateFileName}"/>
   <h:outputText id="output_text_schema_space"
                 value="&nbsp;"
		 escape="false"/>
   <a:actionLink id="action_link_remove_schema"
                 image="/images/icons/delete.gif" 
                 value="#{msg.remove}" 
                 action="#{WizardManager.bean.removeUploadedPresentationTemplateFile}"
                 showLink="false" 
		 target="top"/>
<%
}
%>
   </h:column>

</h:panelGrid>
