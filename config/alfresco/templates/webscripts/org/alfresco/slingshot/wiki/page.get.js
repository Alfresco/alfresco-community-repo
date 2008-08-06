<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/wiki/lib/wiki.lib.js">
/**
 * Get wiki page properties.
 * Returns an error message if the specified page cannot be found.
 *
 * @method GET
 * @param uri {string} /slingshot/wiki/page/{siteid}/{pageTitle}
 */
function main()
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
	   return jsonError("Could not find site: " + params.siteId);
   }

   var wiki = getWikiContainer(site);
   if (wiki === null)
   {
      return jsonError("Could not locate wiki");
   }
   
   var page = wiki.childByNamePath(params.pageTitle);
	if (!page)
	{
		return jsonError(DEFAULT_PAGE_CONTENT);
	}

   // Figure out what (internal) pages this page contains links to
   var content = page.content.toString();
   var re = /\[\[([^\|\]]+)/g;
   var links = [];
    
   var result, match, matched_p;
   var matchedsofar = [];
   while ((result = re.exec(content)) !== null)
   {
      match = result[1];
      matched_p = false;
      // Check for duplicate links
      for (var j=0; j < matchedsofar.length; j++)
      {
         if (match ===  matchedsofar[j])
         {
            matched_p = true;
            break;
         }
      }
      
      if (!matched_p)
      {
         matchedsofar.push(match);
         links.push(match);
      }
   }
    
	return {
	   "page": page,
	   "tags": page.tags,
	   "links": links
	};
}

model.result = main();
