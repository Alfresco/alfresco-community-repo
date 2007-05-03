/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
////////////////////////////////////////////////////////////////////////////////
// TinyMCE extensions for wcm
//
// This script provides callbacks used for overriding the default image and
// link dialogs for presenting a file picker to browse the repository.
//
// This script requires tiny_mce.js, and some alfresco.constants to be
// loaded in advance.
////////////////////////////////////////////////////////////////////////////////

function alfresco_TinyMCE_urlconverter_callback(href, element, onsave)
{
//  dojo.debug("request to convert " + href + " onsave = " + onsave);
  if (onsave)
  {
    return (href && href.startsWith(alfresco.constants.AVM_WEBAPP_URL)
            ? href.substring(alfresco.constants.AVM_WEBAPP_URL.length)
            : href);
  }
  else
  {
    return (href && href.startsWith("/")
            ? alfresco.constants.AVM_WEBAPP_URL + href
            : href);
  }
}

function alfresco_TinyMCE_execcommand_callback(editor_id, elm, command, user_interface, value)
{
  if (command == "mceLink")
  {
    // BEGIN COPIED FROM ADVANCED THEME editor_template_src.js
    var inst = tinyMCE.getInstanceById(editor_id);
    var doc = inst.getDoc();
    var selectedText = (tinyMCE.isMSIE 
                        ? doc.selection.createRange().text
                        : inst.getSel().toString());

    if (!tinyMCE.linkElement &&
        tinyMCE.selectedElement.nodeName.toLowerCase() != "img" && 
        selectedText.length <= 0)
    {
      return true;
    }

    var href = "", target = "", title = "", onclick = "", action = "insert", style_class = "";

    if (tinyMCE.selectedElement.nodeName.toLowerCase() == "a")
    {
      tinyMCE.linkElement = tinyMCE.selectedElement;
    }

    // Is anchor not a link
    if (tinyMCE.linkElement != null && tinyMCE.getAttrib(tinyMCE.linkElement, 'href') == "")
    {
      tinyMCE.linkElement = null;
    }

    if (tinyMCE.linkElement) 
    {
      href = tinyMCE.getAttrib(tinyMCE.linkElement, 'href');
      target = tinyMCE.getAttrib(tinyMCE.linkElement, 'target');
      title = tinyMCE.getAttrib(tinyMCE.linkElement, 'title');
      onclick = tinyMCE.getAttrib(tinyMCE.linkElement, 'onclick');
      style_class = tinyMCE.getAttrib(tinyMCE.linkElement, 'class');

      // Try old onclick to if copy/pasted content
      if (onclick == "")
      {
        onclick = tinyMCE.getAttrib(tinyMCE.linkElement, 'onclick');
      }
      onclick = tinyMCE.cleanupEventStr(onclick);

      href = eval(tinyMCE.settings['urlconverter_callback'] + "(href, tinyMCE.linkElement, true);");

      // Use mce_href if defined
      mceRealHref = tinyMCE.getAttrib(tinyMCE.linkElement, 'mce_href');
      if (mceRealHref != "") 
      {
        href = mceRealHref;
        if (tinyMCE.getParam('convert_urls'))
        {
          href = eval(tinyMCE.settings['urlconverter_callback'] + "(href, tinyMCE.linkElement, true);");
        }
      }

      action = "update";
    }

    var window_props = { file: alfresco.constants.WEBAPP_CONTEXT + "/jsp/wcm/tiny_mce_link_dialog.jsp",
                         width: 510 + tinyMCE.getLang('lang_insert_link_delta_width', 0),
                         height: 400 + tinyMCE.getLang('lang_insert_link_delta_height', 0) };
    var dialog_props = { href: href, 
                         target: target, 
                         title: title, 
                         onclick: onclick, 
                         action: action, 
                         className: style_class, 
                         inline: "yes" };
    tinyMCE.openWindow(window_props, dialog_props);
    return true;
  }
  else if (command == "mceImage")
  {
    var src = "", alt = "", border = "", hspace = "", vspace = "", width = "", height = "", align = "",
      title = "", onmouseover = "", onmouseout = "", action = "insert";
    var img = tinyMCE.imgElement;
    var inst = tinyMCE.getInstanceById(editor_id);

    if (tinyMCE.selectedElement != null && tinyMCE.selectedElement.nodeName.toLowerCase() == "img") 
    {
      img = tinyMCE.selectedElement;
      tinyMCE.imgElement = img;
    }

    if (img) 
    {
      // Is it a internal MCE visual aid image, then skip this one.
      if (tinyMCE.getAttrib(img, 'name').indexOf('mce_') == 0)
      {
        return true;
      }

      src = tinyMCE.getAttrib(img, 'src');
      alt = tinyMCE.getAttrib(img, 'alt');

      // Try polling out the title
      if (alt == "")
      {
        alt = tinyMCE.getAttrib(img, 'title');
      }

      // Fix width/height attributes if the styles is specified
      if (tinyMCE.isGecko) 
      {
        var w = img.style.width;
        if (w != null && w.length != 0)
        {
          img.setAttribute("width", w);
        }

        var h = img.style.height;
        if (h != null && h.length != 0)
        {
          img.setAttribute("height", h);
        }
      }

      border = tinyMCE.getAttrib(img, 'border');
      hspace = tinyMCE.getAttrib(img, 'hspace');
      vspace = tinyMCE.getAttrib(img, 'vspace');
      width = tinyMCE.getAttrib(img, 'width');
      height = tinyMCE.getAttrib(img, 'height');
      align = tinyMCE.getAttrib(img, 'align');
      onmouseover = tinyMCE.getAttrib(img, 'onmouseover');
      onmouseout = tinyMCE.getAttrib(img, 'onmouseout');
      title = tinyMCE.getAttrib(img, 'title');

      // Is realy specified?
      if (tinyMCE.isMSIE) 
      {
        width = img.attributes['width'].specified ? width : "";
        height = img.attributes['height'].specified ? height : "";
      }

      //onmouseover = tinyMCE.getImageSrc(tinyMCE.cleanupEventStr(onmouseover));
      //onmouseout = tinyMCE.getImageSrc(tinyMCE.cleanupEventStr(onmouseout));

      src = eval(tinyMCE.settings['urlconverter_callback'] + "(src, img, true);");

      // Use mce_src if defined
      mceRealSrc = tinyMCE.getAttrib(img, 'mce_src');
      if (mceRealSrc != "") 
      {
        src = mceRealSrc;

        if (tinyMCE.getParam('convert_urls'))
        {
          src = eval(tinyMCE.settings['urlconverter_callback'] + "(src, img, true);");
        }
      }
      action = "update";
    }

    var window_props = { file: alfresco.constants.WEBAPP_CONTEXT + "/jsp/wcm/tiny_mce_image_dialog.jsp",
                         width: 510 + tinyMCE.getLang('lang_insert_image_delta_width', 0),
                         height: 400 + (tinyMCE.isMSIE ? 25 : 0) + tinyMCE.getLang('lang_insert_image_delta_height', 0) };
    var dialog_props = { src: src, 
                         alt: alt, 
                         border: border, 
                         hspace: hspace, 
                         vspace: vspace, 
                         width: width, 
                         height: height, 
                         align: align, 
                         title: title,
                         onmouseover: onmouseover,
                         onmouseout: onmouseout, 
                         action: action,
                         inline: "yes" };
    tinyMCE.openWindow(window_props, dialog_props);
    return true;
  }
  else
  {
    return false;
  }
}
