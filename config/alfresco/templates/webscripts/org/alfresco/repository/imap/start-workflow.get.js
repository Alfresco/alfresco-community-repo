function main()
{
   logger.log("Start workflow form 'Start workflow' webscript");
   var docNode = search.findNode("workspace://SpacesStore/" + args.nodeRefId);
   if (docNode == undefined)
   {
      status.code = 404;
      status.message = "Content with NodeRef id '" + args.nodeRefId + "' not found.";
      status.redirect = true;
      return;
   }      
   
    // ALF-13898: accept FULL workflow-definition name if provided, otherwise revert to prefixing behavior
    var workflowType = args.workflowType;
    if(workflowType.indexOf('$') < 0) {
       workflowType = "jbpm$wf:" + workflowType;
    }
    
    var assignTo = people.getPerson(args.assignTo);
    if (assignTo == undefined)
    {
       status.code = 404;
       status.message = "Person with username '" + args.assignTo + "' not found.";
       status.redirect = true;
       return;
    }
    var day = args.workflowDueDateDay;
    var month = args.workflowDueDateMonth;
    var year = args.workflowDueDateYear;
    if (year != null && year.length == 2)
    {
       year = "20" + year;
    }
    var dueDate = new Date(year, month - 1, day);
    var description = args.description;

    var workflow = actions.create("start-workflow");
    workflow.parameters.workflowName = workflowType;
    workflow.parameters["bpm:workflowDescription"] = description;
    workflow.parameters["bpm:assignee"] = assignTo;
    
    if (args.workflowPriority != null)
    {
       workflow.parameters["bpm:workflowPriority"] = args.workflowPriority;
    }
    
    if (dueDate != null)
    {
       workflow.parameters["bpm:workflowDueDate"] = dueDate;
    } 
    workflow.execute(docNode);
}
main();