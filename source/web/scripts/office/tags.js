/*
 * Prerequisites: mootools.v1.11.js
 *                office_addin.js
 */
var OfficeTags =
{
   /* Scaling for tag clouds - must have supporting CSS classes defined */
   SCALE_FACTOR: 5,
   
   /* Set to preselect a tag after the tag cloud populated */
   preselectedTag: "",
   
   init: function()
   {
      OfficeTags.getTagCloud();
   },
   
   getTagCloud: function()
   {
      if (!window.queryObject.st)
      {
         OfficeAddin.showStatusText("Loading tag cloud...", "ajax_anim.gif", false);
      }

      // ajax call to get repository tag data
      var actionURL = window.serviceContextPath + "/collaboration/tagQuery";
      var myJsonRequest = new Json.Remote(actionURL,
      {
         method: 'get',
         headers: {'If-Modified-Since': 'Sat, 1 Jan 2000 00:00:00 GMT'},
         onComplete: function(tagQuery)
         {
            if (!window.queryObject.st)
            {
               OfficeAddin.hideStatusText();
            }
            OfficeTags.populateTagCloud(tagQuery);
         }
      }).send();
   },
   
   populateTagCloud: function(tagQuery)
   {
      var tagCloud = $("tagCloud"),
         range = tagQuery.countMax - tagQuery.countMin,
         scale = (range / OfficeTags.SCALE_FACTOR);
      
      var tagContainer, tagName, tagNameClass, tagCount;

      tagCloud.empty();
      tagQuery.tags.each(function(tag, i)
      {
         tagNameClass = "tagName" + Math.round((tag.count - tagQuery.countMin) / scale);
         if (OfficeTags.preselectedTag == tag.name)
         {
            tagNameClass += " tagSelected";
         }  
         tagName = new Element("a", {"class": tagNameClass, "rel": tag.name});
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
      if (OfficeTags.preselectedTag !== "")
      {
         OfficeTags.selectTag(OfficeTags.preselectedTag);
      }
   },

   /* AJAX call to perform server-side tag search */
   selectTag: function(tagName)
   {
      OfficeAddin.showStatusText("Searching tags...", "ajax_anim.gif", false);

      var maxResults = 100,
         args = OfficeAddin.defaultQuery + "&type=tag",
         actionURL = window.serviceContextPath + "/office/searchResults" + args + "&search=" + encodeURIComponent(tagName) + "&maxresults=" + maxResults;
      var myAjax = new Ajax(actionURL,
      {
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
      
      var tags = $$("#tagCloud a");
      tags.each(function(tag, i)
      {
         if (tag.rel == tagName)
         {
            tag.addClass("tagSelected");
         }
         else
         {
            tag.removeClass("tagSelected");
         }
      });
   },
   
   preselectTag: function(tagName)
   {
      OfficeTags.preselectedTag = tagName;
   }
};

/* Search Results expects this class */
var OfficeSearch = 
{
   itemsFound: function(shownResults, totalResults)
   {
      var strFound;
      if (totalResults === 0)
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