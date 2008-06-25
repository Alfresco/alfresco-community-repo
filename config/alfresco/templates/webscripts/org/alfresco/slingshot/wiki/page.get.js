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
		return jsonError("No parameters supplied");
    }

    // Get the site
    var site = siteService.getSite(params.siteId);
    if (site === null)
    {
		return jsonError("Could not find site: " + siteId);
    }

    var wiki = site.getContainer("wiki");
    if (wiki === null)
    {
		return jsonError("Could not locate wiki container");
    }

    var page = wiki.childByNamePath(params.pageTitle);
    if (page === null)
    {
		// NOTE: may need a custom content type for a wiki entry
		page = wiki.createFile(params.pageTitle);
		page.addAspect("cm:versionable");
		page.content = '<h2>' + params.pageTitle + '</h2><p>This page contains no content. You can <a href="#edit">edit</a> it.</p>'; 
		page.save();
    }
	
	// Handle wiki markup - well, one instance of it
	var re = /\[\[([^\]]*)\]\]/g;
	var content = new String(page.content); 
	
	return {
		pagetext: "" + content.replace(re, '<a href="$1">$1</a>')
	};
}

var result = main();
model.result = result;
