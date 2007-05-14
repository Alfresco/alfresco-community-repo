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
    * As a special exception to the terms and conditions of version 2.0 of
    * the GPL, you may redistribute this Program in connection with Free/Libre
    * and Open Source Software ("FLOSS") applications as described in Alfresco's
    * FLOSS exception.  You should have recieved a copy of the text describing
    * the FLOSS exception, and it is also available here:
    * http://www.alfresco.com/legal/licensing

    Produces the index page for the press release page.
  -->
<jsp:root version="1.2"
          xmlns:jsp="http://java.sun.com/JSP/Page"
 	  xmlns:c="http://java.sun.com/jsp/jstl/core"
	  xmlns:pr="http://www.alfresco.org/alfresco/pr"
          xmlns:fmt="http://java.sun.com/jsp/jstl/fmt">

  <jsp:output doctype-root-element="html"
	      doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
	      doctype-system="http://www.w3c.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"/>

  <jsp:directive.page language="java" contentType="text/html; charset=UTF-8"/>
  <jsp:directive.page isELIgnored="false"/>
  <html xmlns="http://www.w3.org/1999/xhtml">
    <head>
      <title>{$lang_insert_link_title}</title>
      <link rel="stylesheet" 
            type="text/css" 
            href="${pageContext.request.contextPath}/css/xforms.css">&#160;</link>
      <script language="javascript" 
              type="text/javascript" 
              src="${pageContext.request.contextPath}/scripts/tiny_mce/tiny_mce_popup.js">&#160;</script>
      <script language="javascript" 
              type="text/javascript" 
              src="${pageContext.request.contextPath}/scripts/tiny_mce/utils/mctabs.js">&#160;</script>
      <script language="javascript" 
              type="text/javascript" 
              src="${pageContext.request.contextPath}/scripts/tiny_mce/utils/form_utils.js">&#160;</script>
      <script language="javascript" 
              type="text/javascript" 
              src="${pageContext.request.contextPath}/scripts/tiny_mce/themes/advanced/jscripts/link.js">&#160;</script>
      <script language="javascript" 
              type="text/javascript" 
              src="${pageContext.request.contextPath}/scripts/ajax/dojo/dojo.js">&#160;</script>
      <script type="text/javascript">
        var alfresco = {};
        alfresco.constants = tinyMCEPopup.windowOpener.alfresco.constants;
        alfresco.resources = tinyMCEPopup.windowOpener.alfresco.resources;
      </script>
      <script language="javascript" 
              type="text/javascript" 
              src="${pageContext.request.contextPath}/scripts/ajax/common.js">&#160;</script>
      <script language="javascript" 
              type="text/javascript" 
              src="${pageContext.request.contextPath}/scripts/ajax/ajax_helper.js">&#160;</script>
      <script language="javascript" 
              type="text/javascript" 
              src="${pageContext.request.contextPath}/scripts/upload_helper.js">&#160;</script>
      <script language="javascript" 
              type="text/javascript" 
              src="${pageContext.request.contextPath}/scripts/ajax/file_picker_widget.js">&#160;</script>
      <script type="text/javascript">
        var alfFilePickerWidgetInstance;

        function loadPicker()
        {
          var d = document.getElementById("alfFilePicker");
          var pwOrigHeight = document.getElementById("panel_wrapper").offsetHeight;
          function resizeHandler(event)
          {
            document.getElementById("panel_wrapper").style.height =
             (d.offsetHeight >= 100 
              ? document.getElementById("panel_wrapper").offsetHeight + d.offsetHeight 
              : pwOrigHeight) + "px";
          };
          function changeHandler(picker)
          {
            document.getElementById("href").value = picker.getValue();
          };
          alfFilePickerWidgetInstance = new alfresco.FilePickerWidget("alfFilePicker", d, "", false, changeHandler, resizeHandler)
          alfFilePickerWidgetInstance.setValue(document.getElementById("href").value);
          alfFilePickerWidgetInstance.render();
        }
        setTimeout("loadPicker();", 500);
      </script>
      <base target="_self" />
    </head>
    <body id="link" onload="tinyMCEPopup.executeOnLoad('init();');" style="display: none">
      <form onsubmit="insertLink();return false;" action="#">
        <div class="tabs">
          <ul>
            <li id="general_tab" class="current"><span><a href="javascript:mcTabs.displayTab('general_tab','general_panel');" onmousedown="return false;">{$lang_insert_link_title}</a></span></li>
          </ul>
        </div>

        <div class="panel_wrapper" id="panel_wrapper">
          <div id="general_panel" class="panel current" style="width: 100%">
            <table border="0" cellpadding="4" cellspacing="0" width="100%">
              <tr>
                <td nowrap="nowrap"><label for="href">{$lang_insert_link_url}</label></td>
                <td width="100%" nowrap="nowrap">
                  <input id="href" name="href" type="hidden" value=""/>
                  <div id="alfFilePicker" style="width: 100%; height: 100%;"/>
                </td>
                <td colspan="0" id="hrefbrowsercontainer"/>
              </tr>
              <!-- Link list -->
              <script type="text/javascript">
                <!--
                if (typeof(tinyMCELinkList) != "undefined" && tinyMCELinkList.length > 0) 
                {
                var html = "";
                
                html += "<tr><td><label for='link_list'>{$lang_link_list}</label></td>";
                html += "<td><select id='link_list' name='link_list' style='width: 200px' " +
                        "onchange='this.form.href.value=this.options[this.selectedIndex].value;'>";
                html += "<option value=''>-" + "-" + "-</option>";
                for (var i = 0; i < tinyMCELinkList.length; i++)
                {
                  html += "<option value='" + tinyMCELinkList[i][1] + "'>" + tinyMCELinkList[i][0] + "</option>";
                }
                html += "</select></td></tr>";
                document.write(html);
                }
                -->
              </script>
              <!-- /Link list -->
              <tr>
                <td nowrap="nowrap"><label for="target">{$lang_insert_link_target}</label></td>
                <td><select id="target" name="target" style="width: 200px">
                    <option value="_self">{$lang_insert_link_target_same}</option>
                    <option value="_blank">{$lang_insert_link_target_blank}</option>
                    <script language="javascript">
                      <!--
                          var html = "";
                          var targets = tinyMCE.getParam("theme_advanced_link_targets", "").split(";");

                          for (var i = 0; i < targets.length; i++) 
                          {
                          var key, value;
                          if (targets[i] == "")
                          continue;
                          key = targets[i].split("=")[0];
                          value = targets[i].split("=")[1];
                          html += "<option value='" + value + "'>" + key + "</option>";
                          }
                          document.write(html);
                        -->
                    </script>
                </select></td>
              </tr>
              <tr>
                <td nowrap="nowrap"><label for="linktitle">{$lang_theme_insert_link_titlefield}</label></td>
                <td><input id="linktitle" name="linktitle" type="text" value="" style="width: 200px"/></td>
              </tr>
              <tr id="styleSelectRow">
                <td><label for="styleSelect">{$lang_class_name}</label></td>
                <td>
                  <select id="styleSelect" name="styleSelect">
                    <option value="" selected="selected">{$lang_theme_style_select}</option>
                  </select>
                </td>
              </tr>
            </table>
          </div>
        </div>
        <div class="mceActionPanel">
          <div style="float: left">
            <input type="button" id="insert" name="insert" value="{$lang_insert}" onclick="insertLink();" />
          </div>

          <div style="float: right">
            <input type="button" id="cancel" name="cancel" value="{$lang_cancel}" onclick="tinyMCEPopup.close();" />
          </div>
        </div>
      </form>
    </body>
  </html>
</jsp:root>
