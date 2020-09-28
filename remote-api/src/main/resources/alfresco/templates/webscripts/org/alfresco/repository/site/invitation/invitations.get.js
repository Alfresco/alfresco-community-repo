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
   
   var invites = site.listInvitations(props);
   
   //Get the avatars for the invitees.
   var avatars = new Object();
   for (var i = 0; i<invites.length; i++)
   {
      var invite = invites[i];
      var userName = invite.inviteeUserName;
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
   }

   
   // Pass the information to the template
   model.invitations = invites;
   model.avatars = avatars;
}

main();