var SITE_ROLE_COLLABORATOR = "collaborator";

/**
 * Processes 'accept' response from invitee
 *
 * @method accept
 * @static
 * @param workflowId string id of invite process workflow instance
 * @param inviteeUserName string user name of invitee 
 * @param siteShortName string short name of site for which invitee is accepting invitation to join  
 */
function accept(workflowId, inviteeUserName, siteShortName)
{
   var wfInstance = workflow.getInstance(workflowId);
   var wfPath = wfInstance.getPaths()[0];
   wfPath.signal("accept");
   
   people.enablePerson(inviteeUserName);
   
   /* TODO glen johnson at alfresco dot com -
    * Find out role string that Invitee should be added to Site as
    */
   // Add Invitee to Site
   var site = sites.getSite(siteShortName);
   site.setMembership(username, SITE_ROLE_COLLABORATOR);
   
   // add data to appear in rendition
   model.response = "accept";
   model.siteShortName = siteShortName;
}

/**
 * Processes 'reject' invite response from invitee
 *
 * @method reject
 * @static
 * @param workflowId string id of invite process workflow instance
 * @param inviteeUserName string user name of invitee 
 * @param siteShortName string short name of site for which invitee is rejecting invitation to join  
 */
function reject(workflowId, inviteeUserName, siteShortName)
{
   var wfInstance = workflow.getInstance(wfid);
   var wfPath = wfInstance.getPaths()[0];
   wfPath.signal("reject");
   
   // add data to appear in rendition
   model.response = "reject";
   model.siteShortName = siteShortName;
}

function main()
{
   // check that response has been provided
   if (url.extension === null || url.extension.length == 0) 
   {
      // handle response not provided
      status.code = 400;
      status.message = "response has not been provided as part of URL.";
      status.redirect = true;
   }
   // check that workflow id URL parameter has been provided
   else if ((args["workflowId"] === null) || (args["workflowId"].length == 0)) 
   {
      // handle workflow id not provided
      status.code = 400;
      status.message = "workflow id parameter has not been provided in the URL.";
      status.redirect = true;
   }
   // check that inviteeUserName URL parameter has been provided
   else if ((args["inviteeUserName"] === null) || (args["inviteeUserName"].length == 0)) 
   {
      // handle inviteeUserName not provided
      status.code = 400;
      status.message = "inviteeUserName parameter has not been provided in the URL.";
      status.redirect = true;
   }
   // check that siteShortName URL parameter has been provided
   else if ((args["siteShortName"] === null) || (args["siteShortName"].length == 0)) 
   {
      // handle siteShortName not provided
      status.code = 400;
      status.message = "siteShortName parameter has not been provided in the URL.";
      status.redirect = true;
   }
   else 
   {
      // process response
      var response = url.extension;
      var workflowId = args["workflowId"];
      var inviteeUserName = args["inviteeUserName"];
      var siteShortName = args["siteShortName"];
      if (response == "accept") 
      {
         accept(workflowId, inviteeUserName, siteShortName);
      }
      else if (response == "reject") 
      {
         reject(workflowId, inviteeUserName, siteShortName);
      }
      else 
      {
         /* handle unrecognised response */
         status.code = 400;
         status.message = "response, " + response + ", provided in URL has not been recognised.";
         status.redirect = true;
      }
   }
}

main();
