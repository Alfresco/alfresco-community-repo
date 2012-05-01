<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/parse-args.lib.js">

/**
 * Document List Component: treenode
 */
model.treenode = getTreenode();

/* Create collection of folders in the given space */
function getTreenode()
{
   try
   {
      var items = new Array(),
         hasSubfolders = true,
         ignoredTypes =
         {
            "{http://www.alfresco.org/model/forum/1.0}forum": true,
            "{http://www.alfresco.org/model/forum/1.0}topic": true,
            "{http://www.alfresco.org/model/content/1.0}systemfolder": true
         },
         skipPermissionCheck = args["perms"] == "false",
         evalChildFolders = args["children"] !== "false",
         item, rmNode, capabilities, cap;
   
      // Use helper function to get the arguments
      var parsedArgs = ParseArgs.getParsedArgs();
      if (parsedArgs === null)
      {
         return;
      }

      // Quick version if "skipPermissionCheck" flag set
      if (skipPermissionCheck)
      {
         for each (item in parsedArgs.pathNode.children)
         {
            if (itemIsAllowed(item) && !(item.type in ignoredTypes))
            {
               if (evalChildFolders)
               {
                  hasSubfolders = item.childFileFolders(false, true, "fm:forum").length > 0;
               }

               items.push(
               {
                  node: item,
                  hasSubfolders: hasSubfolders
               });
            }
         }
      }
      else
      {
         for each (item in parsedArgs.pathNode.children)
         {
            if (itemIsAllowed(item) && !(item.type in ignoredTypes))
            {
               capabilities = {};
               rmNode = rmService.getRecordsManagementNode(item);
               for each (cap in rmNode.capabilitiesSet("Create"))
               {
                  capabilities[cap.name] = true;
               }

               if (evalChildFolders)
               {
                  hasSubfolders = item.childFileFolders(false, true, "fm:forum").length > 0;
               }

               items.push(
               {
                  node: item,
                  hasSubfolders: hasSubfolders,
                  permissions:
                  {
                     create: capabilities["Create"]
                  }
               });
            }
         }
      }
   
      items.sort(sortByName);
   
      return (
      {
         parent: parsedArgs.pathNode,
         resultsTrimmed: false,
         items: items
      });
   }
   catch(e)
   {
      status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, e.toString());
      return;
   }
}


/* Sort the results by case-insensitive name */
function sortByName(a, b)
{
   return (b.node.name.toLowerCase() > a.node.name.toLowerCase() ? -1 : 1);
}

/* Filter allowed types, etc. */
function itemIsAllowed(item)
{
   // Must be a subtype of cm:folder
   if (!item.isSubType("cm:folder"))
   {
      return false;
   }
   
   var typeShort = String(item.typeShort);
   
   // Don't show Hold and Transfer top-level containers
   if (typeShort == "rma:hold" || typeShort == "rma:transfer")
   {
      return false;
   }
   
   // Must be a "dod:" or "rma:" namespaced type
   if (typeShort.indexOf("dod:") !== 0 && typeShort.indexOf("rma") !== 0)
   {
      return false;
   }
   
   return true;
}