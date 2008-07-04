
var siteId = url.templateArgs.siteId;
model.pageList = getWikiPages(siteId);

function getWikiPages(siteId)
{
   if (siteId === null || siteId.length === 0)
   {
	   status.setCode(status.STATUS_BAD_REQUEST, "Site not found: '" + siteId + "'");
   	return;
   }
   
   var site = siteService.getSite(siteId);
   if (site === null)
   {
	   status.setCode(status.STATUS_BAD_REQUEST, "Site not found: '" + siteId + "'");
	   return;
   }
   
   var wiki = site.getContainer("wiki");
   if (wiki === null)
   {
	   status.setCode(status.STATUS_BAD_REQUEST, "Wiki container not found");
	   return;
   }
   
   var pages = [];
   var wikiPages = wiki.children;
   
   var page;
   for (var i=0; i < wikiPages.length; i++)
   {
      page = wikiPages[i];
      pages.push(page);
   }
   
   return ( 
   {
      "pages": pages
   });
}