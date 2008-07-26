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
   else if (filterQuery == "node")
   {
      assets = [parsedArgs.rootNode];
   }
   else if (filterQuery == "tag")
   {
      assets = parsedArgs.rootNode.childrenByTags(args["filterData"]);
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
   var itemStatus, itemOwner, actionSet, thumbnail, createdBy, modifiedBy;
   
   for each(asset in assets)
   {
      itemStatus = [];
      itemOwner = null;
      createdBy = null;
      modifiedBy = null;
      
      if ((asset.isContainer && showFolders) || (asset.isDocument && showDocs))
      {
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