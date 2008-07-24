function getParsedArgs()
{
   var rootNode = null;
   var parentNode = null;

   if (url.templateArgs.store_type !== null)
   {
      // nodeRef input
      var storeType = url.templateArgs.store_type;
      var storeId = url.templateArgs.store_id;
      var id = url.templateArgs.id;
      var nodeRef = storeType + "://" + storeId + "/" + id;
      
      if (nodeRef == "alfresco://company/home")
      {
         rootNode = companyhome;
      }
      else if (nodeRef == "alfresco://user/home")
      {
         rootNode = userhome;
      }
      else
      {
         rootNode = search.findNode(nodeRef);
      	if (rootNode === null)
      	{
            status.setCode(status.STATUS_NOT_FOUND, "Not a valid nodeRef: '" + nodeRef + "'");
            return null;
      	}
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
         status.setCode(status.STATUS_NOT_FOUND, "Site not found: '" + site + "'");
         return null;
      }

      rootNode = siteNode.getContainer(container);
      if (rootNode === null)
      {
      	 rootNode = siteNode.createContainer(container);
      	 if (rootNode === null)
      	 {
         	status.setCode(status.STATUS_NOT_FOUND, "Document Library container '" + container + "' not found in '" + site + "'. (No permission?)");
         	return null;
         }
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