<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/wiki/lib/wiki.lib.js">

/**
 * Get wiki page properties.
 * Creates a page if the specified one doesn't exist.
 *
 * @method GET
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

function main()
{
    var params = getTemplateParams();
    if (params === null)
    {
		return null;
    }

    // Get the site
    var site = siteService.getSite(params.siteId);
    if (site === null)
    {
		return null;
    }

    var wiki = getWikiContainer(site);
    if (wiki === null)
    {
       return null;
    }
    
	 var page = wiki.childByNamePath(params.pageTitle);
    if (page === null)
    {
		page = createWikiPage(params.pageTitle, wiki, {
			content: DEFAULT_PAGE_CONTENT,
			versionable: true
		});
	  
		try
		{
		   // Log page create to activity service
		   var d = {
		      pageName: params.pageTitle.replace(/_/g, " "),
		      pageContext: (args.context ? args.context : "")
		   }

			activities.postActivity("org.alfresco.wiki.page-created", params.siteId, "wiki", jsonUtils.toJSONString(d));
		}
		catch(e)
		{
			logger.log(e);
		}
    }
    
	return {
	  "page": page,
	  "tags": page.tags 
	};
}

model.result = main();
