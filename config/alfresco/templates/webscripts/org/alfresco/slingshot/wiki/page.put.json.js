<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/wiki/lib/wiki.lib.js">
/**
 * Update specified wiki page.
 *
 *
 * @method PUT
 * @param uri {string} /slingshot/wiki/page/{siteid}/{pageTitle}
 */

function getTemplateParams()
{
     // Grab the URI parameters
     var siteId = "" + url.templateArgs.siteId;
     var pageTitle = "" + url.templateArgs.pageTitle;

     if (siteId === null || siteId.length === 0)
     {
	 	return null;
     }

     if (pageTitle === null || pageTitle.length === 0)
     {
	 	return null;
     }

     return {
	 	"siteId": siteId,
	 	"pageTitle": pageTitle
     };
}

function update()
{
   var params = getTemplateArgs(["siteId", "pageTitle"]);
   if (params === null)
   {
	   return jsonError("No parameters supplied");
   }

   // Get the site
   var site = siteService.getSite(params.siteId);
   if (site === null)
   {
	   return jsonError("Could not find site: " + siteId);
   }

   var wiki = getWikiContainer(site);
   if (wiki === null)
   {
      return jsonError("Could not locate wiki container");
   }
	
	var page = wiki.childByNamePath(params.pageTitle);
	// Create the page if it doesn't exist
	if (page === null)
   {
	   page = createWikiPage(params.pageTitle, wiki, {
			content: json.get("pagecontent"),
			versionable: true
	   });
	
      var activityType = "org.alfresco.wiki.page-created";
   }
   else 
   {
      // Create a new revision of the page
   	var workingCopy = page.checkout();
   	workingCopy.content = json.get("pagecontent");
   	workingCopy.checkin();
   	
      var activityType = "org.alfresco.wiki.page-edited";
   }
	
	var d = {
      pageName: params.pageTitle.replace(/_/g, " "),
   	pageContext: (args.context ? unescape(args.context) : "")
   }
	// Log activity   
	activities.postActivity(activityType, params.siteId, "wiki", jsonUtils.toJSONString(d));
	
	if (!json.isNull("tags"))
   {
      var tags = Array(json.get("tags"));
      if (tags) 
      {
         // This is so unnecessary!
         // A much cleaner approach would be to just pass in the tags as a space separated
         // string and call the (native) method split
         var tags = [];
         var tmp = json.get("tags");
         for (var x=0; x < tmp.length(); x++)
         {
            tags.push(tmp.get(x));
         }
         page.tags = tags;
      }
      else
      {
         page.tags = []; // reset
      }
      page.save();
   }
	
	// NOTE: for now we return the raw page content and do the transformation
	// of any wiki markup on the client. This is because the edit view needs to display
	// the raw content (for editing) whereas the page view needs to display the rendered content.
	return {
	   page: page,
		tags: page.tags
	}
}

model.result = update();