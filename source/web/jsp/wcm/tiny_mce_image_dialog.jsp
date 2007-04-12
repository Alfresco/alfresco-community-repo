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
	<title>{$lang_insert_image_title}</title>
      <script language="javascript" type="text/javascript" src="${pageContext.request.contextPath}/scripts/tiny_mce/tiny_mce_popup.js">&#160;</script>
      <script language="javascript" type="text/javascript" src="${pageContext.request.contextPath}/scripts/tiny_mce/utils/mctabs.js">&#160;</script>
      <script language="javascript" type="text/javascript" src="${pageContext.request.contextPath}/scripts/tiny_mce/utils/form_utils.js">&#160;</script>
      <script language="javascript" type="text/javascript" src="${pageContext.request.contextPath}/scripts/tiny_mce/themes/advanced/jscripts/image.js">&#160;</script>
      <script language="javascript" type="text/javascript" src="${pageContext.request.contextPath}/scripts/ajax/dojo/dojo.js">&#160;</script>
      <script language="javascript" type="text/javascript" src="${pageContext.request.contextPath}/scripts/ajax/ajax_helper.js">&#160;</script>
      <script language="javascript" type="text/javascript" src="${pageContext.request.contextPath}/scripts/ajax/file_picker_widget.js">&#160;</script>
      <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/xforms.css">&#160;</link>
      <script type="text/javascript">
        alfresco = tinyMCEPopup.windowOpener.alfresco;
        var alfFilePickerWidgetInstance;
        function loadPicker()
        {
        var d = document.getElementById("alfFilePicker");
        var pwOrigHeight = document.getElementById("panel_wrapper").offsetHeight;
        function resizeHandler(event)
        {
        document.getElementById("panel_wrapper").style.height =
        (d.offsetHeight >= 100 ? document.getElementById("panel_wrapper").offsetHeight + d.offsetHeight : pwOrigHeight) + "px";
        };
        function changeHandler(picker)
        {
        document.getElementById("src").value = picker.getValue();
        picker.node.style.display = "none";
        document.getElementById("src").style.display = "inline";
        document.getElementById("alfPickerTrigger").style.display = "inline";
        };
        alfFilePickerWidgetInstance = new alfresco.FilePickerWidget("alfFilePicker", d, "", false, changeHandler, resizeHandler)
        alfFilePickerWidgetInstance.render();
        alfFilePickerWidgetInstance.node.style.display = "none";
        //        widget._navigateToNode("/");
        }

        function showPicker()
        {
        alfFilePickerWidgetInstance.node.style.display = "block";
        document.getElementById("src").style.display = "none";
        document.getElementById("alfPickerTrigger").style.display = "none";
        alfFilePickerWidgetInstance._navigateToNode("/");
        }
      </script>
      
      <base target="_self" />
    </head>
    <body id="image" onload="tinyMCEPopup.executeOnLoad('init(); loadPicker();');" style="display: none">
      <form onsubmit="insertImage();return false;" action="#">
	<div class="tabs">
	  <ul>
	    <li id="general_tab" class="current"><span><a href="javascript:mcTabs.displayTab('general_tab','general_panel');" onmousedown="return false;">{$lang_insert_image_title}</a></span></li>
	  </ul>
	</div>

	<div class="panel_wrapper" id="panel_wrapper">
	  <div id="general_panel" class="panel current">
            <table border="0" cellpadding="4" cellspacing="0" width="100%">
              <tr>
                <td nowrap="nowrap"><label for="src">{$lang_insert_image_src}</label></td>
                <td width="100%">
                  <table width="100%" border="0" cellspacing="0" cellpadding="0">
                    <tr>
                      <td width="100%" nowrap="nowrap">
                        <input id="src" name="src" type="text" value="" style="width: 100%" onchange="getImageData();"/>
                      </td>
                      <td>
                        <input id="alfPickerTrigger" type="button" onclick="showPicker()" value="Browse Repository"/>
                      </td>
                      <td id="srcbrowsercontainer">&#160;</td>
                    </tr>
                    <tr>
                      <td colspan="3" width="100%">
                        <div id="alfFilePicker" style="width:100%"/>
                      </td>
                    </tr>
                  </table>
                </td>
              </tr>
	      <!-- Image list -->
	      <script type="text/javascript">
                <!--
		if (typeof(tinyMCEImageList) != "undefined" && tinyMCEImageList.length > 0) {
		var html = "";

		html += '<tr><td><label for="image_list">{$lang_image_list}</label></td>';
                html += '<td><select id="image_list" name="image_list" style="width: 200px" onchange="this.form.src.value=this.options[this.selectedIndex].value;resetImageData();getImageData();">';
                html += '<option value="">-" + "-" + "-</option>';

                for (var i=0; i<tinyMCEImageList.length; i++)
                html += '<option value="' + tinyMCEImageList[i][1] + '">' + tinyMCEImageList[i][0] + '</option>';

                html += '</select></td></tr>';
                      
                document.write(html);
                }
                  -->
              </script>
	      <!-- /Image list -->
              <tr>
                <td nowrap="nowrap"><label for="alt">{$lang_insert_image_alt}</label></td>
                <td><input id="alt" name="alt" type="text" value="" style="width: 200px"/></td>
              </tr>
              <tr>
                <td nowrap="nowrap"><label for="align">{$lang_insert_image_align}</label></td>
                <td><select id="align" name="align">
                    <option value="">{$lang_insert_image_align_default}</option>
                    <option value="baseline">{$lang_insert_image_align_baseline}</option>
                    <option value="top">{$lang_insert_image_align_top}</option>
                    <option value="middle">{$lang_insert_image_align_middle}</option>
                    <option value="bottom">{$lang_insert_image_align_bottom}</option>
                    <option value="texttop">{$lang_insert_image_align_texttop}</option>
                    <option value="absmiddle">{$lang_insert_image_align_absmiddle}</option>
                    <option value="absbottom">{$lang_insert_image_align_absbottom}</option>
                    <option value="left">{$lang_insert_image_align_left}</option>
                    <option value="right">{$lang_insert_image_align_right}</option>
                </select></td>
              </tr>
              <tr>
                <td nowrap="nowrap"><label for="width">{$lang_insert_image_dimensions}</label></td>
                <td><input id="width" name="width" type="text" value="" size="3" maxlength="3"/>
                    x
                    <input id="height" name="height" type="text" value="" size="3" maxlength="3"/></td>
              </tr>
              <tr>
                <td nowrap="nowrap"><label for="border">{$lang_insert_image_border}</label></td>
                <td><input id="border" name="border" type="text" value="" size="3" maxlength="3"/></td>
              </tr>
              <tr>
                <td nowrap="nowrap"><label for="vspace">{$lang_insert_image_vspace}</label></td>
                <td><input id="vspace" name="vspace" type="text" value="" size="3" maxlength="3"/></td>
              </tr>
              <tr>
                <td nowrap="nowrap"><label for="hspace">{$lang_insert_image_hspace}</label></td>
                <td><input id="hspace" name="hspace" type="text" value="" size="3" maxlength="3"/></td>
              </tr>
            </table>
	  </div>
	</div>

	<div class="mceActionPanel">
	  <div style="float: left">
	    <input type="button" id="insert" name="insert" value="{$lang_insert}" onclick="insertImage();" />
	  </div>

	  <div style="float: right">
	    <input type="button" id="cancel" name="cancel" value="{$lang_cancel}" onclick="tinyMCEPopup.close();" />
	  </div>
	</div>
      </form>
    </body>
  </html>
</jsp:root>
