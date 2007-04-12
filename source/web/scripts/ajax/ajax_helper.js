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
// Ajax helper library
//
// This script manages ajax requests and provides a wrapper around the dojo 
// library.
//
// This script requires dojo.js to be loaded in advance.
////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
// AJAX helper
////////////////////////////////////////////////////////////////////////////////

alfresco = typeof alfresco == "undefined" ? {} : alfresco;
alfresco.constants = typeof alfresco.constants == "undefined" ? {} : alfresco.constants;
alfresco.constants.AJAX_LOADER_DIV_ID = "alfresco-ajax-loader";

alfresco.AjaxHelper = function()
{
}

/** Creates an ajax request object. */
alfresco.AjaxHelper.createRequest = function(target, serverMethod, methodArgs, load, error)
{
  var result = new dojo.io.Request(alfresco.constants.WEBAPP_CONTEXT + 
                                   "/ajax/invoke/XFormsBean." + serverMethod, 
                                   "text/xml");
  result.target = target;
  result.content = methodArgs;
  result.method = "POST";
  result.load = load;
  dojo.event.connect(result, "load", function(type, data, evt)
                     {
                       alfresco.AjaxHelper._loadHandler(result);
                     });
  result.error = error || function(type, e, impl)
    {
      dojo.debug("error [" + type + "] " + e.message);
      if (impl.status == 401)
      {
        document.getElementById("logout").onclick();
      }
      else
      {
        _show_error(document.createTextNode(e.message));
        alfresco.AjaxHelper._loadHandler(this);
      }
    };
  return result;
}

/** Sends an ajax request object. */
alfresco.AjaxHelper.sendRequest = function(req)
{
  alfresco.AjaxHelper._sendHandler(req);
  req.encoding = "utf-8";
  dojo.io.queueBind(req);
}

/** 
 * Returns the ajax loader div element.  If it hasn't yet been created, it is created. 
 */
alfresco.AjaxHelper._getLoaderElement = function()
{
  var result = document.getElementById(alfresco.constants.AJAX_LOADER_DIV_ID);
  if (result)
  {
    return result;
  }
  result = document.createElement("div");
  result.setAttribute("id", alfresco.constants.AJAX_LOADER_DIV_ID);
  dojo.html.setClass(result, "xformsAjaxLoader");
  dojo.html.hide(result);
  document.body.appendChild(result);
  return result;
}

/** All pending ajax requests. */
alfresco.AjaxHelper._requests = [];

/** Updates the loader message or hides it if nothing is being loaded. */
alfresco.AjaxHelper._updateLoaderDisplay = function()
{
  var ajaxLoader = alfresco.AjaxHelper._getLoaderElement();
  ajaxLoader.innerHTML = (alfresco.AjaxHelper._requests.length == 0
                          ? alfresco.xforms.constants.resources["idle"]
                          : (alfresco.xforms.constants.resources["loading"] + 
                             (alfresco.AjaxHelper._requests.length > 1
                              ? " (" + alfresco.AjaxHelper._requests.length + ")"
                              : "...")));
  if (djConfig.isDebug)
  {
    dojo.debug(ajaxLoader.innerHTML);
  }
  if (/* djConfig.isDebug && */ alfresco.AjaxHelper._requests.length != 0)
  {
    dojo.html.show(ajaxLoader);
  }
  else
  {
    dojo.html.hide(ajaxLoader);
  }
}

////////////////////////////////////////////////////////////////////////////////
// ajax event handlers
////////////////////////////////////////////////////////////////////////////////

alfresco.AjaxHelper._sendHandler = function(req)
{
  alfresco.AjaxHelper._requests.push(req);
  alfresco.AjaxHelper._updateLoaderDisplay();
}

alfresco.AjaxHelper._loadHandler = function(req)
{
  var index = alfresco.AjaxHelper._requests.indexOf(req);
  if (index != -1)
  {
    alfresco.AjaxHelper._requests.splice(index, 1);
  }
  else
  {
    var urls = [];
    for (var i = 0; i < alfresco.AjaxHelper._requests.length; i++)
    {
      urls.push(alfresco.AjaxHelper._requests[i].url);
    }
    throw new Error("unable to find " + req.url + 
                    " in [" + urls.join(", ") + "]");
  }
  alfresco.AjaxHelper._updateLoaderDisplay();
}
