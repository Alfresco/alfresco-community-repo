<!--
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see http://www.gnu.org/licenses/.
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
  <script language="javascript" 
          type="text/javascript" 
          src="${pageContext.request.contextPath}/scripts/tiny_mce/tiny_mce.js">&#160;</script>
  <script language="javascript" 
          type="text/javascript" 
          src="${pageContext.request.contextPath}/scripts/ajax/common.js">&#160;</script>

  <script language="javascript" type="text/javascript">
    <!-- Init the Tiny MCE in-line HTML editor -->
    var alfresco = typeof alfresco == "undefined" ? {} : alfresco;
    alfresco.constants = typeof alfresco.constants == "undefined" ? {} : alfresco.constants;

    alfresco.constants.WEBAPP_CONTEXT = "${pageContext.request.contextPath}";
    alfresco.constants.AVM_WEBAPP_URL = "${WizardManager.bean.previewSandboxUrl}";
    alfresco.constants.AVM_WEBAPP_PREFIX = "${WizardManager.bean.avmWebappPrefix}";
    alfresco.constants.AVM_WEBAPP_CONTEXT = "${WizardManager.bean.avmWebappName}";	

    alfresco.resources = 
    {
    //XXXarielb deal with encoding
      add_content: "${msg.add_content}",
      cancel: "${msg.cancel}",
      change: "${msg.change}",
      go_up: "${msg.go_up}",
      ide: "${msg.idle}",
      loading: "${msg.loading}",
      path: "${msg.path}",
      select: "${msg.select}",
      upload: "${msg.upload}"
    };
    
    var lang = "${UserPreferencesBean.language}";
    lang = lang.substring(0,lang.indexOf("_"));

    tinyMCE.init({
      theme : "advanced",
      language : lang,
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
      file_browser_callback: "alfresco_TinyMCE_file_browser_callback"
    });
    
    function saveContent(id, content)
    {
      if (alfresco.constants.AVM_WEBAPP_CONTEXT == "ROOT")
      {
        content = content.replace(new RegExp(alfresco.constants.AVM_WEBAPP_URL, "g"), "");
      }
      else
      {
        content = content.replace(new RegExp(alfresco.constants.AVM_WEBAPP_PREFIX, "g"), "");
      }	
      document.getElementById("wizard:wizard-body:editor-output").value = content;
      return content;
    }
  </script>
  <script language="javascript" 
          type="text/javascript" 
          src="${pageContext.request.contextPath}/scripts/ajax/dojo/dojo.js">&#160;</script>
  <script language="javascript" 
          type="text/javascript" 
          src="${pageContext.request.contextPath}/scripts/ajax/tiny_mce_wcm_extensions.js">&#160;</script>
  <script language="javascript" 
          type="text/javascript" 
          src="${pageContext.request.contextPath}/scripts/ajax/ajax_helper.js">&#160;</script>
  <script language="javascript" 
          type="text/javascript" 
          src="${pageContext.request.contextPath}/scripts/ajax/file_picker_widget.js">&#160;</script>
  <script language="javascript" 
          type="text/javascript" 
          src="${pageContext.request.contextPath}/scripts/upload_helper.js">&#160;</script>

  <div id='editor' style='width:100%; height:360px'>
    <h:outputText value="#{WizardManager.bean.content}" escape="false" />
  </div>
  <h:inputHidden id="editor-output" value="#{WizardManager.bean.content}" />
</jsp:root>
