<%--
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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
--%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>
<%@ page isELIgnored="false" %>

<f:verbatim>
<script language="javascript" type="text/javascript" src="<%=request.getContextPath()%>/scripts/tiny_mce/tiny_mce.js"></script>
<script language="javascript" type="text/javascript">
   var lang = "${UserPreferencesBean.language}";
   lang = lang.substring(0,lang.indexOf("_"));

   <%-- Init the Tiny MCE in-line HTML editor --%>
   tinyMCE.init({
      theme : "advanced",
      language : lang,
      mode : "exact",
      relative_urls: false,
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
      return content;
   }
   
   var isIE = (document.all);
   
</script>

<div id='editor' style='width:100%; height:360px'>
   </f:verbatim>
   <h:outputText value="#{WizardManager.bean.content}" escape="false" />
   <f:verbatim>
</div>
</f:verbatim>
<h:inputHidden id="editor-output" value="#{WizardManager.bean.content}" />
