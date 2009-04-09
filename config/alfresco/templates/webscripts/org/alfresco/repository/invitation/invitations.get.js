/**
 * List/Search invitations implementation
 */ 
function main()
{
   // Get the site id
   var urlElements = url.extension.split("/");
   var shortName = urlElements[0];
   
   // Get the args
   var inviteeUserName = args["inviteeUserName"];
   var invitationType = args["invitationType"];
   var resourceType = args["resourceType"];
   var resourceName = args["resourceName"];
   
   var props = {};
   
   if (inviteeUserName != null)
   {
      props.inviteeUserName = inviteeUserName
   }
   if (invitationType != null)
   {
      props.invitationType = invitationType
   }
   if (resourceType != null)
   {
      props.resourceType = resourceType
   }
   if (resourceName != null)
   {
      props.resourceName = resourceName
   }
   
   var invites = invitations.listInvitations(props);
   
   // Pass the information to the template
   model.invitations = invites;
}

main();