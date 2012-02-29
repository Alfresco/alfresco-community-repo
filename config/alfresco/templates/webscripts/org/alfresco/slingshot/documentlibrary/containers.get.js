/**
 * TODO: This webscript should get all the site's containers and return the Document Library ones.
 * Unfortunately, there's no way to distinguish them, so we can only return the one called "documentLibrary".
 */
function main()
{
   // site input
   var siteId = url.templateArgs.site;

   siteNode = siteService.getSite(siteId);
   if (siteNode === null)
   {
      status.setCode(status.STATUS_NOT_FOUND, "Site not found: '" + siteId + "'");
      return null;
   }

   var containerId = "documentLibrary",
      containerNode = siteNode.getContainer(containerId);
   if (containerNode === null)
   {
      status.setCode(status.STATUS_NOT_FOUND, "Document Library container '" + containerId + "' not found in '" + siteId + "'. (No permission?)");
      return null;
   }

   model.containers = [containerNode];
}

main();