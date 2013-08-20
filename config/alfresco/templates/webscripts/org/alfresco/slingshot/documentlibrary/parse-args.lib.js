const THUMBNAIL_NAME = "doclib",
   TYPE_SITES = "st:sites",
   PREF_DOCUMENT_FAVOURITES = "org.alfresco.share.documents.favourites",
   PREF_FOLDER_FAVOURITES = "org.alfresco.share.folders.favourites",
   LIKES_SCHEME = "likesRatingScheme";

var Common =
{
   /**
    * Cache for person objects
    */
   PeopleCache: {},

   /**
    * Gets / caches a person object
    *
    * @method getPerson
    * @param username {string} User name
    */
   getPerson: function Common_getPerson(username)
   {
      if (username == null || username == "")
      {
         return null;
      }

      if (typeof Common.PeopleCache[username] != "object")
      {
         var person = people.getPerson(username);
         if (person == null)
         {
            if (username == "System" || username.match("^System@") == "System@")
            {
               // special case for the System users
               person =
               {
                  properties:
                  {
                     userName: "System",
                     firstName: "System",
                     lastName: "User"
                  },
                  assocs: {}
               };
            }
            else
            {
               // missing person - may have been deleted from the database
               person =
               {
                  properties:
                  {
                     userName: username,
                     firstName: "",
                     lastName: ""
                  },
                  assocs: {}
               };
            }
         }
         Common.PeopleCache[username] =
         {
            userName: person.properties.userName,
            firstName: person.properties.firstName,
            lastName: person.properties.lastName,
            displayName: (person.properties.firstName + " " + person.properties.lastName).replace(/^\s+|\s+$/g, "")
         };
         if (person.assocs["cm:avatar"] != null)
         {
            Common.PeopleCache[username].avatar = person.assocs["cm:avatar"][0];
         }
      }
      return Common.PeopleCache[username];
   },

   /**
    * Cache for group objects
    */
   GroupCache: {},

   /**
    * Gets / caches a group object
    *
    * @method getGroup
    * @param groupname {string} Group name
    */
   getGroup: function Common_getGroup(groupname)
   {
      if (groupname == null || groupname == "")
      {
         return null;
      }

      if (typeof Common.GroupCache[groupname] != "object")
      {
         var group = groups.getGroupForFullAuthorityName(groupname);
         if (group == null && groupname == "GROUP_EVERYONE")
         {
            group =
            {
               fullName: groupname,
               shortName: "EVERYONE",
               displayName: "EVERYONE"
            };
         }
         Common.GroupCache[groupname] = group;
      }
      return Common.GroupCache[groupname];
   },

   /**
    * Cache for site objects
    */
   SiteCache: {},

   /**
    * Gets / caches a site object
    *
    * @method getSite
    * @param siteId {string} Site ID
    */
   getSite: function Common_getSite(siteId)
   {
      if (typeof Common.SiteCache[siteId] != "object")
      {
         Common.SiteCache[siteId] = siteService.getSite(siteId);
      }
      return Common.SiteCache[siteId];
   },

   /**
    * Get the user's favourite docs and folders from our slightly eccentric Preferences Service
    *
    * @method getFavourites
    */
   getFavourites: function Common_getFavourites()
   {
      var prefs = preferenceService.getPreferences(person.properties.userName, PREF_DOCUMENT_FAVOURITES),
         favourites = {},
         arrFavs = [],
         strFavs, f, ff;
      try
      {
         /**
          * Fasten seatbelts...
          * An "eval" could be used here, but the Rhino debugger will complain if throws an exception, which gets old very quickly.
          * e.g. var strFavs = eval('try{(prefs.' + PREF_DOCUMENT_FAVOURITES + ')}catch(e){}');
          */
         if (prefs && prefs.org && prefs.org.alfresco && prefs.org.alfresco.share && prefs.org.alfresco.share.documents)
         {
            strFavs = prefs.org.alfresco.share.documents.favourites;
            if (typeof strFavs == "string")
            {
               arrFavs = strFavs.split(",");
               for (f = 0, ff = arrFavs.length; f < ff; f++)
               {
                  favourites[arrFavs[f]] = true;
               }
            }
         }
         // Same thing but for folders
         prefs = preferenceService.getPreferences(person.properties.userName, PREF_FOLDER_FAVOURITES);
         if (prefs && prefs.org && prefs.org.alfresco && prefs.org.alfresco.share && prefs.org.alfresco.share.folders)
         {
            strFavs = prefs.org.alfresco.share.folders.favourites;
            if (typeof strFavs == "string")
            {
               arrFavs = strFavs.split(",");
               for (f = 0, ff = arrFavs.length; f < ff; f++)
               {
                  favourites[arrFavs[f]] = true;
               }
            }
         }
      }
      catch (e)
      {
      }
   
      return favourites;
   },
   
   /**
    * Returns the number of nested levels at which two paths match. This is used when there are two 
    * parents to a node and is used to determine the correct one.
    * 
    * @param count
    * @param firstNode An array of Strings that are the elements in a path to a node.
    * @param secondNode An array of Strings that are the elements in a path to a different node.
    */
   getDeepOfPath: function Common_getDeepOfPath(count, firstNode, secondNode)
   {
      var i = 0;
      for (var i = 0; i < count; i++)
      {
         if (!firstNode[i].equals(secondNode[i]))
         {
            break;
         }
      }
      return i;
   }, 
   
   /**
    * Generates a location object literal for a given node.
    * Location is Site-relative unless a libraryRoot node is passed in.
    *
    * @method getLocation
    * @param node {ScriptNode} Node to generate location for
    * @param libraryRoot {ScriptNode} Optional node to work out relative location from.
    * @return {object} Location object literal.
    */
   getLocation: function Common_getLocation(node, libraryRoot)
   {
      var location = null,
         qnamePaths = node.qnamePath.split("/"),
         displayPaths = node.displayPath.split("/");

      if (libraryRoot == undefined && qnamePaths[2] != TYPE_SITES)
      {
         libraryRoot = companyhome;
      }

      if (libraryRoot)
      {
         var libraryRootDisplayPath = libraryRoot.displayPath.split("/");
         var deepLibraryRoot = libraryRootDisplayPath.length;
         var deepNode = displayPaths.length; 

         var count = (deepNode > deepLibraryRoot) ? Common.getDeepOfPath(deepNode, displayPaths, libraryRootDisplayPath): Common.getDeepOfPath(deepLibraryRoot, libraryRootDisplayPath, displayPaths);

         if (node.qnamePath.contains(libraryRoot.qnamePath))
         {
            count++;
         }

         // Generate the path from the supplied library root
         location =
         {
            site: null,
            siteTitle: null,
            container: null,
            path: "/" + displayPaths.slice(count, deepNode).join("/"),
            file: node.name
         };
      } 
      else if ((qnamePaths.length > 4) && (qnamePaths[2] == TYPE_SITES))
      {
         var siteId = displayPaths[3],
            siteNode = Common.getSite(siteId),
            containerId = qnamePaths[4].substr(3);

         if (siteNode != null)
         {
            location = 
            {
               site: siteId,
               siteNode: siteNode,
               siteTitle: siteNode.title,
               container: containerId,
               containerNode: siteNode.getContainer(containerId),
               path: "/" + displayPaths.slice(5, displayPaths.length).join("/"),
               file: node.name
            };
         }
      }
      
      if (location == null)
      {
         location =
         {
            site: null,
            siteTitle: null,
            container: null,
            path: "/" + displayPaths.slice(2, displayPaths.length).join("/"),
            file: node.name
         };
      }
      
      return location;
   },

   /**
    * Returns an object literal representing the current "likes" rating for a node
    *
    * @method getLikes
    * @param node {ScriptNode} Node to query
    * @return {object} Likes object literal.
    */
   getLikes: function Common_getLikes(node)
   {
      var isLiked = false,
         totalLikes = 0;

      try
      {
         totalLikes = ratingService.getRatingsCount(node, LIKES_SCHEME);
         isLiked = totalLikes === 0 ? false : ratingService.getRating(node, LIKES_SCHEME) !== -1;
      }
      catch (e) {}
      
      return (
      {
         isLiked: isLiked,
         totalLikes: totalLikes
      });
   }
};

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
         if (url.templateArgs.store_type == "workspace")
         {
            rootNode = ParseArgs.resolveNode(nodeRef);
         }
         else
         {
            rootNode = libraryRoot || ParseArgs.resolveNode(nodeRef);
         }
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
            rootNode = siteNode.aquireContainer(containerId, containerType || "cm:folder", {"cm:description": "Document Library"});
            if (rootNode === null)
            {
               status.setCode(status.STATUS_GONE, "Document Library container '" + containerId + "' not found in '" + siteId + "'. (No permission?)");
               return null;
            }
         }
      }

      // Path input?
      path = url.templateArgs.path || "";
      pathNode = path.length > 0 ? rootNode.childByNamePath(path) : (pathNode || rootNode);
      if (pathNode === null)
      {
         status.setCode(status.STATUS_NOT_FOUND, "Path not found: '" + path + "'");
         return null;
      }

      // Parent location parameter adjustment
      location = Common.getLocation(pathNode, libraryRoot);
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
   },

   /**
    * Resolve "virtual" nodeRefs into nodes
    *
    * @method resolveVirtualNodeRef
    * @deprecated for ParseArgs.resolveNode
    */
   resolveVirtualNodeRef: function ParseArgs_resolveVirtualNodeRef(nodeRef)
   {
      if (logger.isLoggingEnabled())
      {
         logger.log("WARNING: ParseArgs.resolveVirtualNodeRef is deprecated for ParseArgs.resolveNode");
      }
      return ParseArgs.resolveNode(nodeRef);
   },

   /**
    * Resolve "virtual" nodeRefs, nodeRefs and xpath expressions into nodes
    *
    * @method resolveNode
    * @param reference {string} "virtual" nodeRef, nodeRef or xpath expressions
    * @return {ScriptNode|null} Node corresponding to supplied expression. Returns null if node cannot be resolved.
    */
   resolveNode: function ParseArgs_resolveNode(reference)
   {
      var node = null;
      try
      {
         if (reference == "alfresco://company/home")
         {
            node = companyhome;
         }
         else if (reference == "alfresco://user/home")
         {
            node = userhome;
         }
         else if (reference == "alfresco://sites/home")
         {
            node = companyhome.childrenByXPath("st:sites")[0];
         }
         else if (reference == "alfresco://shared")
         {
            node = companyhome.childrenByXPath("app:shared")[0];
         }
         else if (reference.indexOf("://") > 0)
         {
            if (reference.indexOf(":") < reference.indexOf("://"))
            {
               var newRef = "/" + reference.replace("://", "/");
               var newRefNodes = search.xpathSearch(newRef);
               node = search.findNode(String(newRefNodes[0].nodeRef));
            }
            else
            {
               node = search.findNode(reference);
            }
         }
         else if (reference.substring(0, 1) == "/")
         {
            node = search.xpathSearch(reference)[0];
         }
      }
      catch (e)
      {
         return null;
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
   },

   /**
    * Append multiple parts of a path, ensuring duplicate path separators are removed.
    *
    * @method combinePaths
    * @param path1 {string} First path
    * @param path2 {string} Second path
    * @param ...
    * @param pathN {string} Nth path
    * @return {string} A string containing the combined paths
    */
   combinePaths: function ParseArgs_combinePaths()
   {
      var path = "", i, ii;
      for (i = 0, ii = arguments.length; i < ii; i++)
      {
         if (arguments[i] !== null)
         {
            path += arguments[i] + (arguments[i] !== "/" ? "/" : "");
         }
      }

      return path.replace(/\/{2,}/g, "/").replace(/(.)\/$/g, "$1");
   }
};
