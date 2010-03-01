var ParseArgs =
{
   /**
    * Get and parse arguments
    *
    * @method getParsedArgs
    * @return {array|null} Array containing the validated input parameters
    */
   getParsedArgs: function ParseArgs_getParsedArgs(containerType)
   {
      var type = url.templateArgs.type,
         libraryRoot = args.libraryRoot,
         rootNode = null,
         pathNode = null,
         nodeRef = null,
         path = "";

      if (url.templateArgs.store_type !== null)
      {
         /**
          * nodeRef input: store_type, store_id and id
          */
         var storeType = url.templateArgs.store_type,
            storeId = url.templateArgs.store_id,
            id = url.templateArgs.id;

         nodeRef = storeType + "://" + storeId + "/" + id;
         rootNode = ParseArgs.resolveVirtualNodeRef(nodeRef);
         if (rootNode == null)
         {
            rootNode = search.findNode(nodeRef);
            if (rootNode === null)
            {
               status.setCode(status.STATUS_NOT_FOUND, "Not a valid nodeRef: '" + nodeRef + "'");
               return null;
            }
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
            status.setCode(status.STATUS_NOT_FOUND, "Site not found: '" + siteId + "'");
            return null;
         }

         rootNode = siteNode.getContainer(containerId);
         if (rootNode === null)
         {
            rootNode = siteNode.createContainer(containerId, containerType || "cm:folder");
            if (rootNode === null)
            {
               status.setCode(status.STATUS_NOT_FOUND, "Document Library container '" + containerId + "' not found in '" + siteId + "'. (No permission?)");
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

      // Path input?
      path = url.templateArgs.path || "";
      pathNode = path.length > 0 ? rootNode.childByNamePath(path) : rootNode;
      if (pathNode === null)
      {
         status.setCode(status.STATUS_NOT_FOUND, "Path not found: '" + path + "'");
         return null;
      }

      // Is this library rooted from a non-site nodeRef?
      if (libraryRoot !== null)
      {
         libraryRoot = ParseArgs.resolveVirtualNodeRef(libraryRoot) || search.findNode(libraryRoot);
      }

      var objRet =
      {
         rootNode: rootNode,
         pathNode: pathNode,
         libraryRoot: libraryRoot,
         location: Common.getLocation(pathNode, libraryRoot),
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
   },

   /**
    * Resolve "virtual" nodeRefs into nodes
    *
    * @method resolveVirtualNodeRef
    * @param virtualNodeRef {string} nodeRef
    * @return {ScriptNode|null} Node corresponding to supplied virtual nodeRef. Returns null if supplied nodeRef isn't a "virtual" type
    */
   resolveVirtualNodeRef: function ParseArgs_resolveVirtualNodeRef(nodeRef)
   {
      var node = null;
      if (nodeRef == "alfresco://company/home")
      {
         node = companyhome;
      }
      else if (nodeRef == "alfresco://user/home")
      {
         node = userhome;
      }
      else if (nodeRef == "alfresco://sites/home")
      {
         node = companyhome.childrenByXPath("st:sites")[0];
      }
      return node;
   },

   /**
    * Get multiple input files
    *
    * @method getMultipleInputValues
    * @param param {string} Property name containing the files array
    * @return {array|string} Array containing the files, or string error
    */
   getMultipleInputValues: function ParseArgs_getMultipleInputValues(param)
   {
      var values = [],
         error = null;

      try
      {
         // Was a JSON parameter list supplied?
         if (typeof json == "object")
         {
            if (!json.isNull(param))
            {
               var jsonValues = json.get(param);
               // Convert from JSONArray to JavaScript array
               for (var i = 0, j = jsonValues.length(); i < j; i++)
               {
                  values.push(jsonValues.get(i));
               }
            }
         }
      }
      catch(e)
      {
         error = e.toString();
      }

      // Return the values array, or the error string if it was set
      return (error !== null ? error : values);
   }
};
