var Evaluator =
{
   /**
    * Asset Type evaluator
    */
   getAssetType: function Evaluator_getAssetType(asset)
   {
      var assetType = "";
      if (asset.isContainer)
      {
         assetType = "folder";
      }
      else if (asset.typeShort == "app:folderlink")
      {
         assetType = "folderlink";
      }
      else if (asset.typeShort == "app:filelink")
      {
         assetType = "filelink";
      }
      else
      {
         assetType = "document";
      }
      return assetType;
   },
   
   /**
    * Asset Evaluator - main entrypoint
    */
   run: function Evaluator_run(asset)
   {
      var assetType = Evaluator.getAssetType(asset),
         actions = {},
         actionSet = "empty",
         permissions = {},
         status = {},
         custom = {},
         activeWorkflows = [],
         createdBy = getPerson(asset.properties["cm:creator"]),
         modifiedBy = getPerson(asset.properties["cm:modifier"]),
         isLink = false,
         linkAsset = null,
         lockedBy = null,
         lockOwnerUser = "";

      /**
       * COMMON TO ALL
       */
      permissions =
      {
         "create": asset.hasPermission("CreateChildren"),
         "edit": asset.hasPermission("Write"),
         "delete": asset.hasPermission("Delete"),
         "permissions": asset.hasPermission("ChangePermissions"),
         "cancel-checkout": asset.hasPermission("CancelCheckOut")
      };

      // Get relevant actions set
      switch (assetType)
      {
         /**
          * SPECIFIC TO: LINK
          */
         case "folderlink":
         case "filelink":
            actionSet = "link";
            isLink = true;
            /**
             * NOTE: After this point, the "asset" object will be changed to a link's destination node
             *       if the original node was a filelink type.
             */
            linkAsset = asset;
            asset = linkAsset.properties.destination;
            // Re-evaluate the assetType based on the link's destination node
            assetType = Evaluator.getAssetType(asset);
            break;
         
         /**
          * SPECIFIC TO: FOLDER
          */
         case "folder":
            actionSet = "folder";
            break;

         /**
          * SPECIFIC TO: DOCUMENTS
          */
         case "document":
            actionSet = "document";
            
            // Working Copy?
            if (asset.hasAspect("cm:workingcopy"))
            {
               lockedBy = getPerson(asset.properties["cm:workingCopyOwner"]);
               lockOwnerUser = lockedBy.userName;
               if (lockOwnerUser == person.properties.userName)
               {
                  status["editing"] = true;
                  actionSet = "workingCopyOwner";
               }
               else
               {
                  status["locked " + lockedBy.displayName + "|" + lockedBy.userName] = true;
                  actionSet = "locked";
               }
               var wcNode = asset.properties["source"];
               custom["isWorkingCopy"] = true;
               custom["workingCopyOriginal"] = wcNode.nodeRef;
               if (wcNode.hasAspect("cm:versionable") && wcNode.versionHistory.length > 0)
               {
                  custom["workingCopyVersion"] = wcNode.versionHistory[0].label;
               }
               permissions["view-original"] = true;
            }
            // Locked?
            else if (asset.isLocked)
            {
               lockedBy = getPerson(asset.properties["cm:lockOwner"]);
               lockOwnerUser = lockedBy.userName;
               if (lockOwnerUser == person.properties.userName)
               {
                  status["lock-owner"] = true;
                  actionSet = "lockOwner";
               }
               else
               {
                  status["locked " + lockedBy.displayName + "|" + lockedBy.userName] = true;
                  actionSet = "locked";
               }
               var srcNodes = search.query(
               {
                  query: "+@cm\\:source:\"" + asset.nodeRef + "\" +ISNOTNULL:cm\\:workingCopyOwner",
                  language: "lucene",
                  page:
                  {
                     maxItems: 1
                  }
               });
               if (srcNodes.length == 1)
               {
                  custom["hasWorkingCopy"] = true;
                  custom["workingCopyNode"] = srcNodes[0].nodeRef;
                  permissions["view-working-copy"] = true;
               }
            }
            break;
      }
      
      // Part of an active workflow?
      for each (activeWorkflow in asset.activeWorkflows)
      {
         activeWorkflows.push(activeWorkflow.id);
      }

      return(
      {
         asset: asset,
         type: assetType,
         linkAsset: linkAsset,
         isLink: isLink,
         status: status,
         actionSet: actionSet,
         actionPermissions: permissions,
         createdBy: createdBy,
         modifiedBy: modifiedBy,
         lockedBy: lockedBy,
         tags: asset.tags,
         activeWorkflows: activeWorkflows,
         custom: jsonUtils.toJSONString(custom),
      });
   }
};
