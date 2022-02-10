function main()
{
   // site, component container input
   var siteId = url.templateArgs.site,
      containerId = url.templateArgs.container,
      containerType = args.type;

   siteNode = siteService.getSite(siteId);
   if (siteNode === null)
   {
      status.setCode(status.STATUS_NOT_FOUND, "Site not found: '" + siteId + "'");
      return null;
   }

   containerNode = siteNode.getContainer(containerId);
   if (containerNode === null)
   {
      if (containerType != null && containerType != "")
      {
         containerNode = siteNode.createContainer(containerId, containerType);
      }
      else
      {
         containerNode = siteNode.createContainer(containerId);
      }
      if (containerNode === null)
      {
         status.setCode(status.STATUS_NOT_FOUND, "Document Library container '" + containerId + "' not found in '" + siteId + "'. (No permission?)");
         return null;
      }
   }

   model.container = containerNode;
}

main();