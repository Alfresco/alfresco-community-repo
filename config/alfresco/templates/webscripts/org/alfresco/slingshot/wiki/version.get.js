<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/wiki/lib/wiki.lib.js">

function main()
{
   var params = getTemplateArgs(["siteId", "pageTitle", "versionId"]);
   var content = "";
   // Get the site
   var site = siteService.getSite(params.siteId);
   if (site === null)
   {
	   return "";
   }

   var wiki = getWikiContainer(site);
   if (wiki === null)
   {
      return "";
   }
    
   var page = wiki.childByNamePath(params.pageTitle);
   if (page === null)
   {
      return "";
   }
   
   var version;
   var versions = page.versionHistory;
   // NOTE: would it be possible to pass in the noderef and do a search for the specific
   // version (directly) against the "lightWeightVersionStore"? This would depend on what
   // indexing (if any) there is on the version store.
   for (var i=0; i < versions.length; i++)
   {
      version = versions[i];
      versionNode = version.node;

      // If we don't create a string explicitly the comparison fails
      if (String(versionNode.id) === params.versionId || 
          String(version.label) == params.versionId)
      {
         content = versionNode.content;
         break;
      }
   }
   
   return content;
}

model.content = main();
