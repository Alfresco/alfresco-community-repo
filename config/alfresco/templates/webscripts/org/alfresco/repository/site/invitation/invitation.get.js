/**
 * Get Invitation web script
 */
function main()
{
   // Get the site id
   var urlElements = url.extension.split("/");
   var shortName = urlElements[0];
   var inviteId = urlElements[2];
   
   var site = siteService.getSite(shortName);
   if (site == null)
   {
      // Site cannot be found
      status.setCode(status.STATUS_NOT_FOUND, "The site " + shortName + " does not exist.");
      return;
   }
   
   var invitation = site.getInvitation(inviteId);
   if (invitation == null)
   {
      // Site cannot be found
      status.setCode(status.STATUS_NOT_FOUND, "The invitation :" + inviteId + " for web site :" + shortName + ", does not exist.");
      return;
   }
   
   // Pass the model to the template
   model.invitation = invitation;
   model.site = site;
}
   
main();