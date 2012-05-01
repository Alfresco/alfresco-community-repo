function getDoclist(capabilitySet)
{
   // Use helper function to get the arguments
   var parsedArgs = ParseArgs.getParsedArgs("rma:filePlan");
   if (parsedArgs === null)
   {
      return;
   }

   var filter = args.filter,
      items = [];

   // Try to find a filter query based on the passed-in arguments
   var allNodes = [],
      favourites = Common.getFavourites(),
      filterParams = Filters.getFilterParams(filter, parsedArgs,
      {
         favourites: favourites
      }),
      query = filterParams.query;

   // Ensure folders and folderlinks appear at the top of the list
   var folderNodes = [],
      documentNodes = [];

   // Query the nodes - passing in sort and result limit parameters
   if (query !== "")
   {
      allNodes = search.query(
      {
         query: query,
         language: filterParams.language,
         page:
         {
            maxItems: (filterParams.limitResults ? parseInt(filterParams.limitResults, 10) : 0)
         },
         sort: filterParams.sort,
         templates: filterParams.templates,
         namespace: (filterParams.namespace ? filterParams.namespace : null)
      });
   }

   for each (node in allNodes)
   {
      try
      {
         if (node.isContainer || node.typeShort == "app:folderlink")
         {
            folderNodes.push(node);
         }
         else
         {
            documentNodes.push(node);
         }
      }
      catch (e)
      {
         // Possibly an old indexed node - ignore it
      }
   }
   
   // Node type counts
   var folderNodesCount = folderNodes.length,
      documentNodesCount = documentNodes.length,
      nodes, totalRecords;

   if (parsedArgs.type === "documents")
   {
      nodes = documentNodes;
   }
   else
   {
      nodes = folderNodes.concat(documentNodes);
   }
   totalRecords = nodes.length;
   
   // Pagination
   var pageSize = args.size || nodes.length,
      pagePos = args.pos || "1",
      startIndex = (pagePos - 1) * pageSize;

   // Trim the nodes array down to the page size
   nodes = nodes.slice(startIndex, pagePos * pageSize);

   // Common or variable parent container?
   var parent = null;
   
   if (!filterParams.variablePath)
   {
      var parentEval = Evaluator.run(parsedArgs.pathNode, capabilitySet);
      
      if (parentEval == null)
      {
         status.setCode(status.STATUS_BAD_REQUEST, "Not a Records Management folder: '" + parsedArgs.pathNode.nodeRef + "'");
         return null;
      }
      
      // Parent node permissions (and Site role if applicable)
      parent =
      {
         node: parsedArgs.pathNode,
         type: parentEval.assetType,
         userAccess: parentEval.permissions
      };
   }

   var isThumbnailNameRegistered = thumbnailService.isThumbnailNameRegistered(THUMBNAIL_NAME),
      thumbnail = null,
      filePlanLocation = Common.getLocation(parsedArgs.rootNode);
   
   // Loop through and evaluate each node in this result set
   for each (node in nodes)
   {
      // Does this collection of nodes have potentially differering paths?
      if (filterParams.variablePath)
      {
         location = Common.getLocation(node);
      }
      else
      {
         location =
         {
            site: parsedArgs.location.site,
            siteTitle: parsedArgs.location.siteTitle,
            container: parsedArgs.location.container,
            path: parsedArgs.location.path,
            file: node.name
         };
      }
      
      // Is our thumbnail type registered?
      if (isThumbnailNameRegistered && node.isSubType("cm:content") && item.node.properties.content.inputStream != null)
      {
         // Make sure we have a thumbnail.
         thumbnail = node.getThumbnail(THUMBNAIL_NAME);
         if (thumbnail === null)
         {
            // No thumbnail, so queue creation
            node.createThumbnail(THUMBNAIL_NAME, true);
         }
      }

      // Get evaluated properties
      nodeEvaluator = Evaluator.run(node, capabilitySet);
      
      if (nodeEvaluator != null)
      {
         items.push(
         {
            node: node,
            isLink: false,
            type: nodeEvaluator.assetType,
            createdBy: nodeEvaluator.createdBy,
            modifiedBy: nodeEvaluator.modifiedBy,
            status: nodeEvaluator.status,
            actionSet: nodeEvaluator.actionSet,
            actionPermissions: nodeEvaluator.permissions,
            suppressRoles: nodeEvaluator.suppressRoles,
            dod5015: jsonUtils.toJSONString(nodeEvaluator.metadata),
            tags: node.tags,
            location: location
         });
      }
   }

   return (
   {
      luceneQuery: query,
      paging:
      {
         startIndex: startIndex,
         totalRecords: totalRecords
      },
      filePlan: filePlanLocation.containerNode,
      parent: parent,
      items: items
   });
}
