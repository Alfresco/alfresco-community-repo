// Client has requested server-side action

/* Inputs */
var runAction = args.a;

/* Outputs */
var resultString = "Action failed",
   resultCode = false;

// Is this action targetting a document?
var docNodeId = args.n;
if ((docNodeId != "") && (docNodeId != null))
{
   var docNode = search.findNode("workspace://SpacesStore/" + docNodeId);

   if (docNode != null && docNode.isDocument)
   {
      try
      {
         if (runAction == "makepdf")
         {
            resultString = "Could not convert document";
            var nodeTrans = docNode.transformDocument("application/pdf");
            if (nodeTrans != null)
            {
               resultString = "Document converted";
               resultCode = true;
            }
         }
         else if (runAction == "delete")
         {
            resultString = "Could not delete document";
            if (docNode.remove())
            {
               resultString = "Document deleted";
               resultCode = true;
            }
         }
         else if (runAction == "checkout")
         {
            var workingCopy = docNode.checkout();
            if (workingCopy != null)
            {
               resultString = "Document checked out";
               resultCode = true;
            }
         }
         else if (runAction == "checkin")
         {
            var originalDoc = docNode.checkin();
            if (originalDoc != null)
            {
               resultString = "Document checked in";
               resultCode = true;
            }
         }
         else if (runAction == "makeversion")
         {
            resultString = "Could not version document";
            if (docNode.addAspect("cm:versionable"))
            {
               resultString = "Document versioned";
               resultCode = true;
            }
         }
         else if (runAction == "workflow")
         {
            var workflowType = "jbpm$wf:" + args.wt;
            var assignTo = people.getPerson(args.at);
            var dueDate = new Date(args.dd);
            var description = args.desc;
   
            var workflow = actions.create("start-workflow");
            workflow.parameters.workflowName = workflowType;
            workflow.parameters["bpm:workflowDescription"] = description;
            workflow.parameters["bpm:assignee"] = assignTo;
            if ((args.dd) && (args.dd != ""))
            {
               workflow.parameters["bpm:workflowDueDate"] = dueDate;
            } 
            workflow.execute(docNode);
            resultString = "New workflow started";
            resultCode = true;
         }
         else if (runAction == "test")
         {
            resultString = "Tested ok.";
            resultCode = true;
         }
         else
         {
             resultString = "Unknown action";
         }
      }
      catch(e)
      {
         resultString = "Action failed due to exception";
      }
   }
}
else  // Non document-based actions
{
   try
   {
      if (runAction == "newspace")
      {
         resultString = "Could not create space";
         var parentNodeId = args.p,
            spaceName = args.sn,
            spaceTitle = (args.st == "undefined") ? "" : args.st,
            spaceDescription = (args.sd == "undefined") ? "" : args.sd,
            templateId = args.t;
         var nodeNew;
         
         if ((spaceName == null) || (spaceName == ""))
         {
            resultString = "Space must have a Name";
         }
         else
         {
            var nodeParent = search.findNode("workspace://SpacesStore/" + parentNodeId);
            // Copy from template?
            if ((templateId != null) && (templateId != ""))
            {
               nodeTemplate = search.findNode("workspace://SpacesStore/" + templateId);
               nodeNew = nodeTemplate.copy(nodeParent, true);
               nodeNew.name = spaceName;
            }
            else
            {
               nodeNew = nodeParent.createFolder(spaceName);
            }
            // Always add title & description, default icon
            nodeNew.properties["cm:title"] = spaceTitle;
            nodeNew.properties["cm:description"] = spaceDescription;
            nodeNew.properties["app:icon"] = "space-icon-default";
            nodeNew.save();
            // Add uifacets aspect for the web client
            nodeNew.addAspect("app:uifacets");
            if (nodeNew != null)
            {
               resultString = "New space created";
               resultCode = true;
            }
         }
      }
      else
      {
          resultString = "Unknown action";
      }
   }
   catch(e)
   {
      resultString = "Action failed due to exception";
   }
}
model.resultString = resultString;
model.resultCode = resultCode;