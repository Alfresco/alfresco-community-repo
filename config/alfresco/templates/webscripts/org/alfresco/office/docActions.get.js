// Client has requested server-side action

/* Inputs */
var runAction = args.a;

/* Outputs */
var resultString = "failed",
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
            resultString = "convert.failed";
            var nodeTrans = docNode.transformDocument("application/pdf");
            if (nodeTrans != null)
            {
               resultString = "converted";
               resultCode = true;
            }
         }
         else if (runAction == "delete")
         {
            resultString = "delete.failed";
            if (docNode.remove())
            {
               resultString = "deleted";
               resultCode = true;
            }
         }
         else if (runAction == "checkout")
         {
            resultString = "checkout.failed"
            var workingCopy = docNode.checkout();
            if (workingCopy != null)
            {
               resultString = "checked_out";
               resultCode = true;
            }
         }
         else if (runAction == "checkin")
         {
            resultString = "checkin.failed"
            var originalDoc = docNode.checkin();
            if (originalDoc != null)
            {
               resultString = "checked_in";
               resultCode = true;
            }
         }
         else if (runAction == "makeversion")
         {
            resultString = "version.failed";
            if (docNode.addAspect("cm:versionable"))
            {
               resultString = "versioned";
               resultCode = true;
            }
         }
         else if (runAction == "workflow")
         {
            var workflowType = "jbpm$wf:" + args.wt,
               assignTo = people.getPerson(args.at),
               dueDate = new Date(args.dd),
               description = args.desc;
            
            if (assignTo == null)
            {
               resultString = "user_not_found";
            }
            else
            {
               var workflow = actions.create("start-workflow");
               workflow.parameters.workflowName = workflowType;
               workflow.parameters["bpm:workflowDescription"] = description;
               workflow.parameters["bpm:assignee"] = assignTo;
               if ((args.dd) && (args.dd != ""))
               {
                  workflow.parameters["bpm:workflowDueDate"] = dueDate;
               } 
               workflow.execute(docNode);
               resultString = "workflow_started";
               resultCode = true;
            }
         }
         else if (runAction == "test")
         {
            resultString = "Test complete.";
            resultCode = true;
         }
         else
         {
             resultString = "unknown";
         }
      }
      catch(e)
      {
         resultString = "exception";
      }
   }
}
else  // Non document-based actions
{
   try
   {
      if (runAction == "newspace")
      {
         resultString = "create_space.failed";
         var parentNodeId = args.p,
            spaceName = args.sn,
            spaceTitle = (args.st == "undefined") ? "" : args.st,
            spaceDescription = (args.sd == "undefined") ? "" : args.sd.substr(0, 100)
            templateId = args.t;
         var nodeNew;
         
         if ((spaceName == null) || (spaceName == ""))
         {
            resultString = "create_space.missing_name";
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
               resultString = "space_created";
               resultCode = true;
            }
         }
      }
      else
      {
          resultString = "unknown";
      }
   }
   catch(e)
   {
      resultString = "exception";
   }
}
model.resultString = "office.result." + resultString;
model.resultCode = resultCode;