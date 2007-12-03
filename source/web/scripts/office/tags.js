/*
 * Prerequisites: mootools.v1.11.js
 *                office_addin.js
 */
var OfficeTags =
{
   /* Scaling for tag clouds - must have supporting CSS classes defined */
   SCALE_FACTOR: 5,
   
   /* Manadatory params for searchResults component */
   searchParams: "",
   
   init: function()
   {
      OfficeTags.getTagCloud();
   },
   
   setParams: function(params)
   {
      OfficeTags.searchParams = params;
   },
   
   getTagCloud: function()
   {
      OfficeAddin.showStatusText("Loading tag cloud...", "ajax_anim.gif", false);

      // ajax call to get repository tag data
      var actionURL = window.serviceContextPath + "/collaboration/tagQuery";
      var myJsonRequest = new Json.Remote(actionURL, {
         method: 'get',
         headers: {'If-Modified-Since': 'Sat, 1 Jan 2000 00:00:00 GMT'},
         onComplete: function(tagQuery)
         {
            OfficeAddin.hideStatusText();
            OfficeTags.populateTagCloud(tagQuery);
         }
      }).send();
   },
   
   populateTagCloud: function(tagQuery)
   {
      var tagCloud = $("tagCloud");
      var range = tagQuery.countMax - tagQuery.countMin;
      var scale = (range / OfficeTags.SCALE_FACTOR);
      
      var tagContainer, tagName, tagNameClass, tagCount;

      tagCloud.empty();      
      tagQuery.tags.each(function(tag, i)
      {
         tagNameClass = "tagName" + Math.round((tag.count - tagQuery.countMin) / scale);         
         tagName = new Element("a", {"class": tagNameClass});
         tagName.appendText(tag.name);
         tagName.injectInside(tagCloud);
         tagName.addEvent('click', function(e)
         {
            OfficeTags.selectTag(tag.name);
         });
         tagCloud.appendText(" ");
         
         tagCount = new Element("span", {"class": "tagCount"});
         tagCount.appendText("(" + tag.count + ")");
         tagCount.injectInside(tagName);
      });
      
      // $("tagCloud").innerHTML = Json.toString(tagQuery);
   },

   /* AJAX call to perform server-side tag search */
   selectTag: function(tagName)
   {
      OfficeAddin.showStatusText("Searching tags...", "ajax_anim.gif", false);

      // var maxResults = $('maxResults').value;
      var maxResults = 100;
      
      var args = OfficeTags.searchParams + "&type=tag";
      var actionURL = window.serviceContextPath + "/office/searchResults?p=" + args + "&search=" + tagName.replace(" ", "_x0020_") + "&maxresults=" + maxResults;
      var myAjax = new Ajax(actionURL, {
         method: 'get',
         headers: {'If-Modified-Since': 'Sat, 1 Jan 2000 00:00:00 GMT'},
         evalScripts: true,
         onComplete: function(textResponse, xmlResponse)
         {
            OfficeAddin.hideStatusText();
            $('taggedList').innerHTML = textResponse;
            $('taggedHeader').innerHTML = "Tagged with \"" + tagName + "\"";
         }
      }).request();
   }
};

/* Search Results expects this class */
var OfficeSearch = 
{
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
   }
};

window.addEvent('domready', OfficeTags.init);