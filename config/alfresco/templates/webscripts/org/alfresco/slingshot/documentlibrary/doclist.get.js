<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/action-sets.lib.js">
<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/filters.lib.js">
<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/parse-args.lib.js">

const THUMBNAIL_NAME = "doclib",
   PREF_FAVOURITES = "org.alfresco.share.documents.favourites";

var PeopleCache = {},
   SiteCache = {};

/**
 * Gets / caches a person object
 * @method getPerson
 * @param username {string} User name
 */
function getPerson(username)
{
   if (typeof PeopleCache[username] == "undefined")
   {
      PeopleCache[username] = people.getPerson(username);
   }
   return PeopleCache[username];
}

/**
 * Gets / caches a site object
 * @method getSite
 * @param siteId {string} Site ID
 */
function getSite(siteId)
{
   if (typeof SiteCache[siteId] == "undefined")
   {
      SiteCache[siteId] = siteService.getSite(siteId);
   }
   return SiteCache[siteId];
}

/**
 * Main entry point: Create collection of documents and folders in the given space
 * @method main
 */
function main()
{
   var filter = args.filter,
      items = [],
      assets;
   
   // Is our thumbnail type registered?
   var haveThumbnails = thumbnailService.isThumbnailNameRegistered(THUMBNAIL_NAME);

   // Use helper function to get the arguments
   var parsedArgs = ParseArgs.getParsedArgs();
   if (parsedArgs === null)
   {
      return;
   }

   // "node" Type implies single nodeRef requested
   if (!filter && parsedArgs.type === "node")
   {
      filter = "node";
   }

   // Get the user's favourite docs from our slightly eccentric Preferences Service
   var prefs = preferenceService.getPreferences(person.properties.userName, PREF_FAVOURITES),
      favourites = {};
   try
   {
      /**
       * Fasten seatbelts...
       * An "eval" could be used here, but the Rhino debugger will complain if throws an exception, which gets old very quickly.
       * e.g. var strFavs = eval('try{(prefs.' + PREF_FAVOURITES + ')}catch(e){}');
       */
      if (prefs && prefs.org && prefs.org.alfresco && prefs.org.alfresco.share && prefs.org.alfresco.share.documents)
      {
         var strFavs = prefs.org.alfresco.share.documents.favourites;
         if (typeof strFavs == "string")
         {
            arrFavs = strFavs.split(",");
            for (var f = 0, ff = arrFavs.length; f < ff; f++)
            {
               favourites[arrFavs[f]] = true;
            }
         }
      }
   }
   catch (e)
   {
   }

   // Try to find a filter query based on the passed-in arguments
   var allAssets,
      filterParams = Filters.getFilterParams(filter, parsedArgs,
      {
         favourites: favourites
      }),
      query = filterParams.query;

   // Query and sort the list before trimming to page chunks below
   allAssets = search.luceneSearch(query, filterParams.sortBy, filterParams.sortByAscending, filterParams.limitResults ? filterParams.limitResults : 0);

   // Ensure folders and folderlinks appear at the top of the list
   folderAssets = [];
   documentAssets = [];
   for each (asset in allAssets)
   {
      try
      {
         if (asset.isContainer || asset.type == "{http://www.alfresco.org/model/application/1.0}folderlink")
         {
            folderAssets.push(asset);
         }
         else
         {
            documentAssets.push(asset);
         }
      }
      catch (e)
      {
         // Possibly an old indexed node - ignore it
      }
   }
   
   var folderAssetsCount = folderAssets.length,
      documentAssetsCount = documentAssets.length;
   
   if (parsedArgs.type === "documents")
   {
      assets = documentAssets;
   }
   else
   {
      assets = folderAssets.concat(documentAssets);
   }
   
   // Make a note of totalRecords before trimming the assets array
   var totalRecords = assets.length;

   // Pagination
   var pageSize = args.size || assets.length,
      pagePos = args.pos || "1",
      startIndex = (pagePos - 1) * pageSize;
   
   assets = assets.slice(startIndex, pagePos * pageSize);
   
   var itemStatus, itemOwner, actionSet, thumbnail, createdBy, modifiedBy, activeWorkflows, assetType, linkAsset, isLink,
      location, qnamePaths, displayPaths, locationAsset;

   // Location if we're in a site
   var defaultLocation =
   {
      site: parsedArgs.location.site,
      siteTitle: parsedArgs.location.siteTitle,
      container: parsedArgs.location.container,
      path: parsedArgs.location.path,
      file: null
   };
   
   // User permissions and role
   var user =
   {
      permissions:
      {
   	   create: parsedArgs.parentNode.hasPermission("CreateChildren"),
   	   edit: parsedArgs.parentNode.hasPermission("Write"),
   	   "delete": parsedArgs.parentNode.hasPermission("Delete")
      }
   };
   if (defaultLocation.site !== null)
   {
      user.role = parsedArgs.location.siteNode.getMembersRole(person.properties.userName);
   }
   
   // Locked/working copy status defines action set
   for each (asset in assets)
   {
      itemStatus = [];
      itemOwner = null;
      createdBy = null;
      modifiedBy = null;
      activeWorkflows = [];
      linkAsset = null;
      isLink = false;

      // Asset status
      if (asset.isLocked)
      {
         itemStatus.push("locked");
         itemOwner = getPerson(asset.properties["cm:lockOwner"]);
      }
      if (asset.hasAspect("cm:workingcopy"))
      {
         itemStatus.push("workingCopy");
         itemOwner = getPerson(asset.properties["cm:workingCopyOwner"]);
      }
      // Is this user the item owner?
      if (itemOwner && (itemOwner.properties.userName == person.properties.userName))
      {
         itemStatus.push("lockedBySelf");
      }
      
      // Get users
      createdBy = getPerson(asset.properties["cm:creator"]);
      modifiedBy = getPerson(asset.properties["cm:modifier"]);
      
      // Asset type
      if (asset.isContainer)
      {
         assetType = "folder";
      }
      else if (asset.type == "{http://www.alfresco.org/model/application/1.0}folderlink")
      {
         assetType = "folder";
         isLink = true;
      }
      else if (asset.type == "{http://www.alfresco.org/model/application/1.0}filelink")
      {
         assetType = "document";
         isLink = true;
      }
      else
      {
         assetType = "document";
      }
      
      if (isLink)
      {
         /**
          * NOTE: After this point, the "asset" object will be changed to a link's destination node
          *       if the original node was a filelink type
          */
         linkAsset = asset;
         asset = linkAsset.properties.destination;
      }
      
      // Does this collection of assets have potentially differering paths?
      if (filterParams.variablePath || isLink)
      {
         locationAsset = (isLink && assetType == "document") ? linkAsset : asset;

         qnamePaths = locationAsset.qnamePath.split("/");
         displayPaths = locationAsset.displayPath.split("/");

         if ((qnamePaths.length > 5) && (qnamePaths[2] == "st:sites"))
         {
            // This asset belongs to a site
            location =
            {
               site: displayPaths[3],
               container: displayPaths[4],
               path: "/" + displayPaths.slice(5, displayPaths.length).join("/"),
               file: locationAsset.name
            };
            var site = getSite(location.site);
            if (site != null)
            {
               location.siteTitle = site.title;
            }
         }
         else
         {
            location =
            {
               site: null,
               siteTitle: null,
               container: null,
               path: null,
               file: locationAsset.name
            };
         }
      }
      else
      {
         location =
         {
            site: defaultLocation.site,
            siteTitle: defaultLocation.siteTitle,
            container: defaultLocation.container,
            path: defaultLocation.path,
            file: asset.name
         };
      }

      // Make sure we have a thumbnail
      if (haveThumbnails)
      {
         thumbnail = asset.getThumbnail(THUMBNAIL_NAME);
         if (thumbnail === null)
         {
            // No thumbnail, so queue creation
            asset.createThumbnail(THUMBNAIL_NAME, true);
         }
      }
      
      // Get relevant actions set
      actionSet = getActionSet(asset,
      {
         assetType: assetType,
         isLink: isLink,
         itemStatus: itemStatus,
         itemOwner: itemOwner
      });
      
      // Part of an active workflow?
      for each (activeWorkflow in asset.activeWorkflows)
      {
         activeWorkflows.push(activeWorkflow.id);
      }
      
      items.push(
      {
         asset: asset,
         linkAsset: linkAsset,
         type: assetType,
         isLink: isLink,
         status: itemStatus,
         owner: itemOwner,
         createdBy: createdBy,
         modifiedBy: modifiedBy,
         actionSet: actionSet,
         tags: asset.tags,
         activeWorkflows: activeWorkflows,
         location: location,
         isFavourite: (favourites[asset.nodeRef] === true)
      });
   }

   return (
   {
      luceneQuery: query,
      onlineEditing: utils.moduleInstalled("org.alfresco.module.vti"),
      itemCount:
      {
         folders: folderAssetsCount,
         documents: documentAssetsCount
      },
      paging:
      {
         startIndex: startIndex,
         totalRecords: totalRecords
      },
      user: user,
      items: items,
      parent: filterParams.variablePath ? null : parsedArgs.parentNode
   });
}

/**
 * Document List Component: doclist
 */
model.doclist = main();