<#--
   ADMIN TEMPLATE MACROS
-->
<#--
   Template outer "page" macro.
   
   @param title - Title msg for the page
   @param readonly (default:false) - boolean read only flag, if true will not display the Submit buttons.
   @param controller (default:"/admin") - optionally override the Form controller
   @param params (default:"") - url encoded params to be added to the HTML form URL
-->
<#macro page title readonly=false controller=DEFAULT_CONTROLLER!"/admin" params="" dialog=false>
<#assign FORM_ID="admin-jmx-form" />
<#if metadata??>
<#assign HOSTNAME>${msg("admin-console.host")}: ${metadata.hostname}</#assign>
<#assign HOSTADDR>${msg("admin-console.ipaddress")}: ${metadata.hostaddress}</#assign>
</#if>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
   <title>Alfresco &raquo; ${title?html}<#if metadata??> [${HOSTNAME} ${HOSTADDR}]</#if></title>
   <meta http-equiv="X-UA-Compatible" content="IE=Edge" />
   <link rel="stylesheet" type="text/css" href="${url.context}/css/reset.css" />
   <link rel="stylesheet" type="text/css" href="${url.context}/css/alfresco.css" />
   <link rel="stylesheet" type="text/css" href="${url.context}/admin/css/admin.css" />
   <!--[if IE 8 ]><style type="text/css">.dialog{width:100%}</style><![endif]-->
   <script type="text/javascript">//<![CDATA[

/* JavaScript global helper methods and event handlers */
var el = function el(id)
{
   return document.getElementById(id);
}

/* Admin namespace helper methods */
var Admin = Admin || {};
(function() {
   
   /* private scoped values */
   _dialog = null;
   _dialogScrollPosition = null;
   
   /* publicly accessable helper functions */
   
   /**
    * String trim helper
    * 
    * @param s {string}    String to trim pre and post whitespace from
    * @return trimmed string value - returns empty string for null or undefined input
    */
   Admin.trim = function trim(s)
   {
      return s ? s.replace(/^\s+|\s+$/g, "") : "";
   }
   
   /**
    * String HTML encoding helper
    * 
    * @param s {string}    String to HTML encode
    * @return encoded string value - returns empty string for null or undefined input
    */
   Admin.html = function html(s)
   {
      if (!s)
      {
         return "";
      }
      s = "" + s;
      return s.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#39;");
   }
   
   /**
    * Helper to add a named DOM event listener function to an object
    * 
    * @param obj {Element}    DOM object to add the named event listener too
    * @param event {string}   Event name e.g. "click"
    * @param fn {function}    Event handler function to invoke
    */
   Admin.addEventListener = function addEventListener(obj, event, fn)
   {
      if (obj.addEventListener)
      {
         obj.addEventListener(event, fn, false);
      }
      else
      {
         obj.attachEvent("on" + event, fn);
      }
   }
   
   /**
    * DIV Section toggle event handler
    * 
    * @param a {Element}   Anchor element that fired the toggle event
    */
   Admin.sectionToggle = function sectionToggle(a)
   {
      <#-- NOTE: dependent on template structure in @tsection macro below -->
      
      // walk the DOM nodes to get to the toggle div element
      var div = a.parentElement.parentElement.getElementsByTagName("div")[0].getElementsByTagName("div")[0];
      if (Admin.toggleHiddenElement(div))
      {
         // will now be open
         a.innerHTML = "\u25BC";
      }
      else
      {
         // will now be closed
         a.innerHTML = "\u25BA";
      }
   }
   
   /**
    * Toggle the "hidden" class for a given DOM element
    * 
    * @param el {Element}  Element to add/remove the "hidden" class to
    */
   Admin.toggleHiddenElement = function toggleHiddenElement(el)
   {
      var clazzes = el.className.split(" "),
          foundHidden = false;
      for (var i = 0; i < clazzes.length; i++)
      {
         // found the toggle el - switch display class
         if (foundHidden = (clazzes[i] === "hidden"))
         {
            clazzes.splice(i, 1);
            break;
         }
      }
      if (!foundHidden)
      {
         clazzes.push("hidden");
      }
      
      // apply new classes to the el
      el.className = clazzes.join(" ");
      
      return foundHidden;
   }
   
   Admin.addClass = function addClass(el, c)
   {
      var clazzes = el.className.split(" "), found = false;
      for (var i = 0; i < clazzes.length; i++)
      {
         if (found = (clazzes[i] === c)) break;
      }
      if (!found)
      {
         el.className += " " + c;
      }
   }
   
   Admin.removeClass = function removeClass(el, c)
   {
      var clazzes = el.className.split(" ");
      for (var i = 0; i < clazzes.length; i++)
      {
         if (clazzes[i] === c)
         {
            clazzes.splice(i, 1);
            break;
         }
      }
      el.className = clazzes.join(" ");
   }
   
   /**
    * Add a row to an existing table
    * 
    * @param table {Element}  The table DOM element
    * @param row   {Array}    Array of cell data to add
    * @param index {Integer}  Index to insert at - default is add to end
    */
   Admin.addTableRow = function addTableRow(table, row, index)
   {
      var tr = table.insertRow(index !== undefined ? index : -1);
      for (var i=0, td; i<row.length; i++)
      {
         td = tr.insertCell(-1);
         td.innerHTML = row[i];
      }
   }
   
   /**
    * Show the contents of a URL in a dialog styled IFrame
    * 
    * @param url  {String} URL in the same domain to display in the dialog frame
    */
   Admin.showDialog = function showDialog(url, compact)
   {
      if (!_dialog)
      {
         // generate IFrame for the dialog contents
         var iframe = document.createElement('iframe');
         iframe.name = iframe.id = 'admin-dialog';
         iframe.src = url;
         iframe.frameBorder = 0;
         iframe.className = (compact? 'dialog compact' : 'dialog');
         _dialog = iframe;
         
         // generate a shield over the background elements
         var shield = document.createElement('div');
         shield.id = "shield";
         shield.className = 'shield';
         
         // display the elements and hide background scrollbars
         document.body.appendChild(shield);
         document.body.appendChild(iframe);
         document.body.style.overflow = 'hidden';
         
         // scroll to top and record last Y scroll position
         _dialogScrollPosition = window.pageYOffset;
         window.scrollTo(0,0);
      }
   }
   
   /**
    * Remove any existing dialog frame and restore background elements
    */
   Admin.removeDialog = function removeDialog(state, args)
   {
      if (_dialog)
      {
         // remove the dialog IFrame and shield from the DOM
         _dialog.parentNode.removeChild(_dialog);
         var shield = el("shield");
         shield.parentNode.removeChild(shield);
         
         // restore background scrollbars
         document.body.style.overflow = "";
         
         // scroll back
         window.scrollTo(0, _dialogScrollPosition);
         
         _dialog = null;
         
         if (state)
         {
            switch (state)
            {
               case "saved":
                  Admin.ondialogFinished(args);
                  break;
            }
         }
      }
   }
   
   /**
    * Default ondialogFinished event handler with empty impl.
    * Dialog templates can override this to add additional processing.
    */
   Admin.ondialogFinished = function ondialogFinished(args)
   {
   }
   
   /**
    * Ajax request helper
    * 
    * @param config  {Object} Of the form:
    *                {
    *                   method: "GET|POST|PUT|DELETE",
    *                   url: "endpointurl",
    *                   data: {object to be posted},
    *                   requestContentType: "application/json",
    *                   responseContentType: "application/json",
    *                   fnSuccess: successHandler(response),
    *                   fnFailure: failureHandler(response)
    *                }
    */
   Admin.request = function request(config)
   {
      var req = new XMLHttpRequest();
      var data = config.data || {};
      if (req.overrideMimeType) req.overrideMimeType((config.responseContentType ? config.responseContentType : "application/json") + "; charset=utf-8");
      req.open(config.method ? config.method : "GET", config.url);
      req.setRequestHeader("Accept", config.requestContentType ? config.requestContentType : "application/json");
      req.onreadystatechange = function() {
         if (req.readyState === 4)
         {
            if (req.status === 200)
            {
               // success - call handler
               if (config.fnSuccess)
               {
                  var json;
                  try
                  {
                     json = !config.responseContentType || config.responseContentType === "application/json" ? JSON.parse(req.responseText) : null;
                  }
                  catch (e)
                  {
                     // Developer JSON response error (e.g. malformed response text)
                     alert(e + "\n" + req.status + "\n" + req.responseText);
                  }
                  config.fnSuccess.call(this, {
                     responseText: req.responseText,
                     responseStatus: req.status,
                     responseJSON: json
                  });
               }
            }
            else
            {
               // failure - call handler
               if (config.fnFailure)
               {
                  config.fnFailure.call(this, {
                     responseText: req.responseText,
                     responseStatus: req.status
                  });
               }
               else
               {
                  // default error handler
                  alert("${msg("admin-console.requesterror")}\n\n" + res.responseText + "\n\n" + res.responseStatus);
               }
            }
         }
      };
      if (config.method === "POST" || config.method === "PUT")
      {
         // TODO: support form url encoded
         req.send(JSON.stringify(data));
      }
      else
      {
         req.send(null);
      }
   }
   
   /**
    * Perform binary file upload to a given service URL. Uses hidden iframe technique to give
    * an Ajax like upload with support for earlier browser APIs.
    * 
    * @param fileId  ID of the File element to POST
    * @param url     URL of the service endpoint
    * @param successHandler   Success handler function - passed JSON object response as argument
    * @param failureHandler   Failure handler function - no arguments
    */
   Admin.uploadFile = function uploadFile(fileId, url, successHandler, failureHandler)
   {
      var file = el(fileId),
          ownerDocument = file.ownerDocument,
          pwindow = ownerDocument.defaultView || ownerDocument.parentWindow,
          iframe = ownerDocument.createElement("iframe");
      iframe.style.display = "none";
      iframe.name = "AdminUploadFrame";
      iframe.id = iframe.name;
      ownerDocument.body.appendChild(iframe);
      
      // target the frame properly in IE
      pwindow.frames[iframe.name].name = iframe.name;
      
      Admin.addEventListener(iframe, 'load', function() {
         var frame = document.getElementById(iframe.name);
         if (frame.contentDocument)
         {
            content = frame.contentDocument.body.textContent;
         }
         else if (frame.contentWindow)
         {
            content = frame.contentWindow.document.body.textContent;
         }
         try
         {
            if (successHandler)
            {
               var json = JSON.parse(content);
               successHandler.call(this, json);
            }
         }
         catch (e)
         {
            if (failureHandler)
            {
               failureHandler.call(this);
            }
         }
      });
      
      var form = ownerDocument.createElement("form");
      ownerDocument.body.appendChild(form);
      form.style.display = "none";
      form.method = "post";
      form.encoding = "multipart/form-data";
      form.enctype = "multipart/form-data";
      form.target = iframe.name;
      form.action = url;
      form.appendChild(file);
      form.submit();
   }
   
   /**
    * Switch an input field between test and password to show and hide the text.
    * 
    * @param id      {String} ID of the password field
    * @param button  {Element} The button that was clicked
    */
   Admin.togglePassword = function togglePassword(id, button)
   {
      var field = el(id);
      
      if(field.type === "password")
      {
         button.value = "${msg("admin-console.password.hide")?html}";
         field.type = "text";
      }
      else
      {
         button.value = "${msg("admin-console.password.show")?html}";
         field.type = "password";
      }
   }
   
})();

/* Page load handler */
Admin.addEventListener(window, 'load', function() {
   // get the root form element
   var form = el("${FORM_ID}");
   
   // ensure ENTER press in a Form field doesn't submit the Form
   Admin.addEventListener(form, 'keypress', function(e) {
      if (e.keyCode === 13)
      {
         e.preventDefault ? e.preventDefault() : event.returnValue = false;
      }
      return true;
   });
   
   // highlight first form input field
   var fields = form.getElementsByTagName("input");
   for (var i=0; i<fields.length; i++)
   {
      if (fields[i].type === "text" || fields[i].type === "textarea")
      {
         if (!fields[i].readOnly)
         {
            fields[i].focus();
            break;
         }
      }
   }
   
   // escape key handler to close dialog page
   Admin.addEventListener(document, 'keypress', function(e) {
      if (e.keyCode === 27)
      {
         top.window.Admin.removeDialog();
      }
   });
});

//]]></script>
</head>
<#if !dialog>
<body>
   <#--
       Template for a full page view
   -->
   <div class="sticky-wrapper">
      
      <div class="header">
         <span><a href="${url.serviceContext}${DEFAULT_CONTROLLER!"/admin"}">${msg("admin-console.header")}</a></span><#if metadata??><span class="meta">${HOSTNAME}</span><span class="meta">${HOSTADDR}</span></#if>
         <div style="float:right"><a href="http://docs.alfresco.com/5.0/concepts/ch-administering.html" target="_blank">${msg("admin-console.help")}</a></div>
      </div>
      
      <div class="navigation-wrapper">
         
         <div class="navigation">
            <#-- A console tool is defined as a member of the 'AdminConsole' WebScript family -->
<#local tool=""/>
<#if tools??>
            <ul>
   <#list tools as group>
      <#list group as tool>
         <#if tool_index = 0 && tool.group != ""></ul><h3>${tool.groupLabel}</h3><ul></#if>
               <li class="<#if tool.selected><#local tool=tool.uri/>selected</#if>"><a href="${url.serviceContext}${tool.uri}" class="tool-link" title="${tool.description?html}">${tool.label?html}</a></li>
      </#list>
   </#list>
</#if>
            </ul>
         </div>
         
         <div class="main-wrapper">
         
            <div class="title">
               <span class="logo"><img src="${url.context}/images/logo/logo.png" width="145" height="48" alt="" /></span>
               <span class="logo-separator">&nbsp;</span>
               <h1>${title?html}</h1>
            </div>
<#-- User information messages -->
<#if args.m??>
            <div class="message">
               ${.now?string("HH:mm:ss")} - ${msg(args.m)?html}
               <a href="#" onclick="this.parentElement.style.display='none';" title="${msg("admin-console.close")}">[X]</a>
            </div>
</#if>
<#if args.e??>
            <div class="message error">
               ${.now?string("HH:mm:ss")} - ${msg(args.e)?html}
               <a href="#" onclick="this.parentElement.style.display='none';" title="${msg("admin-console.close")}">[X]</a>
            </div>
</#if>
            <div class="main">
               <form id="${FORM_ID}" action="${url.serviceContext}${controller}?t=${tool?url}<#if params!="">&${params}</#if>" enctype="multipart/form-data" accept-charset="utf-8" method="post">
<#-- Template-specific markup -->
<#nested>

<#if !readonly>
                  <div class="submission buttons">
                     <input type="submit" value="${msg("admin-console.save")}" />
                     <input class="cancel" type="button" value="${msg("admin-console.cancel")}" onclick="location.href='${url.service}'" />
                  </div>
</#if>
               </form>
            </div>
            
         </div>
      
      </div>
      
      <div class="push"></div>
      
   </div>
   
   <div class="footer">
      Alfresco Software, Inc. &copy; 2005-2015 All rights reserved.
   </div>
   
<#else>
<body class="dialog-body">
   <#--
       Template for a dialog page view
   -->
   <div>
      
      <div class="navigation-wrapper">
         
         <div>
         
            <div class="title">
               <span class="logo"><img src="${url.context}/images/logo/logo.png" width="145" height="48" alt="" /></span>
               <span class="logo-separator">&nbsp;</span>
               <h1>${title?html}</h1>
            </div>
            <div class="main">
               <form id="${FORM_ID}" action="${url.serviceContext}/enterprise/admin/admin-dialog<#if params!="">&${params}</#if>" enctype="multipart/form-data" accept-charset="utf-8" method="post">
<#-- Template-specific markup -->
<#nested>
               </form>
            </div>
            
         </div>
      
      </div>
      
   </div>
</#if>
</body>
</html>
</#macro>

<#macro dialogbuttons save=false close=true>
   <div class="buttons">
<#-- Template-specific markup -->
<#nested>
      <#if save><input type="submit" value="${msg("admin-console.save")}" /></#if>
      <#if close><input class="cancel" type="button" value="${msg("admin-console.close")}" onclick="top.window.Admin.removeDialog();" /></#if>
   </div>
</#macro>

<#--
   Template section macros.
-->
<#macro section label>
   <h2>${label?html}</h2>
   <div class="section">
<#nested>
   </div>
</#macro>
<#macro tsection label closed=true>
   <div>
      <h2>${label?html} <a class="action toggler" href="#" onclick="Admin.sectionToggle(this);return false;"><#if closed>&#x25BA;<#else>&#x25BC;</#if></a></h2>
      <div class="section">
         <div class="toggle <#if closed>hidden</#if>">
<#nested>
         </div>
      </div>
   </div>
</#macro>

<#--
   Template field macros and value conversion.
-->
<#function cvalue type value="">
   <#switch type>
      <#case "java.util.Date">
         <#if value?has_content>
            <#return value?datetime>
         <#else>
            <#return value>
         </#if>
         <#break>
      <#case "boolean">
         <#return value?string>
         <#break>
      <#case "java.lang.Long">
         <#return value>
         <#break>
      <#default>
         <#return value>
   </#switch>
</#function>

<#macro control attribute>
   <#if attribute.readonly>
      <@attrfield attribute=attribute />
   <#else>
      <#switch attribute.type>
         <#case "java.util.Date">
            <@attrtext attribute=attribute />
            <#break>
         <#case "boolean">
            <@attrcheckbox attribute=attribute />
            <#break>
         <#case "java.lang.Long">
            <@attrtext attribute=attribute />
            <#break>
         <#default>
            <@attrtext attribute=attribute />
      </#switch>
   </#if>
</#macro>

<#-- Hidden field -->
<#macro hidden name value="" id="">
   <input type="hidden" <#if id?has_content>id="${id?html}"</#if> name="${name?html}" value="${value?html}" />
</#macro>
<#macro attrhidden attribute name=attribute.qname id="">
   <@hidden name=name value=cvalue(attribute.type, attribute.value) id=id />
</#macro>

<#-- Label and simple read-only field -->
<#macro field label="" description="" value="" style="">
   <div class="control field"<#if style?has_content> style="${style?html}"</#if>>
      <#if label?has_content><span class="label">${label?html}:</span></#if>
      <#if value?has_content><span class="value">${value?html}</span></#if>
      <#if description?has_content><span class="description">${description?html}</span></#if>
      <#nested>
   </div>
</#macro>
<#macro attrfield attribute label=attribute.name description="" style="">
   <@field label=label description=description value=cvalue(attribute.type, attribute.value) style=style>
      <#nested>
   </@field>
</#macro>

<#-- Label and text input field -->
<#macro text name label="" description="" value="" maxlength=255 id="" style="" controlStyle="" valueStyle="" placeholder="" escape=true>
   <div class="control text"<#if style?has_content> style="${style?html}"</#if>>
      <#if label?has_content><span class="label">${label?html}:</span></#if>
      <span class="value"<#if valueStyle?has_content> style="${valueStyle?html}"</#if>><input <#if id?has_content>id="${id?html}"</#if> name="${name?html}" value="${value?html}" maxlength="${maxlength?c}" tabindex="0" <#if placeholder?has_content>placeholder="${placeholder?html}"</#if> <#if controlStyle?has_content>style="${controlStyle?html}"</#if>/></span>
      <#if description?has_content><span class="description"><#if escape>${description?html}<#else>${description}</#if></span></#if>
   </div>
</#macro>
<#macro attrtext attribute label=attribute.name description="" maxlength=255 id="" style="" controlStyle="" valueStyle="" placeholder="" escape=true>
   <@text name=attribute.qname label=label description=description value=cvalue(attribute.type, attribute.value) maxlength=maxlength id=id style=style controlStyle=controlStyle valueStyle=valueStyle placeholder=placeholder escape=escape />
</#macro>

<#-- Label and password input field -->
<#macro password id name label="" description="" value="" maxlength=255 style="" controlStyle="" visibilitytoggle=false>
   <div class="control text password"<#if style?has_content> style="${style?html}"</#if>>
      <#if label?has_content><span class="label">${label?html}:</span></#if>
      <span class="value"><input id="${id?html}" name="${name?html}" value="${value?html}" maxlength="${maxlength?c}" type="password" tabindex="0" <#if controlStyle?has_content>style="${controlStyle?html}"</#if>/></span>
      <#if visibilitytoggle><@button label=msg("admin-console.password.show")?html onclick="Admin.togglePassword('${id?html}', this);" /></#if>
      <#if description?has_content><span class="description">${description?html}</span></#if>
   </div>
</#macro>
<#macro attrpassword attribute label=attribute.name id=attribute.qname description="" maxlength=255 style="" controlStyle="" visibilitytoggle=false populatevalue=false>
   <#if populatevalue>
   <@password name=attribute.qname label=label id=id description=description value=cvalue(attribute.type, attribute.value) maxlength=maxlength style=style controlStyle=controlStyle visibilitytoggle=visibilitytoggle />
   <#else>
   <@password name=attribute.qname label=label id=id description=description maxlength=maxlength style=style controlStyle=controlStyle visibilitytoggle=visibilitytoggle />
   </#if>
</#macro>

<#-- Label and text area field -->
<#macro textarea name label="" description="" value="" maxlength=255 id="" style="" controlStyle="">
   <div class="control textarea"<#if style?has_content> style="${style?html}"</#if>>
      <#if label?has_content><span class="label">${label?html}:</span></#if>
      <span class="value"><textarea <#if id?has_content>id="${id?html}"</#if> name="${name?html}" maxlength="${maxlength?c}" tabindex="0" <#if controlStyle?has_content>style="${controlStyle?html}"</#if>>${value?html}</textarea></span>
      <#if description?has_content><span class="description">${description?html}</span></#if>
   </div>
</#macro>
<#macro attrtextarea attribute label=attribute.name description="" maxlength=255 id="" style="" controlStyle="">
   <@textarea name=attribute.qname label=label description=description value=cvalue(attribute.type, attribute.value) maxlength=maxlength id=id style=style controlStyle=controlStyle />
</#macro>

<#-- Label and checkbox boolean field -->
<#macro checkbox name label description="" value="false" id="" style="" controlStyle="">
   <div class="control checkbox"<#if style?has_content> style="${style?html}"</#if>>
      <span class="label">${label?html}:</span>
      <span class="value">
         <input <#if id?has_content>id="${id?html}"</#if> name="" onchange="el('${name?html}').value = (this.checked ? 'true' : 'false');" type="checkbox" <#if value="true">checked="checked"</#if> tabindex="0" <#if controlStyle?has_content>style="${controlStyle?html}"</#if>/>
         <input id="${name?html}" name="${name?html}" type="hidden" value="<#if value="true">true<#else>false</#if>" />
      </span>
      <#if description?has_content><span class="description">${description?html}</span></#if>
      <#nested>
   </div>
</#macro>
<#macro attrcheckbox attribute label=attribute.name description="" id="" style="" controlStyle="">
   <@checkbox name=attribute.qname label=label description=description value=cvalue(attribute.type, attribute.value) id=id style=style controlStyle=controlStyle>
   <#nested>
   </@checkbox>
</#macro>

<#-- Status read-only boolean field -->
<#macro status label description="" value="false" style="">
   <#if value != "">
      <#if value="true"><#local tooltip=msg("admin-console.enabled")?html><#else><#local tooltip=msg("admin-console.disabled")?html></#if>
      <div class="control status"<#if style?has_content> style="${style?html}"</#if>>
         <span class="label">${label?html}:</span>
         <span class="value">
            <img src="${url.context}/admin/images/<#if value="true">enabled<#else>disabled</#if>.gif" width="16" height="16" alt="${tooltip}" title="${tooltip}" />
            <span>${tooltip}</span>
         </span>
         <#if description?has_content><span class="description">${description?html}</span></#if>
      </div>
   <#else>
      <div class="control status"<#if style?has_content> style="${style?html}"</#if>>
         <span class="label">${label?html}:</span>
         <span class="value">
            <span>${msg("admin-console.unavailable")}</span>
         </span>
         <#if description?has_content><span class="description">${description?html}</span></#if>
      </div>
   </#if>
</#macro>
<#macro attrstatus attribute="" label=attribute.name description="" style="">
   <#-- Special handling for missing attribute - as some JMX objects can be temporarily unavailable -->
   <#if attribute?has_content>
      <@status label=label description=description value=cvalue(attribute.type, attribute.value) style=style />
   <#else>
      <@status label=label description=description value="" style=style />
   </#if>
</#macro>

<#-- Label and Options Drop-Down list -->
<#macro options name label="" description="" value="" id="" style="" valueStyle="" onchange="" onclick="" escape=true>
   <div class="control options"<#if style?has_content> style="${style?html}"</#if>>
      <#if label?has_content><span class="label">${label?html}:</span></#if>
      <span class="value"<#if valueStyle?has_content> style="${valueStyle?html}"</#if>>
         <select <#if id?has_content>id="${id?html}"</#if> name="${name?html}" tabindex="0"<#if onchange?has_content> onchange="${onchange?html}"</#if><#if onclick?has_content> onclick="${onclick?html}"</#if>>
<#assign _value=value>
<#nested>
         </select>
      </span>
      <#if description?has_content><span class="description"><#if escape>${description?html}<#else>${description}</#if></span></#if>
   </div>
</#macro>
<#macro option label value>
            <option value="${value?html}" <#if value=_value>selected="selected"</#if>>${label?html}</option>
</#macro>
<#macro attroptions attribute label=attribute.name description="" id="" style="" valueStyle="" onchange="" escape=true>
   <@options name=attribute.qname label=label description=description value=cvalue(attribute.type, attribute.value) id=id style=style valueStyle="" onchange="" escape=escape>
      <#nested>
   </@options>
</#macro>

<#-- Label and Radio Button list -->
<#macro radios name label="" description="" value="" style="">
   <div class="control radio"<#if style?has_content> style="${style?html}"</#if>>
      <#if label?has_content><span class="label">${label?html}:</span></#if>
      <span class="value">
<#assign _name=name>
<#assign _value=value>
<#nested>
      </span>
      <#if description?has_content><span class="description">${description?html}</span></#if>
   </div>
</#macro>
<#macro radio label value id="">
         <div class="radiovalue">
            <input <#if id?has_content>id="${id?html}"</#if> type="radio" name="${_name?html}" value="${value?html}" <#if value=_value>checked="checked"</#if> tabindex="0" />
            <span class="radiolabel">${label?html}</span>
         </div>
</#macro>

<#-- Ordered and Unordered list of values -->
<#macro list listtype label="" description="" value="" style="">
   <div class="control list"<#if style?has_content> style="${style?html}"</#if>>
      <#if label?has_content><span class="label">${label?html}:</span></#if>
      <${listtype?html}>
   <#list value?split(",") as x>
         <li>${x?html}</li>
   </#list>
      </${listtype?html}>
      <#if description?has_content><span class="description">${description?html}</span></#if>
   </div>
</#macro>
<#macro ulist label="" description="" value="" style="">
   <@list listtype="ul" label=label description=description value=value style=style />
</#macro>
<#macro olist label="" description="" value="" style="">
   <@list listtype="ol" label=label description=description value=value style=style />
</#macro>
<#macro attrulist attribute label=attribute.name description="" style="">
   <@ulist label=label description=description value=cvalue(attribute.type, attribute.value) style=style />
</#macro>
<#macro attrolist attribute label=attribute.name description="" style="">
   <@olist label=label description=description value=cvalue(attribute.type, attribute.value) style=style />
</#macro>

<#-- Simple button with JavaScript event handler -->
<#macro button label description="" onclick="" style="" id="" class="" disabled="false">
   <input class="<#if class?has_content>${class?html}<#else>inline</#if>" <#if id?has_content>id="${id?html}"</#if> <#if style?has_content>style="${style?html}"</#if> type="button" value="${label?html}" onclick="${onclick?html}" <#if disabled="true">disabled="true"</#if> />
   <#if description?has_content><span class="description">${description?html}</span></#if>
</#macro>
