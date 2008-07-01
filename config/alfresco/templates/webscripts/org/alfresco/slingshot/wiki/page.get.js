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
		//return jsonError("No parameters supplied");
		return null;
    }

    // Get the site
    var site = siteService.getSite(params.siteId);
    if (site === null)
    {
		//return jsonError("Could not find site: " + siteId);
		return null;
    }

    var wiki = site.getContainer("wiki");
    if (wiki === null)
    {
		//return jsonError("Could not locate wiki container");
		return null;
    }
    
	var page = wiki.childByNamePath(params.pageTitle);
    if (page === null)
    {
		page = createWikiPage(params.pageTitle, wiki, {
			content: 'This is a new page. It contains no content',
			versionable: true
		});
    }

/**
	if (page.type == "{http://www.alfresco.org/model/content/1.0}link")
	{
		page = search.findNode(page.properties["cm:destination"].nodeRef);
	}
**/
	return page;
}

var page = main();
model.page = page;

// NOTE: until there are some general purposes functions in the Freemarker for escaping 
// certain characters for the JSON representation, we have to do it here for now.
var result = eval('(' + jsonUtils.toJSONString({ content: page.content }) + ')'); 
model.jsonPageText = result.content; // the escaped text
