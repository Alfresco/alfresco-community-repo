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

   // Try to find a filter query based on the passed-in arguments
   var filterQuery = getFilterQuery(filter, parsedArgs);
   if (filterQuery === null)
   {
      // Default to all children of parentNode
      assets = parsedArgs.parentNode.children;
   }
   else
   {
      // Run the query returned from the filter
      assets = search.luceneSearch(filterQuery);
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
   
   // Locked/working copy status defines action set
   var itemStatus, itemOwner, actionSet, thumbnail;
   
   for each(asset in assets)
   {
      itemStatus = [];
      itemOwner = "";
      
      if ((asset.isContainer && showFolders) || (asset.isDocument && showDocs))
      {
         if (asset.isLocked)
         {
            itemStatus.push("locked");
            itemOwner = asset.properties["cm:lockOwner"];
         }
         if (asset.hasAspect("cm:workingcopy"))
         {
            itemStatus.push("workingCopy");
            itemOwner = asset.properties["cm:workingCopyOwner"];
         }
         // Is this user the item owner?
         if (itemOwner == person.properties.userName)
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
            actionSet: actionSet
         });
      }
   }

   items.sort(sortByType);

   return (
   {
      "luceneQuery": filterQuery,
      "items": items
   });
}


function sortByType(a, b)
{
   if (a.asset.isContainer == b.asset.isContainer)
   {
      return (b.asset.name.toLowerCase() > a.asset.name.toLowerCase() ? -1 : 1);
   }
   return (a.asset.isContainer ? -1 : 1);
}