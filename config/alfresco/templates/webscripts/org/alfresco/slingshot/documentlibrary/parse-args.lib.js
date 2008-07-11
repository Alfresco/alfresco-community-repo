function getParsedArgs()
{
   var rootNode = null;
   var parentNode = null;

   if (url.templateArgs.store_type != undefined)
   {
      // nodeRef input
      var storeType = url.templateArgs.store_type;
      var storeId = url.templateArgs.store_id;
      var id = url.templateArgs.id;
      var nodeRef = storeType + "://" + storeId + "/" + id;
      rootNode = search.findNode(nodeRef);
   	if (rootNode === null)
   	{
         status.setCode(status.STATUS_BAD_REQUEST, "Not a valid nodeRef: '" + nodeRef + "'");
         return;
   	}
   }
   else
   {
      // site, component container, path input
      var site = url.templateArgs.site;
      var container = url.templateArgs.container;

      var siteNode = siteService.getSite(site);
      if (siteNode === null)
      {
         status.setCode(status.STATUS_BAD_REQUEST, "Site not found: '" + site + "'");
         return;
      }

      rootNode = siteNode.getContainer(container);
      if (rootNode === null)
      {
         status.setCode(status.STATUS_BAD_REQUEST, "Document Library container '" + container + "' not found in '" + site + "'. (No permission?)");
         return;
      }
   }

   // path input
   var path = url.templateArgs.path;
   if ((path !== null) && (path != ""))
   {
      parentNode = rootNode.childByNamePath(path);
   }
   else
   {
      parentNode = rootNode;
      path = "";
   }
   if (parentNode === null)
   {
      parentNode = rootNode;
   }

   return (
   {
      rootNode: rootNode,
      parentNode: parentNode,
      path: path
   });
}