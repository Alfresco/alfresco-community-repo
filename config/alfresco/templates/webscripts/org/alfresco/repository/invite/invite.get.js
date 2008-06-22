/**
 * Starts the Invite workflow
 *
 * @method start
 * @static
 * @param siteShortName string short name of site that the invitee is being
 *          invited to by the inviter
 *
 */
function start(siteShortName)
{
   var wfDefinition = workflow.getDefinitionByName("wf:invite");
   
   // create invitee with generated user name and password, and with a
   // disabled user account
   var invitee = people.createPerson(true, false);
   
   // create workflow properties, containing inviter and invitee user name, 
   // and site short name
   var workflowProps = [];
   workflowProps["inviterUserName"] = person.properties.userName;
   workflowProps["inviteeUserName"] = invitee.properties.userName;
   workflowProps["siteShortName"] = siteShortName;
   wfDefinition.startWorkflow(null, workflowProps);
   
   // add action context info to model
   model.action = "start";
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
   workflowInstance.cancel();
   
   // add action context info to model
   model.action = "cancel";
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
   // check that the action ('start' or 'cancel') has been provided on the URL
   // and that URL parameters have been provided
   if ((url.extension === null) || (args.length == 0)) 
   {
      // handle action not provided or no parameters given
      status.code = 400;
      status.message = "Action has not been provided in URL or " +
      "no parameters have been provided on URL";
      status.redirect = true;
   }
   else 
   {
      // handle action provided in URL ('start' or 'cancel')
      var action = url.extension;
      
      // handle if provided 'action' is 'start'
      if (action == "start") 
      {
         // check for 'siteShortName' parameter
         if ((args["siteShortName"] === null) || (args["siteShortName"].length == 0)) 
         {
            // handle siteShortName URL parameter not provided
            status.code = 400;
            status.message = "'siteShortName' parameter has not been provided in URL for action 'start'";
            status.redirect = true;
         }
         else 
         {
            start(args["siteShortName"]);
         }
      }
      // else handle if provided 'action' is 'cancel' 
      else if (action == "cancel") 
      {
         // check for 'workflowId' parameter
         if ((args["workflowId"] === null) || (args["workflowId"].length == 0)) 
         {
            // handle workflowId URL parameter not provided
            status.code = 400;
            status.message = "'workflowId' parameter has not been provided in URL for action 'cancel'";
            status.redirect = true;
         }
         else 
         {
            cancel(args["workflowId"]);
         }
      }
      // handle action not recognised
      else 
      {
         status.code = 400;
         status.message = "Action, '" + action + "', provided in URL has not been recognised.";
         status.redirect = true;
      }
   }
}

main();
