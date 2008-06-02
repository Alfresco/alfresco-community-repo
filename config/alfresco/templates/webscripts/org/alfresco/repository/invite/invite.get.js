/**
 * Starts the Invite workflow
 *
 * @method start
 * @static
 */
function start()
{
   var wfDefinition = workflow.getDefinitionByName("wf:invite");
   /* TODO glen.johnson@alfresco.com - pass inviter and invitee users in
    * as properties when starting workflow
    */
   wfDefinition.startWorkflow(null, null);
}

/**
 * Cancels pending invite. Note that only the Inviter is allowed to
 * cancel the pending invite.
 *
 * @method cancel
 * @static
 */
function cancel()
{
   // try and get hold of workflow id URL parameter
   if (args.wfid == undefined || args.wfid.length == 0) 
   {
      status.code = 400;
      status.message = "workflow ID parameter has not been provided in URL.";
      status.redirect = true;
   }
   else 
   {
      var wfid = args.wfid;
      var workflow = workflow.getInstance(wfid);  
      workflow.cancel();
   }
}

/**
 * Processes Inviter actions
 *
 * @method main
 * @static
 */
function main()
{
   // check that action has been provided
   if ((url.extension === null || args.response.length == 0)) 
   {
      // handle action not provided
      status.code = 400;
      status.message = "Action has not been provided in URL.";
      status.redirect = true;
   }
   else 
   {
      // process action
      var action = url.extension;
      if (action == "start") 
      {
         start();
      }
      else if (action == "cancel") 
      {
         cancel();
      }
      else 
      {
         // handle action not recognised
         status.code = 400;
         status.message = "Action, " + action + ", provided in URL has not been recognised.";
         status.redirect = true;
      }
   }
}

main();
