var SITE_ROLE_COLABORATOR = "collaborator";

/**
 * Processes 'accept' response from invitee
 * 
 * @method accept
 * @static
 * @param {int} wfid
 * @param (string) username
 * @param (string) sitename 
 */
function accept(wfid, username, sitename)
{
    var wfInstance = workflow.getInstance(wfid);
    var wfPath = wfInstance.getPaths()[0];
    wfPath.signal("accept");
    
    // TODO glen.johnson@alfresco.com Somehow activate inactive Invitee account
    
    /* TODO glen.johnson@alfresco.com Find out role string that Invitee
     * should be added to Site as
     */
    
    // Add Invitee to Site
    var site = sites.getSite(sitename);
    site.setMembership(username, SITE_ROLE_COLABORATOR);    
}

/**
 * Processes 'reject' response from invitee
 * 
 * @method reject
 * @static
 * @param {int} wfid
 */
function reject(wfid)
{
    var wfInstance = workflow.getInstance(wfid);
    var wfPath = wfInstance.getPaths()[0];
    wfPath.signal("reject");
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
   // check that workflow id has been provided
   else if (args.wfid === null || args.wfid.length == 0)
   {
      // handle response not provided
      status.code = 400;
      status.message = "workflow id parameter has not been provided in the URL.";
      status.redirect = true;
   }
   else
   {
      // process response
      var response = url.extension;
      var wfid = args.wfid;
      if (response == "accept") 
      {
         accept(wfid);
      }
      else if (response == "reject") 
      {
         reject(wfid);
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
