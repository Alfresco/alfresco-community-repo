function main()
{
   // site, component container input
   siteId = url.templateArgs.site;
   containerId = url.templateArgs.container;

   siteNode = siteService.getSite(siteId);
   if (siteNode === null)
   {
      status.setCode(status.STATUS_NOT_FOUND, "Site not found: '" + siteId + "'");
      return null;
   }

   containerNode = siteNode.getContainer(containerId);
   if (containerNode === null)
   {
      status.setCode(status.STATUS_NOT_FOUND, "Document Library container '" + containerId + "' not found in '" + siteId + "'. (No permission?)");
      return null;
   }

   model.container = containerNode;
}

main();