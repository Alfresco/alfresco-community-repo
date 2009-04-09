/**
 * List invitations implementation
 */
function main ()
{
   // Get the site id
   var urlElements = url.extension.split("/");
   var shortName = urlElements[0];
   
   // Get the args
   var inviteeUserName = args["inviteeUserName"];
   var invitationType = args["invitationType"];
   
   var site = siteService.getSite(shortName);
   if (site == null)
   {
      // Site cannot be found
      status.setCode(status.STATUS_NOT_FOUND, "The site " + shortName + " does not exist.");
      return;
   }
   
   var props = {};
   
   if (inviteeUserName != null)
   {
      props.inviteeUserName = inviteeUserName
   }
   if (invitationType != null)
   {
      props.invitationType = invitationType
   }
   
   var invitations = site.listInvitations(props);
   
   // Pass the information to the template
   model.invitations = invitations;
}

main();