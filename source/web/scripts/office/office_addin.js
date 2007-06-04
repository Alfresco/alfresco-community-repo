/*
 * Prerequisites: mootools.v1.1.js
 */
var OfficeAddin = 
{
   ANIM_LENGTH: 300,
   STATUS_FADE: 10000,
   LIST_DEF_HEIGHT: 204,

   init: function()
   {
      this.queryObject = OfficeAddin.toQueryObject(document.location.search);

      /* Update needed after page load? */      
      if(this.queryObject.st)
      {
         var objResponse = Json.evaluate(this.queryObject.st);
         var imgSuccess = (objResponse.statusCode ? "action_successful.gif" : "action_failed.gif");
         OfficeAddin.showStatusText(objResponse.statusString, imgSuccess, true);
      }
      
      /* Have search box? */
      if ($("searchText"))
      {
         $("searchText").addEvent("keydown", function(event)
         {
            event = new Event(event);
            if (event.key == 'enter')
            {
               $("simpleSearchButton").click();
            }
         });
      }
      
      /* Have expandos? */
      if ($("toggleSpaceList"))
      {
         $("toggleSpaceList").addEvent("click", function()
         {
            $("spaceList").removeClass("listMediumShrink");
            $("spaceList").addClass("listMediumGrow");
            $("documentList").removeClass("listMediumGrow");
            $("documentList").addClass("listMediumShrink");
         });
         $("toggleDocumentList").addEvent("click", function()
         {
            $("documentList").removeClass("listMediumShrink");
            $("documentList").addClass("listMediumGrow");
            $("spaceList").removeClass("listMediumGrow");
            $("spaceList").addClass("listMediumShrink");
         });
      }
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

   showStatusText: function(statusText, statusImage, fadeOut)
   {
      var e = $("statusText");
      if (statusImage)
      {
         statusText = "<img src=\"../../images/office/" + statusImage + "\" alt=\"*\" style=\"padding-right: 8px;\" align=\"top\" />" + statusText;
      }
      e.innerHTML = statusText;
      e.setStyle("opacity", "1");
      if (statusText == "")
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
            fx.onComplete = new function()
            {
               OfficeAddin.hideStatusText.delay(OfficeAddin.STATUS_FADE);
            }
         }
         fx.start('#ffffcc', '#ffffff');
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
   runAction: function(useTemplate, Action, Doc, Msg)
   {
      if (Msg != "" && !confirm(Msg))
      {
         return;
      }
   
      OfficeAddin.showStatusText("Running action...", "ajax_anim.gif", false);
      var actionURL = useTemplate + "?a=" + Action + "&d=" + Doc;
      var myAjax = new Ajax(actionURL, {
         method: 'get',
         headers: {'If-Modified-Since': 'Sat, 1 Jan 2000 00:00:00 GMT'},
         onComplete: function(textResponse, xmlResponse)
         {
            // Remove any trailing hash
            var href = window.location.href.replace("#", "")
            // Remove any previous "&st=" strings
            href = href.replace(/[?&]st=([^&$]+)/g, "");
            // Optionally add a status string
            if (textResponse != "")
            {
               href += "&st=" + encodeURI(textResponse);
            }
            window.location.href = href;
         }
      });
      myAjax.request();
   },
   
   /* AJAX call to perform server-side search */
   runSearch: function(useTemplate, argPath)
   {
      OfficeAddin.showStatusText("Searching...", "ajax_anim.gif", false);

      var searchString = $("searchText").value;
      var maxResults = $("maxResults").value;

      var actionURL = useTemplate + "?p=" + argPath + "&search=" + searchString + "&maxresults=" + maxResults;
      var myAjax = new Ajax(actionURL, {
         method: 'get',
         headers: {'If-Modified-Since': 'Sat, 1 Jan 2000 00:00:00 GMT'},
         onComplete: function(textResponse, xmlResponse)
         {
            OfficeAddin.hideStatusText();
            $("searchResultsList").innerHTML = textResponse;
         }
      });
      myAjax.request();
   }
};

window.addEvent('domready', OfficeAddin.init);