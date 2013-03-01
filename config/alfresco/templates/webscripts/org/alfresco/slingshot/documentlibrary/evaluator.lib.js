
var Evaluator =
{
   /**
    * Node Type evaluator
    */
   getNodeType: function Evaluator_getNodeType(node)
   {
      var nodeType = "";
      if (node.isContainer)
      {
         nodeType = "folder";
      }
      else if (node.typeShort == "app:folderlink")
      {
         nodeType = "folderlink";
      }
      else if (node.typeShort == "app:filelink")
      {
         nodeType = "filelink";
      }
      else
      {
         nodeType = "document";
      }
      return nodeType;
   },
   
   /**
    * Parent container evaluators
    */
   parentContainer: function Evaluator_parentContainer(node, permissions)
   {
   },
   
   /**
    * Document and Folder common evaluators
    */
   documentAndFolder: function Evaluator_documentAndFolder(node, permissions, status, actionLabels)
   {
      /* Simple Workflow */
      if (node.hasAspect("app:simpleworkflow"))
      {
         status["simple-workflow"] = true;
         if (node.properties["app:approveStep"] != null)
         {
            permissions["simple-approve"] = true;
            actionLabels["onActionSimpleApprove"] = node.properties["app:approveStep"];
         }
         if (node.properties["app:rejectStep"] != null)
         {
            permissions["simple-reject"] = true;
            actionLabels["onActionSimpleReject"] = node.properties["app:rejectStep"];
         }
      }
   },
   
   /**
    * Node Evaluator - main entrypoint
    */
   run: function Evaluator_run(node, isParent)
   {
      var nodeType = Evaluator.getNodeType(node),
         actions = {},
         actionSet = "empty",
         permissions = {},
         status = {},
         custom = {},
         actionLabels = {},
         activeWorkflows = [],
         createdBy = Common.getPerson(node.properties["cm:creator"]),
         modifiedBy = Common.getPerson(node.properties["cm:modifier"]),
         isLink = false,
         linkedNode = null,
         lockedBy = null,
         lockOwnerUser = "";

      /**
       * COMMON TO ALL
       */
      permissions =
      {
         "create": node.hasPermission("CreateChildren"),
         "edit": node.hasPermission("Write"),
         "delete": node.hasPermission("Delete"),
         "permissions": node.hasPermission("ChangePermissions"),
         "cancel-checkout": node.hasPermission("CancelCheckOut")
      };
      
      // When evaluating parent container
      if (isParent)
      {
         Evaluator.parentContainer(node, permissions);
      }

      // Get relevant actions set
      switch (nodeType)
      {
         /**
          * SPECIFIC TO: LINK
          */
         case "folderlink":
         case "filelink":
            actionSet = "link";
            isLink = true;
            
            /**
             * NOTE: After this point, the "node" object will be changed to a link's destination node
             *       if the original node was a filelink type.
             */
            linkedNode = node.properties.destination;
            if (linkedNode == null)
            {
               return null;
            }
            // Re-evaluate the nodeType based on the link's destination node
            nodeType = Evaluator.getNodeType(linkedNode);
            break;
         
         /**
          * SPECIFIC TO: FOLDER
          */
         case "folder":
            actionSet = "folder";

            /* Document Folder common evaluator */
            Evaluator.documentAndFolder(node, permissions, status, actionLabels);
            
            /* Rules applied? */
            if (node.hasAspect("rule:rules"))
            {
               status["rules"] = true;
            }

            /* Transferred Nodes */
            if (node.hasAspect("trx:transferred"))
            {
               status["transferred-node"] = true;
               permissions["view-source-repository"] = true;
               actionSet = "transferredFolder";
            }
            break;

         /**
          * SPECIFIC TO: DOCUMENTS
          */
         case "document":
            actionSet = "document";

            /* Document Folder common evaluator */
            Evaluator.documentAndFolder(node, permissions, status, actionLabels);
            
            // Working Copy?
            if (node.hasAspect("{http://www.alfresco.org/model/content/1.0}workingcopy"))
            {
               var wcStatus = "";
               lockedBy = Common.getPerson(node.properties["cm:workingCopyOwner"]);
               lockOwnerUser = lockedBy.userName;
               if (lockOwnerUser == person.properties.userName)
               {
                  wcStatus = "editing";
                  actionSet = "workingCopyOwner";
               }
               else
               {
                  wcStatus = "locked " + lockedBy.displayName + "|" + lockedBy.userName;
                  actionSet = "locked";
               }
               var wcNode = node.assocs["cm:original"][0];
               custom["isWorkingCopy"] = true;
               custom["workingCopyOriginal"] = wcNode.nodeRef;
               if (wcNode.hasAspect("{http://www.alfresco.org/model/content/1.0}versionable"))
               {
                  custom["workingCopyVersion"] = wcNode.properties["cm:versionLabel"];
               }
               permissions["view-original"] = true;

               // Google Doc?
               if (node.hasAspect("{http://www.alfresco.org/model/googledocs/1.0}googleResource"))
               {
                  custom["googleDocUrl"] = node.properties["gd:url"];
                  permissions["view-google-doc"] = true;
                  if (lockOwnerUser == person.properties.userName)
                  {
                     permissions["checkin-from-google"] = true;
                     wcStatus = "google-docs-owner";
                     actionSet = "googleDocOwner";
                  }
                  else
                  {
                     wcStatus = "google-docs-locked " + lockedBy.displayName + "|" + lockedBy.userName;
                     actionSet = "googleDocLocked";
                  }
               }
               status[wcStatus] = true;
            }
            // Locked?
            else if (node.isLocked && !node.hasAspect("trx:transferred"))
            {
               var lockStatus = "";
               lockedBy = Common.getPerson(node.properties["{http://www.alfresco.org/model/content/1.0}lockOwner"]);
               lockOwnerUser = lockedBy.userName;
               if (lockOwnerUser == person.properties.userName)
               {
                  lockStatus = "lock-owner";
                  actionSet = "lockOwner";
               }
               else
               {
                  lockStatus = "locked " + lockedBy.displayName + "|" + lockedBy.userName;
                  actionSet = "locked";
               }
               if (node.hasAspect("{http://www.alfresco.org/model/content/1.0}checkedOut"))
               {
                  var srcNode = node.assocs["cm:workingcopylink"][0];
                  custom["hasWorkingCopy"] = true;
                  custom["workingCopyNode"] = srcNode.nodeRef;
                  permissions["view-working-copy"] = true;

                  // Google Doc?
                  if (srcNode.hasAspect("{http://www.alfresco.org/model/googledocs/1.0}googleResource"))
                  {
                     custom["googleDocUrl"] = srcNode.properties["gd:url"];
                     permissions["view-google-doc"] = true;
                     if (lockOwnerUser == person.properties.userName)
                     {
                        permissions["checkin-from-google"] = true;
                        lockStatus = "google-docs-owner";
                        actionSet = "googleDocOwner";
                     }
                     else
                     {
                        lockStatus = "google-docs-locked " + lockedBy.displayName + "|" + lockedBy.userName;
                        actionSet = "googleDocLocked";
                     }
                  }
               }
               status[lockStatus] = true;
            }
            
            // Inline editable aspect?
            if (node.hasAspect("app:inlineeditable"))
            {
               permissions["inline-edit"] = true;
            }
            
            // Google Docs editable aspect?
            if (node.hasAspect("{http://www.alfresco.org/model/googledocs/1.0}googleEditable"))
            {
               permissions["googledocs-edit"] = true;
            }

            /* Transferred Nodes */
            if (node.hasAspect("trx:transferred"))
            {
               status["transferred-node"] = true;
               permissions["view-source-repository"] = true;
               actionSet = "transferredDocument";
            }
            break;
      }
      
      if (node !== null)
      {
         // Part of an active workflow? Guard against stale worklow tasks.
         try
         {
            for each (activeWorkflow in node.activeWorkflows)
            {
               activeWorkflows.push(activeWorkflow.id);
            }
         }
         catch (e) {}

         return(
         {
            node: node,
            type: nodeType,
            isLink: isLink,
            linkedNode: linkedNode,
            status: status,
            actionSet: actionSet,
            actionPermissions: permissions,
            createdBy: createdBy,
            modifiedBy: modifiedBy,
            lockedBy: lockedBy,
            tags: node.tags,
            activeWorkflows: activeWorkflows,
            custom: jsonUtils.toJSONString(custom),
            customObj: custom,
            actionLabels: actionLabels
         });
      }
      else
      {
         return null;
      }
   }
};
