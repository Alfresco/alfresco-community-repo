<%--
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
 * http://www.alfresco.com/legal/licensing"
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
