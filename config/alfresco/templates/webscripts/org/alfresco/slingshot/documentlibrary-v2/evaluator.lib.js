
var Evaluator =
{
   /**
    * Node Type evaluator
    */
   getNodeType: function Evaluator_getNodeType(node)
   {
      var nodeType = "document";
      if (node.isContainer)
      {
         nodeType = "folder";
      }
      else if (node.isLinkToContainer)
      {
         nodeType = "folderlink";
      }
      else if (node.isLinkToDocument)
      {
         nodeType = "filelink";
      }
      return nodeType;
   },
   
   /**
    * Node Evaluator - main entrypoint
    */
   run: function Evaluator_run(node, isParent)
   {
      var nodeType = Evaluator.getNodeType(node),
         workingCopy = {},
         activeWorkflows = [],
         isLink = false,
         linkedNode = null;

      if (!isParent)
      {
         // Get relevant actions set
         switch (nodeType)
         {
            /**
             * SPECIFIC TO: LINK
             */
            case "folderlink":
            case "filelink":
               isLink = true;

               linkedNode = node.properties.destination;
               if (linkedNode == null)
               {
                  return null;
               }
               break;

            /**
             * SPECIFIC TO: DOCUMENTS
             */
            case "document":
               // Working Copy?
               if (node.hasAspect("cm:workingcopy"))
               {
                  var wcNode = node.assocs["cm:original"][0];
                  workingCopy["isWorkingCopy"] = true;
                  workingCopy["sourceNodeRef"] = wcNode.nodeRef;
                  if (wcNode.hasAspect("cm:versionable") && wcNode.versionHistory !== null && wcNode.versionHistory.length > 0)
                  {
                     workingCopy["workingCopyVersion"] = wcNode.versionHistory[0].label;
                  }

                  // Google Doc?
                  if (node.hasAspect("{http://www.alfresco.org/model/googledocs/1.0}googleResource"))
                  {
                     // Property is duplicated here for convenience
                     workingCopy["googleDocUrl"] = node.properties["gd:url"];
                  }
               }
               // Locked?
               else if (node.isLocked && !node.hasAspect("trx:transferred") && node.hasAspect("cm:checkedOut"))
               {
                  var srcNode = node.assocs["cm:workingcopylink"][0];
                  workingCopy["hasWorkingCopy"] = true;
                  workingCopy["workingCopyNodeRef"] = srcNode.nodeRef;

                  // Google Doc?
                  if (srcNode.hasAspect("{http://www.alfresco.org/model/googledocs/1.0}googleResource"))
                  {
                     workingCopy["googleDocUrl"] = srcNode.properties["gd:url"];
                  }
               }
         }

         // Part of an active workflow? Guard against stale worklow tasks.
         try
         {
            for each (activeWorkflow in node.activeWorkflows)
            {
               activeWorkflows.push(activeWorkflow.id);
            }
         }
         catch (e) {}
      }
      
      if (node !== null)
      {
         return(
         {
            node: node,
            nodeJSON: appUtils.toJSON(node, true),
            type: nodeType,
            isLink: isLink,
            linkedNode: linkedNode,
            activeWorkflows: activeWorkflows,
            workingCopy: workingCopy,
            workingCopyJSON: jsonUtils.toJSONString(workingCopy)
         });
      }
      else
      {
         return null;
      }
   }
};
