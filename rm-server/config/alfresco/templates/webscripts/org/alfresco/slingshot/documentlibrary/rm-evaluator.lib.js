var Evaluator =
{
   /**
    * Asset type evaluator
    */
   getAssetType: function Evaluator_getAssetType(asset)
   {
      var assetType = "";
      // More detailed asset type
      switch (String(asset.typeShort))
      {
         case "rma:filePlan":
            assetType = "fileplan";
            break;
         case "rma:recordCategory":
            assetType = "record-category";
            break;
         case "rma:recordFolder":
            assetType = "record-folder";
            if (asset.hasAspect("dod:ghosted"))
            {
               assetType = "metadata-stub-folder";
            }
            break;
         case "rma:nonElectronicDocument":
            // Fall-through
         case "cm:content":
            if (asset.hasAspect("rma:record"))
            {
               assetType = "undeclared-record";
               if (asset.hasAspect("rma:declaredRecord"))
               {
                  assetType = "record";
                  if (asset.hasAspect("dod:ghosted"))
                  {
                     assetType = "metadata-stub";
                  }
               }
            }
            break;
         case "rma:transfer":
            assetType = "transfer-container";
            break;
         case "rma:hold":
            assetType = "hold-container";
            break;
         default:
            assetType = asset.isContainer ? "folder" : "document";
            break;
      }

      return assetType;
   },

   /**
    * Records Management metadata extracter
    */
   getMetadata: function Evaluator_getMetadata(asset)
   {
      var metadata = {};

      var fnExtract = function(p_asset)
      {
         for (var index in p_asset.properties)
         {
            if (index.indexOf("{http://www.alfresco.org/model/recordsmanagement/1.0}") === 0)
            {
               metadata[index.replace("{http://www.alfresco.org/model/recordsmanagement/1.0}", "rma:")] = p_asset.properties[index];
            }
            else if (index.indexOf("{http://www.alfresco.org/model/dod5015/1.0}") === 0)
            {
               metadata[index.replace("{http://www.alfresco.org/model/dod5015/1.0}", "dod:")] = p_asset.properties[index];
            }
         }
      };

      // General Records Management properties
      fnExtract(asset);

      // Disposition Instructions, if relevant
      if (asset.hasAspect("rma:scheduled"))
      {
         var dsArray = asset.childAssocs["rma:dispositionSchedule"];
         if (dsArray != null)
         {
            var dsNode = dsArray[0];
            if (dsNode !== null)
            {
               fnExtract(dsNode);
            }
         }
      }

      return metadata;
   },

   /**
    * Previous disposition action
    */
   getPreviousDispositionAction: function Evaluator_getPreviousDispositionAction(asset)
   {
      var history = asset.childAssocs["rma:dispositionActionHistory"],
         previous = null,
         fnSortByCompletionDateReverse = function sortByCompletionDateReverse(a, b)
         {
            // Sort the results by Disposition Action Completed At date property
            return (b.properties["rma:dispositionActionCompletedAt"] > a.properties["rma:dispositionActionCompletedAt"] ? 1 : -1);
         };

      if (history != null)
      {
         history.sort(fnSortByCompletionDateReverse);
         previous = history[0];
      }

      return previous;
   },

   /**
    * Record and Record Folder common evaluators
    */
   recordAndRecordFolder: function Evaluator_recordAndRecordFolder(asset, permissions, status)
   {
      var actionName = asset.properties["rma:recordSearchDispositionActionName"],
         actionAsOf = asset.properties["rma:recordSearchDispositionActionAsOf"],
         hasNextAction = asset.childAssocs["rma:nextDispositionAction"] != null,
         recentHistory = Evaluator.getPreviousDispositionAction(asset),
         previousAction = null,
         now = new Date();

      /* Next Disposition Action */
      // Next action could become eligible based on asOf date
      if (actionAsOf != null)
      {
         if (hasNextAction)
         {
            permissions["disposition-as-of"] = true;
         }

         // Check if action asOf date has passed
         if (actionAsOf < now)
         {
            permissions[actionName] = true;
         }
      }
      // Next action could also become eligible based on event completion
      if (asset.properties["rma:recordSearchDispositionEventsEligible"] == true)
      {
         permissions[actionName] = true;
      }

      /* Previous Disposition Action */
      if (recentHistory != null)
      {
         previousAction = recentHistory.properties["rma:dispositionAction"];
      }

      /* Cut Off status */
      if (asset.hasAspect("rma:cutOff"))
      {
         status["cutoff"] = true;
         if (asset.hasAspect("rma:dispositionLifecycle"))
         {
            if (previousAction == "cutoff")
            {
               permissions["undo-cutoff"] = true;
               delete permissions["cutoff"];
            }
         }
      }

      /* Transfer or Accession Pending Completion */
      // Don't show transfer or accession if either is pending completion
      var assocs = asset.parentAssocs["rma:transferred"];
      if (actionName == "transfer" && assocs != null && assocs.length > 0)
      {
         delete permissions["transfer"];
         delete permissions["undo-cutoff"];
         delete permissions["disposition-as-of"];
         status["transfer " + assocs[0].name] = true;
      }
      assocs = asset.parentAssocs["rma:ascended"];
      if (actionName == "accession" && assocs != null && assocs.length > 0)
      {
         delete permissions["accession"];
         delete permissions["undo-cutoff"];
         delete permissions["disposition-as-of"];
         status["accession " + assocs[0].name] = true;
      }

      /* Transferred status */
      if (asset.hasAspect("rma:transferred"))
      {
         var transferLocation = "";
         if (previousAction == "transfer")
         {
            var actionId = recentHistory.properties["rma:dispositionActionId"],
               actionNode = search.findNode("workspace://SpacesStore/" + actionId);

            if (actionNode != null && actionNode.properties["rma:dispositionLocation"])
            {
               transferLocation = " " + actionNode.properties["rma:dispositionLocation"];
            }
         }
         status["transferred" + transferLocation] = true;
      }

      /* Accessioned status */
      if (asset.hasAspect("rma:ascended"))
      {
         status["accessioned NARA"] = true;
      }

      /* Review As Of Date */
      if (asset.hasAspect("rma:vitalRecord"))
      {
         if (asset.properties["rma:reviewAsOf"] != null)
         {
            permissions["review-as-of"] = true;
         }
      }

      /* Frozen/Unfrozen */
      if (asset.hasAspect("rma:frozen"))
      {
         status["frozen"] = true;
         if (permissions["Unfreeze"])
         {
            permissions["unfreeze"] = true;
         }
      }
      else
      {
         if (permissions["ExtendRetentionPeriodOrFreeze"])
         {
            permissions["freeze"] = true;
         }
      }
   },

   /**
    * Record Type evaluator
    */
   recordType: function Evaluator_recordType(asset)
   {
      /* Supported Record Types */
      var recordTypes =
      [
         "digitalPhotographRecord",
         "pdfRecord",
         "scannedRecord",
         "webRecord"
      ],
         currentRecordType = null;

      for (var i = 0; i < recordTypes.length; i++)
      {
         if (asset.hasAspect("dod:" + recordTypes[i]))
         {
            currentRecordType = recordTypes[i];
            break;
         }
      }

      return currentRecordType;
   },

   /**
    * Asset Evaluator - main entrypoint
    */
   run: function Evaluator_run(asset, capabilitySet)
   {
      var assetType = Evaluator.getAssetType(asset),
         rmNode,
         recordType = null,
         capabilities = {},
         actions = {},
         actionSet = "empty",
         permissions = {},
         status = {},
         suppressRoles = false;

      var now = new Date();

      try
      {
         rmNode = rmService.getRecordsManagementNode(asset)
      }
      catch (e)
      {
         // Not a Records Management Node
         return null;
      }

      /**
       * Capabilities and Actions
       */
      var caps, cap, act;
      if (capabilitySet == "all")
      {
         caps = rmNode.capabilities;
      }
      else
      {
         caps = rmNode.capabilitiesSet(capabilitySet);
      }

      for each (cap in caps)
      {
         capabilities[cap.name] = true;
         for each (act in cap.actions)
         {
            actions[act] = true;
         }
      }

      /**
       * COMMON FOR ALL TYPES
       */

      /**
       * Basic permissions - start from entire capabiltiies list
       * TODO: Filter-out the ones not relevant to DocLib UI.
       */
      permissions = capabilities;

      /**
       * Multiple parent assocs
       */
      var parents = asset.parentAssocs["contains"];
      if (parents !== null && parents.length > 1)
      {
         status["multi-parent " + parents.length] = true;
      }

      /**
       * E-mail type
       */
      if (asset.mimetype == "message/rfc822")
      {
         permissions["split-email"] = true;
      }

      switch (assetType)
      {
         /**
          * SPECIFIC TO: FILE PLAN
          */
         case "fileplan":
            permissions["new-series"] = capabilities["Create"];
            break;


         /**
          * SPECIFIC TO: RECORD SERIES
          */
         case "record-series":
            actionSet = "recordSeries";
            permissions["new-category"] = capabilities["Create"];
            break;


         /**
          * SPECIFIC TO: RECORD CATEGORY
          */
         case "record-category":
            actionSet = "recordCategory";
            permissions["new-folder"] = capabilities["Create"];
            break;


         /**
          * SPECIFIC TO: RECORD FOLDER
          */
         case "record-folder":
            actionSet = "recordFolder";

            /* Record and Record Folder common evaluator */
            Evaluator.recordAndRecordFolder(asset, permissions, status);

            /* Update Cut Off status to folder-specific status */
            if (status["cutoff"] == true)
            {
               delete status["cutoff"];
               status["cutoff-folder"] = true;
            }

            /* File new Records */
            permissions["file"] = capabilities["Create"];

            /* Open/Closed */
            if (asset.properties["rma:isClosed"])
            {
               // Cutoff implies closed, so no need to duplicate
               if (!status["cutoff-folder"])
               {
                  status["closed"] = true;
               }
               if (capabilities["ReOpenFolders"])
               {
                  permissions["open-folder"] = true;
               }
            }
            else
            {
               status["open"] = true;
               if (capabilities["CloseFolders"])
               {
                  permissions["close-folder"] = true;
               }
            }
            break;


         /**
          * SPECIFIC TO: RECORD
          */
         case "record":
            actionSet = "record";

            /* Record and Record Folder common evaluator */
            Evaluator.recordAndRecordFolder(asset, permissions, status);

            /* Electronic/Non-electronic documents */
            if (asset.typeShort == "rma:nonElectronicDocument")
            {
               assetType = "record-nonelec";
            }
            else
            {
               permissions["download"] = true;
            }

            /* Record Type evaluator */
            recordType = Evaluator.recordType(asset);
            if (recordType != null)
            {
               status[recordType] = true;
            }

            /* Undeclare Record */
            if (asset.hasAspect("rma:cutOff") == false)
            {
               permissions["undeclare"] = true;
            }
            break;


         /**
          * SPECIFIC TO: GHOSTED RECORD FOLDER (Metadata Stub Folder)
          */
         case "metadata-stub-folder":
            actionSet = "metadataStubFolder";

            /* Destroyed status */
            status["destroyed"] = true;
            break;


         /**
          * SPECIFIC TO: GHOSTED RECORD (Metadata Stub)
          */
         case "metadata-stub":
            actionSet = "metadataStub";

            /* Destroyed status */
            status["destroyed"] = true;

            /* Record Type evaluator */
            recordType = Evaluator.recordType(asset);
            if (recordType != null)
            {
               status[recordType] = true;
            }
            break;


         /**
          * SPECIFIC TO: UNDECLARED RECORD
          */
         case "undeclared-record":
            actionSet = "undeclaredRecord";

            /* Electronic/Non-electronic documents */
            if (asset.typeShort == "rma:nonElectronicDocument")
            {
               assetType = "undeclared-record-nonelec";
            }
            else
            {
               permissions["download"] = true;

               /* Record Type evaluator */
               recordType = Evaluator.recordType(asset);
               if (recordType != null)
               {
                  status[recordType] = true;
               }
               else
               {
                  permissions["set-record-type"] = true;
               }
            }
            break;


         /**
          * SPECIFIC TO: TRANSFER CONTAINERS
          */
         case "transfer-container":
            actionSet = "transferContainer";
            suppressRoles = true;
            break;


         /**
          * SPECIFIC TO: HOLD CONTAINERS
          */
         case "hold-container":
            actionSet = "holdContainer";
            permissions["Unfreeze"] = true;
            permissions["ViewUpdateReasonsForFreeze"] = true;
            suppressRoles = true;
            break;


         /**
          * SPECIFIC TO: LEGACY TYPES
          */
         default:
            actionSet = assetType;
            break;
      }

      return (
      {
         assetType: assetType,
         actionSet: actionSet,
         permissions: permissions,
         createdBy: Common.getPerson(asset.properties["cm:creator"]),
         modifiedBy: Common.getPerson(asset.properties["cm:modifier"]),
         status: status,
         metadata: Evaluator.getMetadata(asset, assetType),
         suppressRoles: suppressRoles
      });
   }
};
