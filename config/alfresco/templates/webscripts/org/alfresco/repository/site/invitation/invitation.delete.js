/**
 * Cancel invitation for a web site
 */
function main()
{
   // Get the url values
   var urlElements = url.extension.split("/");
   var shortName = urlElements[0];
   var inviteId = urlElements[2];
   
   // Get the site
   var site = siteService.getSite(shortName);
   if (site == null)
   {
      // Site cannot be found
      status.setCode(status.STATUS_NOT_FOUND, "The site " + shortName + " does not exist.");
      return;
   }
   
   // Need to cancel an invitation here
   var invitation = site.getInvitation(inviteId);
   if (invitation == null)
   {
      // Site cannot be found
      status.setCode(status.STATUS_NOT_FOUND, "The invitation :" + inviteId + " for web site :" + shortName + ", does not exist.");
      return;
   }
   
   // Cancel the invitation
   invitation.cancel();
}

main();