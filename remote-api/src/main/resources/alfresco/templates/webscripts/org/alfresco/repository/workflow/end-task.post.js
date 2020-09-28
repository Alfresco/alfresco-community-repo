function main()
{
   // Task ID
   var taskId = url.templateArgs.taskId;
   if (taskId === undefined || taskId == null )
   {
		status.setCode(status.STATUS_BAD_REQUEST, "TaskID missing when ending task.");
		return;
   }

   var separatorIndex = taskId.indexOf('$');
   if(separatorIndex == -1)
   {
       status.setCode(status.STATUS_BAD_REQUEST, "TaskID missing when ending task.");
       return;
   }
   
   // Check TaskId is valid
   var task = workflow.getTask(taskId);
   if (task === null)
   {
	   status.setCode(status.STATUS_BAD_REQUEST, "Invalid TaskID when ending task.");
	   return;
   }
   
   model.taskId = taskId;
   
   // Optional Transition ID
   var transitionId = url.templateArgs.transitionId;
   if (transitionId === undefined)
   {
      transitionId = null;
   }
   
   model.transitionId = transitionId;
   
   task.endTask(transitionId);
}

main();