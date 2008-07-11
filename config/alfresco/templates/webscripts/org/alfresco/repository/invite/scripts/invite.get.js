var WORKFLOW_DEFINITION_NAME = "jbpm$wf:invite";
var ACTION_START = "start";
var ACTION_CANCEL = "cancel";
var TRANSITION_SEND_INVITE = "sendInvite";

/**
 * Starts the Invite workflow
 *
 * @method start
 * @static
 * @param inviteeEmail string email address of invitee
 * @param siteShortName string short name of site that the invitee is being
 *          invited to by the inviter
 *
 */
function start(inviteeEmail, siteShortName)
{
   var wfDefinition = workflow.getDefinitionByName(WORKFLOW_DEFINITION_NAME);
   
   // handle workflow definition does not exist
   if (wfDefinition === null)
   {
      status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, "Workflow definition "
      + "for name " + WORKFLOW_DEFINITION_NAME + " does not exist");
      return; 
   } 
   
   // create invitee person with generated user name and password, and with a
   // disabled user account
   var invitee = people.createPerson(true, false);
   invitee.properties["cm:email"] = inviteeEmail;
   invitee.save();
   
   var inviteeUserName = invitee.properties.userName;
   
   // create workflow properties
   var workflowProps = [];
   workflowProps["wf:inviterUserName"] = person.properties.userName;
   workflowProps["wf:inviteeUserName"] = inviteeUserName;
   workflowProps["wf:inviteeGenPassword"] = invitee.properties.generatedPassword;
   workflowProps["wf:siteShortName"] = siteShortName;
   
   // start the workflow
   var wfPath = wfDefinition.startWorkflow(workflowProps);
   var workflowId = wfPath.instance.id;
   
   // send out the invite
   wfPath.signal(TRANSITION_SEND_INVITE);
   
   // add action info to model for template processing
   model.action = ACTION_START;
   model.workflowId = workflowId;
   model.inviteeUserName = inviteeUserName;
   model.siteShortName = siteShortName;
}

/**
 * Cancels pending invite. Note that only the Inviter should
 * cancel the pending invite.
 *
 * @method cancel
 * @static
 * @param workflowId string workflow id of the invite process that inviter
 *          wishes to cancel
 */
function cancel(workflowId)
{
   var workflowInstance = workflow.getInstance(workflowId);
   
   // handle workflow instance for given workflow ID does not exist
   if (workflowInstance === null)
   {
      status.setCode(status.STATUS_BAD_REQUEST, "Workflow instance for given "
      + "workflow ID " + workflowId + " does not exist");
      return;
   }
   
   // cancel the workflow
   workflowInstance.cancel();
   
   // add action info to model for template
   model.action = ACTION_CANCEL;
   model.workflowId = workflowId;
}

/**
 * Carries out inviter's actions on invite process (that he/she started)
 *
 * @method main
 * @static
 */
function main()
{
   // extract action string from URL
   var action = null;
   var actionStartIndex = url.service.lastIndexOf("/") + 1;
   if (actionStartIndex <= url.service.length() - 1) 
   {
      action = url.service.substring(actionStartIndex, url.service.length());
   }
   
   // check that the action has been provided on the URL
   // and that URL parameters have been provided
   if ((action === null) || (action.length == 0)) 
   {
      // handle action not provided on URL
      status.setCode(status.STATUS_BAD_REQUEST, "Action has not been provided in URL");
      return;
   }
   
   // handle no parameters given on URL
   if (args.length == 0) 
   {
      status.setCode(status.STATUS_BAD_REQUEST, "No parameters have been provided on URL");
      return;
   }
   
   // handle action 'start'
   if (action == ACTION_START) 
   {
      // check for 'inviteeEmail' parameter not provided
      if ((args["inviteeEmail"] === null) || (args["inviteeEmail"].length == 0)) 
      {
         // handle inviteeEmail URL parameter not provided
         status.setCode(status.STATUS_BAD_REQUEST, "'inviteeEmail' parameter " +
         "has not been provided in URL for action '" + ACTION_START + "'");
         return;
      }
      
      // check for 'siteShortName' parameter not provided
      if ((args["siteShortName"] === null) || (args["siteShortName"].length == 0)) 
      {
         // handle siteShortName URL parameter not provided
         status.setCode(status.STATUS_BAD_REQUEST, "'siteShortName' parameter " +
         "has not been provided in URL for action '" + ACTION_START + "'");
         return;
      }
      
      // process action 'start' with provided parameters
      var inviteeEmail = args["inviteeEmail"];
      var siteShortName = args["siteShortName"];
      start(inviteeEmail, siteShortName);
   }
   // else handle if provided 'action' is 'cancel' 
   else if (action == ACTION_CANCEL) 
   {
      // check for 'workflowId' parameter not provided
      if ((args["workflowId"] === null) || (args["workflowId"].length == 0)) 
      {
         // handle workflowId URL parameter not provided
         status.setCode(status.STATUS_BAD_REQUEST, "'workflowId' parameter has "
         + "not been provided in URL for action '" + ACTION_CANCEL +"'");
         return;
      }
      
      // process action 'cancel' with provided parameters
      var workflowId = args["workflowId"];
      cancel(workflowId);
   }
   // handle action not recognised
   else 
   {
      status.setCode(status.STATUS_BAD_REQUEST, "Action, '" + action + "', "
      + "provided in URL has not been recognised.");
   }
}

main();
