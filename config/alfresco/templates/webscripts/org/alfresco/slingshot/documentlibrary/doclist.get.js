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
   var assets = new Array()
   
   // Is our thumbnail tpe registered?
   var haveThumbnails = thumbnailService.isThumbnailNameRegistered(THUMBNAIL_NAME);

   // Use helper function to get the arguments
   var parsedArgs = getParsedArgs();
   if (parsedArgs === null)
   {
      return;
   }

   // Try to find a filter query based on the passed-in arguments
   var allAssets;
   var filterQuery = getFilterQuery(filter, parsedArgs);
   if (filterQuery === null)
   {
      // Default to all children of parentNode
      allAssets = parsedArgs.parentNode.children;
   }
   else if (filterQuery == "node")
   {
      allAssets = [parsedArgs.rootNode];
   }
   else if (filterQuery == "tag")
   {
      allAssets = parsedArgs.rootNode.childrenByTags(args["filterData"]);
   }
   else
   {
      // Run the query returned from the filter
      allAssets = search.luceneSearch(filterQuery);
   }
   
   // Documents and/or folders?
   var showDocs = true;
   var showFolders = true;
   var type = url.templateArgs.type;
   if ((type !== null) && (type != ""))
   {
      showDocs = ((type == "all") || (type == "documents"));
      showFolders = ((type == "all") || (type == "folders"));
   }

   // Only interesting in folders and/or documents depending on passed-in type
   for each(asset in allAssets)
   {
      if ((asset.isContainer && showFolders) || (asset.isDocument && showDocs))
      {
         assets.push(asset);
      }
   }
   
   // Make a note of totalRecords before trimming the assets array
   var totalRecords = assets.length;

   // Sort the list before trimming to page chunks
   assets.sort(sortByType);
   
   // Pagination
   var pageSize = args["size"] || assets.length;
   var pagePos = args["pos"] || "1";
   var startIndex = (pagePos - 1) * pageSize;
   assets = assets.slice(startIndex, pagePos * pageSize);
   
   // Locked/working copy status defines action set
   var itemStatus, itemOwner, actionSet, thumbnail, createdBy, modifiedBy;
   
   for each(asset in assets)
   {
      itemStatus = [];
      itemOwner = null;
      createdBy = null;
      modifiedBy = null;
      
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
      
      // Get users
      createdBy = people.getPerson(asset.properties["cm:creator"]);
      modifiedBy = people.getPerson(asset.properties["cm:modifier"]);
      
      // Get relevant actions set
      actionSet = getActionSet(asset,
      {
         itemStatus: itemStatus,
         itemOwner: itemOwner
      });
      
      items.push(
      {
         asset: asset,
         status: itemStatus,
         owner: itemOwner,
         createdBy: createdBy,
         modifiedBy: modifiedBy,
         actionSet: actionSet,
         tags: asset.tags
      });
   }

   return (
   {
      luceneQuery: filterQuery,
      paging:
      {
         startIndex: startIndex,
         totalRecords: totalRecords
      },
      items: items
   });
}


function sortByType(a, b)
{
   if (a.isContainer == b.isContainer)
   {
      return (b.name.toLowerCase() > a.name.toLowerCase() ? -1 : 1);
   }
   return (a.isContainer ? -1 : 1);
}