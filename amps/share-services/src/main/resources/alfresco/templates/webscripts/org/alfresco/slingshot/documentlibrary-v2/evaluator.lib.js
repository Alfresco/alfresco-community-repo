
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
                  linkedNode = { isTargetDeleted: true };
               }
               break;

            /**
             * SPECIFIC TO: DOCUMENTS
             */
            case "document":
               // Working Copy?
               if (node.hasAspect("{http://www.alfresco.org/model/content/1.0}workingcopy"))
               {
                  var wcLink = node.sourceAssocs["cm:workingcopylink"];
                  var isWorkingCopy = wcLink != null;
                  if (isWorkingCopy)
                  {
                     var wcNode = wcLink[0];
                     workingCopy["isWorkingCopy"] = true;
                     workingCopy["sourceNodeRef"] = wcNode.nodeRef;
                     if (wcNode.hasAspect("{http://www.alfresco.org/model/content/1.0}versionable"))
                     {
                        workingCopy["workingCopyVersion"] = wcNode.properties["cm:versionLabel"];
                     }
                  }
                  else
                  {
                     logger.error("Node: " + node.nodeRef +" hasn't \"cm:workingcopylink\" association");
                  }
               }
               // Locked?
               else if (node.isLocked && !node.hasAspect("trx:transferred") && node.hasAspect("{http://www.alfresco.org/model/content/1.0}checkedOut"))
               {
                  var srcNode = node.assocs["cm:workingcopylink"][0];
                  workingCopy["hasWorkingCopy"] = true;
                  workingCopy["workingCopyNodeRef"] = srcNode.nodeRef;
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

      var nodeJSON = appUtils.toJSON(node, true);
      var stripLinkedNodeProperties = (args["stripLinkedNodeProperties"] == "true" ? true : false);

      if (isLink && stripLinkedNodeProperties) {
         var nodeJsonObj = jsonUtils.toObject(nodeJSON);
         var linkedPropsInclude = ["cm:taggable","cm:categories"];
         var linkedNodePropsCompact = {};

         for each (var linkedProp in linkedPropsInclude) {
            var prop = nodeJsonObj.linkedNode.properties[linkedProp];
            if (prop) {
               linkedNodePropsCompact[linkedProp] = prop;
            }
         }

         nodeJsonObj.linkedNode.properties = linkedNodePropsCompact;
         nodeJSON = jsonUtils.toJSONString(nodeJsonObj);
      }

      if (node !== null)
      {
         return(
         {
            node: node,
            nodeJSON: nodeJSON,
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
