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
      // Wiki "not found" error is used elsewhere
      return status.setCode(status.STATUS_PRECONDITION_FAILED, "Could not find site: " + params.siteId);
   }

   var wiki = getWikiContainer(site);
   if (wiki === null)
   {
      return jsonError("Could not locate wiki");
   }
 
   var page = wiki.childByNamePath(params.pageTitle);
   if (!page)
   {
      model.container = wiki;
      return status.setCode(status.STATUS_NOT_FOUND, "The page \"" + params.pageTitle.replace(/_/g, " ") + "\" does not exist.");
   }

   // Figure out what (internal) pages this page contains links to
   var content = page.content.toString();
   var re = /\[\[([^\|\]]+)/g;
    
   var links = [], result, match, matched_p, matchedSoFar = [], j;
   while ((result = re.exec(content)) !== null)
   {
      match = result[1];
      matched_p = false;
      // Check for duplicate links
      for (j = 0; j < matchedSoFar.length; j++)
      {
         if (match === matchedSoFar[j])
         {
            matched_p = true;
            break;
         }
      }
      
      if (!matched_p)
      {
         matchedSoFar.push(match);
         links.push(match);
      }
   }

   // Also return complete list of pages to resolve links
   var query = "+PATH:\"" + wiki.qnamePath + "//*\" ";
   query += " +(@\\{http\\://www.alfresco.org/model/content/1.0\\}content.mimetype:application/octet-stream OR";
   query += "  @\\{http\\://www.alfresco.org/model/content/1.0\\}content.mimetype:text/html)";
   query += " -TYPE:\"{http://www.alfresco.org/model/content/1.0}thumbnail\"";
   query += " -TYPE:\"{http://www.alfresco.org/model/forum/1.0}post\"";
   
   var wikiPages = search.luceneSearch(query);
   var p, pageList = [];
   for each (p in wikiPages)
   {
      pageList.push(p.name);
   }
    
   return (
   {
      page: page,
      container: wiki,
      tags: page.tags,
      links: links,
      pageList: pageList
   });
}

model.result = main();
