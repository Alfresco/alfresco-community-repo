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
   run: function Evaluator_run(node)
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
         linkNode = null,
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
            linkNode = node;
            node = linkNode.properties.destination;
            // Re-evaluate the nodeType based on the link's destination node
            nodeType = Evaluator.getNodeType(node);
            break;
         
         /**
          * SPECIFIC TO: FOLDER
          */
         case "folder":
            actionSet = "folder";

            /* Document Folder common evaluator */
            Evaluator.documentAndFolder(node, permissions, status, actionLabels);
            break;

         /**
          * SPECIFIC TO: DOCUMENTS
          */
         case "document":
            actionSet = "document";

            /* Document Folder common evaluator */
            Evaluator.documentAndFolder(node, permissions, status, actionLabels);
            
            // Working Copy?
            if (node.hasAspect("cm:workingcopy"))
            {
               lockedBy = Common.getPerson(node.properties["cm:workingCopyOwner"]);
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
               var wcNode = node.properties["source"];
               custom["isWorkingCopy"] = true;
               custom["workingCopyOriginal"] = wcNode.nodeRef;
               if (wcNode.hasAspect("cm:versionable") && wcNode.versionHistory.length > 0)
               {
                  custom["workingCopyVersion"] = wcNode.versionHistory[0].label;
               }
               permissions["view-original"] = true;
            }
            // Locked?
            else if (node.isLocked)
            {
               lockedBy = Common.getPerson(node.properties["cm:lockOwner"]);
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
                  query: "+@cm\\:source:\"" + node.nodeRef + "\" +ISNOTNULL:cm\\:workingCopyOwner",
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
            
            // Inline editable aspect?
            if (node.hasAspect("app:inlineeditable"))
            {
               permissions["inline-edit"] = true;
            }
            break;
      }
      
      // Part of an active workflow?
      for each (activeWorkflow in node.activeWorkflows)
      {
         activeWorkflows.push(activeWorkflow.id);
      }

      return(
      {
         node: node,
         type: nodeType,
         linkNode: linkNode,
         isLink: isLink,
         status: status,
         actionSet: actionSet,
         actionPermissions: permissions,
         createdBy: createdBy,
         modifiedBy: modifiedBy,
         lockedBy: lockedBy,
         tags: node.tags,
         activeWorkflows: activeWorkflows,
         custom: jsonUtils.toJSONString(custom),
         actionLabels: actionLabels
      });
   }
};
