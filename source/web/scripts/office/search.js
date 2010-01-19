/*
 * Prerequisites: mootools.v1.11.js
 *                office_addin.js
 */
var OfficeSearch = 
{
   MIN_LENGTH: 3,
   
   init: function()
   {
      $('searchText').addEvent('keydown', function(event)
      {
         event = new Event(event);
         if (event.key == 'enter')
         {
            $("simpleSearchButton").onclick();
         }
      });
      
      $('itemsFound').innerHTML = "Results Shown Below";
      
      $('searchText').focus();
   },
   
   itemsFound: function(shownResults, totalResults)
   {
      var strFound;
      if (totalResults === 0)
      {
         strFound = "No items found";
      }
      else
      {
         strFound = shownResults + " items found";
      }
      $('itemsFound').innerHTML = strFound;
   },

   /* AJAX call to perform server-side search */
   runSearch: function(useTemplate, argPath)
   {
      var searchString = $('searchText').value;
      var maxResults = $('maxResults').value;
      
      if (searchString.length < OfficeSearch.MIN_LENGTH)
      {
         OfficeAddin.showStatusText("Minimum " + OfficeSearch.MIN_LENGTH + " characters.", "info.gif", true);
         return;
      }

      OfficeAddin.showStatusText("Searching...", "ajax_anim.gif", false);
      var actionURL = useTemplate + argPath + "&search=" + encodeURIComponent(searchString) + "&maxresults=" + maxResults;
      var myAjax = new Ajax(actionURL,
      {
         method: 'get',
         headers: {'If-Modified-Since': 'Sat, 1 Jan 2000 00:00:00 GMT'},
         evalScripts: true,
         onComplete: function(textResponse, xmlResponse)
         {
            OfficeAddin.hideStatusText();
            $('searchResultsList').innerHTML = textResponse;
            /* Custom tooltips
            var myTips = new Tips($$(".toolTip"),
            {
               fixed: true,
               maxTitleChars: 50
            });
            */
         }
      });
      myAjax.request();
   }
};

window.addEvent('domready', OfficeSearch.init);