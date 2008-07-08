<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/wiki/lib/wiki.lib.js">
/**
 * Renames a wiki page. Updates the name of the current page
 * and creates a link with the previous name that points to the page;
 * this is done so that all references to the old page will still work.
 *
 * @method POST
 * @param uri {string} /slingshot/wiki/page/{siteid}/{pageTitle}
 */

model.result = main();

function main()
{
	if (json.isNull("name"))
	{
		return jsonError("No new name property specified");
	}
	// Remove any whitespace and replace with "_"
	var newName = String(json.get("name")).replace(/\\s+/g, "_");

	var params = getTemplateArgs(["siteId", "pageTitle"]);
	
	// Get the site
    var site = siteService.getSite(params.siteId);
    if (site === null)
    {
		return jsonError("Could not find site: " + params.siteId);
    }

	var wiki = site.getContainer("wiki");
    if (wiki === null)
    {
		return jsonError("Could not locate wiki");
    }

	var page = wiki.childByNamePath(params.pageTitle);
	if (!page)
	{
		return jsonError("Could not find specified page.");
	}
	
	// Finally, now we can do what we are supposed to do
	try 
	{
		var currentName = page.name;
		page.name = newName;
		page.save();

/**		
		var link = wiki.createNode(currentName, "cm:link");
		link.properties["cm:destination"] = page.nodeRef;
		link.save();
**/		
		var placeholder = createWikiPage(currentName, wiki, {
			content: "This page has been moved [["  + args.name + "|here]]."
		});
		
	   var d = {
		   currentName: newName,
		   previousName: currentName,
		   pageContext: ""
		}

		activities.postActivity("org.alfresco.wiki.page-renamed", params.siteId, "wiki", jsonUtils.toJSONString(d));
		
		return {
			name: newName // Return the new name to the client (?)
		}
	}
	catch (e)
	{
		return jsonError(e.toString());
	}
}