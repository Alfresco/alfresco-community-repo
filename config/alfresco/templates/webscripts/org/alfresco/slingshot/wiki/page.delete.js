<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/wiki/lib/wiki.lib.js">
/**
 * Deletes le page.
 *
 * @method DELETE
 * @param uri {string} /slingshot/wiki/page/{siteid}/{pageTitle}
 */
deleteEvent();
 
function deleteEvent()
{
   var params = getTemplateArgs(["siteId", "pageTitle"]);
   if (params === null)
   {
      status.setCode(status.STATUS_BAD_REQUEST, "Correct parameters not supplied.");
	   return ;
   }

   var site = siteService.getSite(params.siteId);
   if (site === null)
   {
      status.setCode(status.STATUS_NOT_FOUND, "Could not find site.");
	   return;
   }

   var wiki = site.getContainer("wiki");
   if (wiki === null)
   {
      status.setCode(status.STATUS_NOT_FOUND, "Could not find wiki container.");
	   return;
   }

   var page = wiki.childByNamePath(params.pageTitle);
   if (page === null)
   {
      status.setCode(status.STATUS_NOT_FOUND, "Could not find specified page.");
	   return;
   }

	var whatPage = page.name; 
	
   if (!page.remove())
   {
      status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, "Failed to delete page");
	   return;
   }
   
   // Success
   status.setCode(status.STATUS_NO_CONTENT); // Nothing to do here yet
   return;
}