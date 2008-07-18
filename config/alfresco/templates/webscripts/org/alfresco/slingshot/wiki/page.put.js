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
	var params = getTemplateParams();
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
	try 
	{
		// Create a new revision of the page
		var workingCopy = page.checkout();
		workingCopy.content = json.get("pagecontent");
		workingCopy.checkin();

      // Log page update to activity service
		var d = {
		   pageName: params.pageTitle.replace(/_/g, " "),
		   pageContext: (args.context ? unescape(args.context) : "")
		}

		activities.postActivity("org.alfresco.wiki.page-edited", params.siteId, "wiki", jsonUtils.toJSONString(d));
	}
	catch(e)
	{
		if (logger.isLoggingEnabled())
		{
			logger.log(e);
		}
	}
	
	// NOTE: for now we return the raw page content and do the transformation
	// of any wiki markup on the client. This is because the edit view needs to display
	// the raw content (for editing) whereas the page view needs to display the rendered content.
	return {
		pagetext: "" + page.content
	}
}

var result = update();
model.result = jsonUtils.toJSONString(result); 