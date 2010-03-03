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

/** All pending ajax requests. */
alfresco.AjaxHelper._requests = [];

/** A counter for numbering requests - for debugging */
alfresco.AjaxHelper._requestCounter = 0;

/** Creates an ajax request object. */
alfresco.AjaxHelper.sendRequest = function(serverMethod, methodArgs, asynchronous, success, failure)
{
  var result = new XHR({ method: "post", encoding: "utf-8", async: asynchronous });
  sucess = success || Class.empty;
  failure = failure || Class.empty;
  result._alfresco_AjaxHelper_request_counter = alfresco.AjaxHelper._requestCounter++;
  result.addEvent("onRequest", alfresco.AjaxHelper._onRequestHandler.bindAsEventListener(result));
  result.addEvent("onSuccess", alfresco.AjaxHelper._loadHandler.bindAsEventListener(result));
  result.addEvent("onSuccess", function(text, xml) { success(xml); });
  result.addEvent("onFailure", alfresco.AjaxHelper._loadHandler.bindAsEventListener(result));
  result.addEvent("onFailure", alfresco.AjaxHelper._errorHandler.bindAsEventListener(result));
  result.addEvent("onFailure", function(text, xml) { failure(xml); });

  methodArgs = alfresco.AjaxHelper.toQueryString(methodArgs);
  alfresco.log("sending request to " + serverMethod + "(" + methodArgs + ")");
  result.send(alfresco.constants.WEBAPP_CONTEXT + "/ajax/invoke/" + serverMethod, methodArgs);
  return result;
}

/** 
 * Returns the ajax loader div element.  If it hasn't yet been created, it is created. 
 */
alfresco.AjaxHelper._getLoaderElement = function()
{
  var result = $(alfresco.constants.AJAX_LOADER_DIV_ID);
  if (!result)
  {
    result = new Element("div", 
                         {
                           "id": alfresco.constants.AJAX_LOADER_DIV_ID,
                           "class": "xformsAjaxLoader"
                         });
    result.setOpacity(0);
    document.body.appendChild(result);
  }
  return result;
}

/** Updates the loader message or hides it if nothing is being loaded. */
alfresco.AjaxHelper._updateLoaderDisplay = function()
{
  var ajaxLoader = alfresco.AjaxHelper._getLoaderElement();
  ajaxLoader.setText(alfresco.AjaxHelper._requests.length == 0
                     ? alfresco.resources["idle"]
                     : (alfresco.resources["loading"] + 
                        (alfresco.AjaxHelper._requests.length > 1
                         ? " (" + alfresco.AjaxHelper._requests.length + ")"
                         : "...")));
  alfresco.log(ajaxLoader.getText());
  ajaxLoader.setOpacity(alfresco.AjaxHelper._requests.length != 0 ? 1 : 0);
}

////////////////////////////////////////////////////////////////////////////////
// ajax event handlers
////////////////////////////////////////////////////////////////////////////////

alfresco.AjaxHelper._onRequestHandler = function()
{
  alfresco.AjaxHelper._requests.push(this._alfresco_AjaxHelper_request_counter);
  alfresco.AjaxHelper._updateLoaderDisplay();
}

alfresco.AjaxHelper._loadHandler = function()
{
  var index = alfresco.AjaxHelper._requests.indexOf(this._alfresco_AjaxHelper_request_counter);
  if (index != -1)
  {
    alfresco.AjaxHelper._requests.splice(index, 1);
  }
  else
  {
    var urls = [];
    for (var i = 0; i < alfresco.AjaxHelper._requests.length; i++)
    {
      urls.push(alfresco.AjaxHelper._requests[i]);
    }
    throw new Error("unable to find " + req.url + 
                    " in [" + urls.join(", ") + "]");
  }
  alfresco.AjaxHelper._updateLoaderDisplay();
}

alfresco.AjaxHelper._errorHandler = function(transport)
{
  alfresco.log("error status = " + transport.status +
               " response text = " + transport.responseText);
  if (transport.status == 401)
  {
    document.getElementById("logout").onclick();
  }
  else if (transport.status != 0)
  {
    var d = document.createElement("div");
    d.innerHTML = transport.responseText;
    _show_error(d);
  }
}

alfresco.AjaxHelper.toQueryString = function(source)
{
  var queryString = [];
  for (var property in source) 
  {
    if (typeof source[property] == "object")
    {
      for (var i = 0; i < source[property].length; i++)
      {
        queryString.push(encodeURIComponent(property) + '=' + encodeURIComponent(source[property][i]));
      }
    }
    else
    {
      queryString.push(encodeURIComponent(property) + '=' + encodeURIComponent(source[property]));
    }
  }
  return queryString.join('&');
};
