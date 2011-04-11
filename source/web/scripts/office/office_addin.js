/*
 * Prerequisites: mootools.v1.11.js
 */
var OfficeAddin = 
{
   ANIM_LENGTH: 500,
   STATUS_FADE: 10000,
   LIST_DEF_HEIGHT: 204,
   
   defaultQuery: "",
   
   init: function()
   {
      $('overlayPanel').setStyle('opacity', 0);

      window.queryObject = OfficeAddin.toQueryObject(document.location.search);
      window.contextPath = OfficeAddin.getContextPath();
      window.serviceContextPath = OfficeAddin.getServiceContextPath();

      /* Update needed after page load? */      
      if (this.queryObject.st)
      {
         var objResponse = Json.evaluate(this.queryObject.st),
            imgSuccess = (objResponse.statusCode ? "action_successful.gif" : "action_failed.gif"),
            colBackground = (objResponse.statusCode ? "#50ff50" : "#ff5050");
         OfficeAddin.showStatusText(objResponse.statusString, imgSuccess, true, colBackground);
      }
      
      OfficeAddin.makeExternalLinks();
   },

   makeExternalLinks: function(e)
   {
      $$('a').each(function(anchor, i)
      {
         if (anchor.getProperty('rel') == '_blank')
         {
            anchor.target = "_blank";
         }
      });
   },

   toQueryObject: function(s)
   {
      var obj = {};
      $A(s.substring(1).split('&')).each(function(param)
      {
         param = decodeURIComponent(param).split("=");
         obj[param[0]] = param[1];
      });
      return obj;
   },

   showStatusText: function(statusText, statusImage, fadeOut, colBackground)
   {
      var e = $("statusText");
      if (statusImage)
      {
         statusText = "<img src=\"../../images/office/" + statusImage + "\" alt=\"*\" style=\"padding-right: 8px;\" align=\"top\" />" + statusText;
      }
      e.innerHTML = statusText;
      e.setStyle("opacity", "1");
      if (statusText === "")
      {
         e.setStyle("border-top", "");
      }
      else
      {
         e.setStyle("border-top", "1px dashed #808080");
         var fx = new Fx.Style(e, 'background-color',
         {
            duration: OfficeAddin.ANIM_LENGTH,
            transition: Fx.Transitions.linear
         });
         if (fadeOut)
         {
            fx.chain(function()
            {
               OfficeAddin.hideStatusText.delay(OfficeAddin.STATUS_FADE);
            });
         }
         var fxBackground = (typeof colBackground == "undefined") ? "#ffffcc" : colBackground;
         fx.start(fxBackground, "#ffffff");
      }
   },
   
   hideStatusText: function()
   {
      var e = $("statusText");
      e.setStyle("opacity", "1");
      var fx = new Fx.Style(e, 'opacity',
      {
         duration: OfficeAddin.ANIM_LENGTH
      }).start('1', '0');
   },
   
   /* AJAX call to perform server-side actions */
   getAction: function(useTemplate, action, nodeId, confirmMsg, inParams, outParams)
   {
      return OfficeAddin.runAction("get", useTemplate, action, nodeId, confirmMsg, inParams, outParams);
   },
   postAction: function(useTemplate, action, nodeId, confirmMsg, inParams, outParams)
   {
      return OfficeAddin.runAction("post", useTemplate, action, nodeId, confirmMsg, inParams, outParams);
   },
   runAction: function(httpMethod, useTemplate, action, nodeId, confirmMsg, inParams, outParams)
   {
      if (confirmMsg && confirmMsg !== "")
      {
         if (!window.confirm(confirmMsg))
         {
            return;
         }
      }
   
      OfficeAddin.showStatusText("Running action...", "ajax_anim.gif", false);
      var actionURL = useTemplate + "?a=" + action + "&n=" + nodeId;
      if (inParams && inParams !== "")
      {
         actionURL += "&" + inParams;
      }
      var myAjax = new Ajax(actionURL,
      {
         method: httpMethod,
         headers: {'If-Modified-Since': 'Sat, 1 Jan 2000 00:00:00 GMT'},
         onComplete: function(textResponse, xmlResponse)
         {
            // Remove any trailing hash
            var href = window.location.href.replace("#", "");
            // Remove any previous "st" parameters
            href = OfficeAddin.removeParameters(href, "st|version");
            // Optionally add a status string
            if (textResponse !== "")
            {
               href += (href.indexOf("?") == -1) ? "?" : "&";
               href += "st=" + encodeURIComponent(textResponse);
               if (outParams && outParams !== "")
               {
                  href += "&" + outParams;
               }
            }
            window.location.href = href.replace(/&undefined/g, "");
         }
      }).request();
   },

   /* Calculates and returns the context path for the current page */
   getContextPath: function()
   {
      var path = window.location.pathname,
         idx = path.indexOf("/", 1),
         contextPath = "";
      if (idx != -1)
      {
         contextPath = path.substring(0, idx);
      }
      else
      {
         contextPath = "";
      }
   
      return contextPath;
   },

   /* Calculates and returns the service context path for the current page */
   getServiceContextPath: function()
   {
      var path = window.location.pathname,
         idx = path.indexOf("/", 1),
         serviceContextPath = "";
      if (idx != -1)
      {
         serviceContextPath = path.substring(0, idx);
         path = path.substring(idx);
         idx = path.indexOf("/", 1);

         if (idx != -1)
         {
            serviceContextPath += path.substring(0, idx);
         }
      }
   
      return serviceContextPath;
   },
   
   /* Removes params "param1|param2...|paramN" from a URL */
   removeParameters: function(theUrl, theParams)
   {
      var regexp = new RegExp("[?&](" + theParams + ")=([^&$]+)", "g"),
         url = theUrl.replace(regexp, "");

      // Check that an href still contains a "?" after removing parameters
      var pos = url.indexOf("?");
      if (pos == -1)
      {
         // None found - do we have an "&" ?
         pos = url.indexOf("&");
         if (pos != -1)
         {
            // Yes - so replace the first one with a "?"
            url = url.substring(0, pos) + "?" + url.substring(pos+1);
         }
      }
      return url;
   },
   
   sortTasks: function(taskContainer)
   {
      var taskArray = [];
      taskContainer.getElementsBySelector('.taskItem').each(function(task, i)
      {
         taskArray[i] = {dueDate: task.getProperty('rel'), theTask: task.clone()};
      });

      taskArray.sort(OfficeAddin.sortByDueDate);
      taskContainer.empty();

      for(var i = 0; i < taskArray.length; i++)
      {
         taskArray[i].theTask.addClass((i % 2 === 0) ? "odd" : "even");
         taskArray[i].theTask.injectInside(taskContainer);
      }
   },
   
   sortByDueDate: function(a, b)
   {
      var x = a.dueDate,
         y = b.dueDate;
      return ((x < y) ? -1 : ((x > y) ? 1 : 0));
   },
   
   openWindowCallback: function(url, callback)
   {
      // Store the callback function for later
      OfficeAddin.callbackFunction = callback;
      // Register our "well known" callback function
      window.alfrescoCallback = OfficeAddin.openWindowOnCallback;
      // Use a named window so that only one dialog is active at a time
      window.open(url, 'alfrescoDialog', 'width=1024,height=768,scrollbars=yes');
   },
   
   openWindowOnCallback: function(fromTimeout)
   {
      if (typeof(fromTimeout)=='undefined')
      {
         window.setTimeout("OfficeAddin.openWindowOnCallback(true)", 10);
      }
      else
      {
         // Clear out the global callback function
         window.alfrescoCallback = null;
         try
         {
            OfficeAddin.callbackFunction();
         }
         catch (e)
         {
         }
         OfficeAddin.callbackFunction = null;
      }
   }
};

window.addEvent('domready', OfficeAddin.init);