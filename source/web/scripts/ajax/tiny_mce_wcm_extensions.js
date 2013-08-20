/*
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
if (!alfresco.log)
{
  alfresco.log = alfresco.constants.DEBUG ? log : Class.empty;
}

//MNT-2080: AVM_WEBAPP url rendering in the html view of TinyMCE editor
tinyMCE.avmWebappUrl = alfresco.constants.AVM_WEBAPP_URL;

function alfresco_TinyMCE_urlconverter_callback(href, element, onsave)
{
  var result = null;
  
  alfresco.log("element = " + element);
  
  // NOTE: since upgrade of TinyMCE to v3 'onsave' now seems to always = true 
  
  if (href)
  {
    if (href.startsWith(alfresco.constants.AVM_WEBAPP_PREFIX))
    {
      //do nothin. AVM_WEBAPP_URL will be removed during saving image in _tinyMCE_blurHandler
      result = href;
    }
    else if (href.startsWith("/"))
    {
    	result = alfresco.constants.AVM_WEBAPP_URL + href;
    }
    else if (href.startsWith(document.location.href))
    { 
      result = href.substring(document.location.href.length);
    }
    else
    {
      result = href;
    }
  
    // handle URL issue with IE (WCM-1134)
    if (navigator.appName == "Microsoft Internet Explorer" || tinyMCE.isMSIE)
    {
      var server = document.location.protocol + "//" + document.location.host;
      if (href.startsWith(server))
      {
        result = href.substring(server.length);
      }
    }
  }
  
  alfresco.log("alfresco_TinyMCE_urlconverter_callback('" + href + "', ... , " + onsave + ") = " + result);
  
  return result;
}

function alfresco_TinyMCE_file_browser_callback(field_name, url, type, win)
{
  // remove url prefix ready for picker
  if (url.startsWith(alfresco.constants.AVM_WEBAPP_URL))
  {
    url = url.replace(alfresco.constants.AVM_WEBAPP_URL, ""); 
  }
  else
  {
    url = url.replace(alfresco.constants.AVM_WEBAPP_CONTEXT, ""); 
  }
  
  //tinyMCE.loadCSS doesn't seem to work with plugins so add css manually
  //tinyMCE.activeEditor.dom.loadCSS(alfresco.constants.WEBAPP_CONTEXT + "/css/xforms.css");
  var headEl = win.document.getElementsByTagName("head")[0];         
  var cssEl = win.document.createElement('link');
  cssEl.type = 'text/css';
  cssEl.rel = 'stylesheet';
  cssEl.href = alfresco.constants.WEBAPP_CONTEXT + "/css/xforms.css";
  cssEl.media = 'screen';
  headEl.appendChild(cssEl);
  
  // ALF-872:
  // Drop-down and list boxes do not have a z-index property, these are window level controls. 
  // When you want to show a div in a page that contains these controls, you will face an overlapping problem.
  // This is a well-known problem with the IE 6 browser.
  // To solve this we just hiding form's divs until FilePickerWidget is undestroyed.

  if (window.ie6)
  {
    var divs = win.document.getElementsByTagName("div");        
    for (var i = 0; i < divs.length; i++)
    {    
      divs[i].style.visibility = "hidden";
    }                                                                                    
  }
  
  var div = win.document.createElement("div");
  div.style.width = "100%";
  div.style.height = "100%";
  div.style.backgroundColor = "white";
  div.style.position = "absolute";
  div.style.top = "0px";
  div.style.left = "0px";
  win.document.body.appendChild(div);
  var pickerDiv = win.document.createElement("div");
  pickerDiv.style.height = "100%";
  pickerDiv.style.position = "relative";
  div.appendChild(pickerDiv);
  var picker = new alfresco.FilePickerWidget("alfFilePicker", 
                                             pickerDiv, 
                                             url, 
                                             false, 
                                             function(picker)
                                             {
                                               win.document.getElementById(field_name).value = picker.getValue();
                                               picker.destroy();
                                               div.parentNode.removeChild(div);
                                             },  
                                             function()
                                             {
                                               picker.destroy();
                                               
                                               // Please see comment above
                                               if (window.ie6)
                                               {
                                                 var divs = win.document.getElementsByTagName("div");       
                                                 for (var i = 0; i < divs.length; i++)
                                                 {    
                                                   divs[i].style.visibility = "visible";
                                                 }                                                                                         
                                               }
                                               
                                               div.parentNode.removeChild(div);
                                             },
                                             function(picker)
                                             {
                                               picker.node.style.height = div.offsetHeight + "px";
                                               picker.node.style.width = div.offsetWidth + "px";
                                             }, 
                                             type == "image" ? ["wcm:avmcontent"] : [], 
                                             type == "image" ? ["image/*"] : []);
  picker._navigateToNode(url);
}
