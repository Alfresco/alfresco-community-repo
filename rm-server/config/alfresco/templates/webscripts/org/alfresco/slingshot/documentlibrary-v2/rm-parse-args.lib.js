/**
 * Get and parse arguments
 *
 * @method getParsedArgs
 * @return {array|null} Array containing the validated input parameters
 */
ParseArgs.getParsedArgs = function RecordsManagementFilter_getParsedArgs(containerType)
{
   var type = url.templateArgs.type,
      libraryRoot = args.libraryRoot,
      rootNode = null,
      pathNode = null,
      nodeRef = null,
      path = "",
      location = null;

   // Is this library rooted from a non-site nodeRef?
   if (libraryRoot !== null)
   {
      libraryRoot = ParseArgs.resolveNode(libraryRoot);
   }


   if (url.templateArgs.store_type !== null)
   {
      /**
       * nodeRef input: store_type, store_id and id
       */
      var storeType = url.templateArgs.store_type,
         storeId = url.templateArgs.store_id,
         id = url.templateArgs.id;

      nodeRef = storeType + "://" + storeId + "/" + id;
      rootNode = libraryRoot || ParseArgs.resolveNode(nodeRef);
      if (rootNode == null)
      {
         status.setCode(status.STATUS_NOT_FOUND, "Not a valid nodeRef: '" + nodeRef + "'");
         return null;
      }

      // Special case: make sure filter picks up correct mode
      if (type == null && args.filter == null)
      {
         args.filter = "node";
      }
   }
   else
   {
      /**
       * Site and container input
       */
      var siteId = url.templateArgs.site,
         containerId = url.templateArgs.container,
         siteNode = siteService.getSite(siteId);

      if (siteNode === null)
      {
         status.setCode(status.STATUS_GONE, "Site not found: '" + siteId + "'");
         return null;
      }

      rootNode = siteNode.getContainer(containerId);
      if (rootNode === null)
      {
         rootNode = siteNode.createContainer(containerId, containerType || "cm:folder");
         if (rootNode === null)
         {
            status.setCode(status.STATUS_GONE, "Document Library container '" + containerId + "' not found in '" + siteId + "'. (No permission?)");
            return null;
         }

         rootNode.properties["cm:description"] = "Document Library";

         /**
          * MOB-593: Add email alias on documentLibrary container creation
          *
         rootNode.addAspect("emailserver:aliasable");
         var emailAlias = siteId;
         if (containerId != "documentLibrary")
         {
            emailAlias += "-" + containerId;
         }
         rootNode.properties["emailserver:alias"] = emailAlias;
         */
         rootNode.save();
      }
   }

   if (args.filter == "unfiledRecords")
   {
      var container = rootNode.childrenByXPath("rma:Unfiled_x0020_Records");
      if (container.length > 0)
      {
         pathNode = container[0];
      }
   }
   else if (args.filter == "holds")
   {
      var container = rootNode.childrenByXPath("rma:Holds");
      if (container.length > 0)
      {
         pathNode = container[0];
      }
   }
   else if (args.filter == "transfers")
   {
      var container = rootNode.childrenByXPath("rma:Transfers");
      if (container.length > 0)
      {
         pathNode = container[0];
      }
   }
   else
   {
      // Path input?
      path = url.templateArgs.path || "";
      pathNode = path.length > 0 ? rootNode.childByNamePath(path) : (pathNode || rootNode);
   }

   if (pathNode === null)
   {
      status.setCode(status.STATUS_NOT_FOUND, "Path not found: '" + path + "'");
      return null;
   }

   // Parent location parameter adjustment
   location = Common.getLocation(pathNode, libraryRoot);
   if (location === null)
   {
      status.setCode(status.STATUS_GONE, "Location is 'null'. (No permission?)");
      return null;
   }
   if (path !== "")
   {
      location.path = ParseArgs.combinePaths(location.path, location.file);
   }
   if (args.filter !== "node" && !pathNode.isContainer)
   {
      location.file = "";
   }

   var objRet =
   {
      rootNode: rootNode,
      pathNode: pathNode,
      libraryRoot: libraryRoot,
      location: location,
      path: path,
      nodeRef: nodeRef,
      type: type
   };

   // Multiple input files in the JSON body?
   var files = ParseArgs.getMultipleInputValues("nodeRefs");
   if (typeof files != "string")
   {
      objRet.files = files;
   }

   return objRet;
};