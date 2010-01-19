var ParseArgs =
{
   /**
    * Get multiple input files
    *
    * @method getMultipleInputValues
    * @param param {string} Property name containing the files array
    * @return {array|string} Array containing the files, or string error
    */
   getMultipleInputValues: function ParseArgs_getMultipleInputValues(param)
   {
      var values = [];
      var error = null;

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
   },

   /**
    * Get and parse arguments
    *
    * @method getParsedArgs
    * @return {array|null} Array containing the validated input parameters
    */
   getParsedArgs: function ParseArgs_getParsedArgs(containerType)
   {
      var rootNode = null,
         parentNode = null,
         nodeRef = null,
         path = "",
         siteId, siteNode, containerId, type;

      if (url.templateArgs.store_type !== null)
      {
         /**
          * nodeRef input
          */
         var storeType = url.templateArgs.store_type,
            storeId = url.templateArgs.store_id,
            id = url.templateArgs.id,
            type = "node";

         nodeRef = storeType + "://" + storeId + "/" + id;

         if (nodeRef == "alfresco://company/home")
         {
            rootNode = companyhome;
         }
         else if (nodeRef == "alfresco://user/home")
         {
            rootNode = userhome;
         }
         else if (nodeRef == "alfresco://sites/home")
         {
            rootNode = companyhome.childrenByXPath("st:sites")[0];
         }
         else
         {
            rootNode = search.findNode(nodeRef);
            if (rootNode === null)
            {
               status.setCode(status.STATUS_NOT_FOUND, "Not a valid nodeRef: '" + nodeRef + "'");
               return null;
            }
            
            parentNode = rootNode.parent;
         }
      }
      else
      {
         /**
          * Site, component container input
          */
         siteId = url.templateArgs.site;
         containerId = url.templateArgs.container;
         type = url.templateArgs.type;

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

      // Path input
      path = url.templateArgs.path;
      if ((path !== null) && (path !== ""))
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

      // Resolve site, container and path for parentNode
      var location =
      {
         site: null,
         siteTitle: null,
         container: null,
         path: "/" + path
      };

      var qnamePaths = search.ISO9075Decode(parentNode.qnamePath).split("/"),
         displayPaths = parentNode.displayPath.split("/");

      if (parentNode.isContainer && type != "node")
      {
         displayPaths = displayPaths.concat([parentNode.name]);
      }

      if ((qnamePaths.length > 4) && (qnamePaths[2] == "st:sites"))
      {
         siteId = displayPaths[3];
         siteNode = siteService.getSite(siteId);
         if (siteNode != null)
         {
            containerId = qnamePaths[4].substr(3);
            location = 
            {
               site: siteId,
               siteTitle: siteNode.title,
               siteNode: siteNode,
               container: containerId,
               containerNode: siteNode.getContainer(containerId),
               path: "/" + displayPaths.slice(5, displayPaths.length).join("/")
            };
         }
      }

      var objRet =
      {
         rootNode: rootNode,
         parentNode: parentNode,
         path: path,
         location: location,
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
   }
};
