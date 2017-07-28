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
   
   var avatars = new Object();
   // Get the avatar. 
   var userName = invitation.inviteeUserName;
   var person = people.getPerson(userName);
   if (person != undefined && person !=null)
   {
      var assocs = person.assocs['{http://www.alfresco.org/model/content/1.0}avatar'];
      if (assocs !=null && assocs.length>0)
      {
         var avatar = 'api/node/';
         avatar = avatar + assocs[0].nodeRef.toString();
         avatar = avatar + '/content/thumbnails/avatar';
         avatars[userName] = avatar;
      }
   }
   
   // Pass the model to the template
   model.invitation = invitation;
   model.site = site;
   model.avatars=avatars;
}
   
main();