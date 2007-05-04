<!--
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing
-->
<jsp:root version="1.2"
          xmlns:jsp="http://java.sun.com/JSP/Page"
 	  xmlns:c="http://java.sun.com/jsp/jstl/core"
          xmlns:fmt="http://java.sun.com/jsp/jstl/fmt"
          xmlns:f="http://java.sun.com/jsf/core"
          xmlns:h="http://java.sun.com/jsf/html">

  <jsp:output doctype-root-element="html"
	      doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
	      doctype-system="http://www.w3c.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"/>

  <jsp:directive.page language="java" contentType="text/html; charset=UTF-8"/>
  <jsp:directive.page isELIgnored="false"/>
  <script language="javascript" type="text/javascript" src="${pageContext.request.contextPath}/scripts/tiny_mce/tiny_mce.js">&#160;</script>
  <script language="javascript" type="text/javascript" src="${pageContext.request.contextPath}/scripts/ajax/tiny_mce_wcm_extensions.js">&#160;</script>
  <script language="javascript" type="text/javascript" src="${pageContext.request.contextPath}/scripts/ajax/file_picker_widget.js">&#160;</script>
  <script language="javascript" type="text/javascript" src="${pageContext.request.contextPath}/scripts/ajax/ajax_helper.js">&#160;</script>
  <script language="javascript" type="text/javascript" src="${pageContext.request.contextPath}/scripts/ajax/dojo/dojo.js">&#160;</script>
  <script language="javascript" type="text/javascript" src="${pageContext.request.contextPath}/scripts/upload_helper.js">&#160;</script>

  <script language="javascript" type="text/javascript">
if (!String.prototype.startsWith)
{
  String.prototype.startsWith = function(s)
  {
    return this.indexOf(s) == 0;
  }
}

    <!-- Init the Tiny MCE in-line HTML editor -->
    var alfresco = typeof alfresco == "undefined" ? {} : alfresco;
    alfresco.constants = typeof alfresco.constants == "undefined" ? {} : alfresco.constants;

    alfresco.constants.WEBAPP_CONTEXT = "${pageContext.request.contextPath}";
    alfresco.constants.AVM_WEBAPP_URL = "${WizardManager.bean.previewSandboxUrl}";

    alfresco.resources = {
    loading: "${msg.loading}",
    ide: "${msg.idle}",
    add_content: "${msg.add_content}",
    go_up: "${msg.go_up}",
    upload: "${msg.upload}",
    path: "${msg.path}",
    cancel: "${msg.cancel}"
    };

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
    extended_valid_elements : "a[href|target|name],font[face|size|color|style],span[class|align|style]",
    urlconverter_callback: "alfresco_TinyMCE_urlconverter_callback",
    execcommand_callback: "alfresco_TinyMCE_execcommand_callback",
    });
    
    function saveContent(id, content)
    {
    document.getElementById("wizard:wizard-body:editor-output").value = content;
    }
    
    var isIE = (document.all);
    
  </script>

  <div id='editor' style='width:100%; height:360px'>
    <h:outputText value="#{WizardManager.bean.content}" escape="false" />
  </div>
  <h:inputHidden id="editor-output" value="#{WizardManager.bean.content}" />
</jsp:root>
