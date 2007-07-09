/*
 * Prerequisites: mootools.v1.11.js
 *                office_addin.js
 */
var OfficeSearch = 
{
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
   },
   
   itemsFound: function(shownResults, totalResults)
   {
      var strFound;
      if (totalResults == 0)
      {
         strFound = "No items found";
      }
      else if (shownResults < totalResults)
      {
         strFound = "Showing first " + shownResults + " of " + totalResults + " total items found";
      }
      else
      {
         strFound = "Showing all " + shownResults + " items found";
      }
      $('itemsFound').innerHTML = strFound;
   },

   /* AJAX call to perform server-side search */
   runSearch: function(useTemplate, argPath)
   {
      OfficeAddin.showStatusText("Searching...", "ajax_anim.gif", false);

      var searchString = $('searchText').value;
      var maxResults = $('maxResults').value;

      var actionURL = useTemplate + "?p=" + argPath + "&search=" + searchString + "&maxresults=" + maxResults;
      var myAjax = new Ajax(actionURL, {
         method: 'get',
         headers: {'If-Modified-Since': 'Sat, 1 Jan 2000 00:00:00 GMT'},
         evalScripts: true,
         onComplete: function(textResponse, xmlResponse)
         {
            OfficeAddin.hideStatusText();
            $('searchResultsList').innerHTML = textResponse;
         }
      });
      myAjax.request();
   }
};

window.addEvent('domready', OfficeSearch.init);