<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/action-sets.lib.js">
<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/filters.lib.js">
<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/parse-args.lib.js">
var THUMBNAIL_NAME = "doclib";

/**
 * Document List Component: doclist
 */
model.doclist = getDocList(args["filter"]);

/* Create collection of documents and folders in the given space */
function getDocList(filter)
{
   var items = new Array();
   var assets;
   
   // Is our thumbnail tpe registered?
   var haveThumbnails = thumbnailService.isThumbnailNameRegistered(THUMBNAIL_NAME);

   // Use helper function to get the arguments
   var parsedArgs = getParsedArgs();
   if (parsedArgs === null)
   {
      return;
   }

   // Type missing implies single nodeRef requested
   if (url.templateArgs.type === null)
   {
      filter = "node";
   }

   // Try to find a filter query based on the passed-in arguments
   var allAssets, filterQuery, query;
   var filterParams = getFilterParams(filter, parsedArgs);
   query = filterParams.query;

   // Specialise by passed-in type
   var typeQuery = getTypeFilterQuery(url.templateArgs.type);
   query += " " + typeQuery;

   // Sort the list before trimming to page chunks 
   allAssets = search.luceneSearch(query, filterParams.sortBy, filterParams.sortByAscending);

   // Limit the resultset?
   if (filterParams.limitResults)
   {
      /**
       * This isn't a true results trim (page-trimming is done below), as we haven't yet filtered by type.
       * However, it's useful for a quick slimming-down of the "recently..." queries.
       */
      allAssets = allAssets.slice(0, filterParams.limitResults);
   }
      
   // Ensure folders and folderlinks appear at the top of the list
   folderAssets = new Array();
   documentAssets = new Array();
   for each(asset in allAssets)
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
   assets = folderAssets.concat(documentAssets);
   
   // Make a note of totalRecords before trimming the assets array
   var totalRecords = assets.length;

   // Pagination
   var pageSize = args["size"] || assets.length;
   var pagePos = args["pos"] || "1";
   var startIndex = (pagePos - 1) * pageSize;
   assets = assets.slice(startIndex, pagePos * pageSize);
   
   var itemStatus, itemOwner, actionSet, thumbnail, createdBy, modifiedBy, activeWorkflows, assetType, linkAsset, isLink;
   var defaultLocation, location, qnamePaths, displayPaths, locationAsset;

   // Location if we're in a site
   var defaultLocation =
   {
      site: parsedArgs.location.site,
      container: parsedArgs.location.container,
      path: parsedArgs.location.path,
      file: null
   }
   
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
      user.role = parsedArgs.location.siteNode.getMembersRole(person.properties["userName"]);
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
         itemOwner = people.getPerson(asset.properties["cm:lockOwner"]);
      }
      if (asset.hasAspect("cm:workingcopy"))
      {
         itemStatus.push("workingCopy");
         itemOwner = people.getPerson(asset.properties["cm:workingCopyOwner"]);
      }
      // Is this user the item owner?
      if (itemOwner && (itemOwner.properties.userName == person.properties.userName))
      {
         itemStatus.push("lockedBySelf");
      }
      
      // Get users
      createdBy = people.getPerson(asset.properties["cm:creator"]);
      modifiedBy = people.getPerson(asset.properties["cm:modifier"]);
      
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
               site: qnamePaths[3].substr(3),
               container: qnamePaths[4].substr(3),
               path: "/" + displayPaths.slice(5, displayPaths.length).join("/"),
               file: locationAsset.name
            }
         }
         else
         {
            location =
            {
               site: null,
               container: null,
               path: null,
               file: locationAsset.name
            }
         }
      }
      else
      {
         location =
         {
            site: defaultLocation.site,
            container: defaultLocation.container,
            path: defaultLocation.path,
            file: asset.name
         }
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
         location: location
      });
   }

   return (
   {
      luceneQuery: query,
      onlineEditing: utils.moduleInstalled("org.alfresco.module.vti"),
      paging:
      {
         startIndex: startIndex,
         totalRecords: totalRecords
      },
      user: user,
      items: items
   });
}
