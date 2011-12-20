/**
 * Create / Post / Invitation
 */
function main()
{
   var invitation = null;
   
   // Get the web site site 
   var shortName = url.extension.split("/")[0];
   var site = siteService.getSite(shortName);
   if (site == null)
   {
      // Site cannot be found
      status.setCode(status.STATUS_NOT_FOUND, "The site " + shortName + " does not exist.");
      return;
   }
   
   if (!json.has("invitationType"))
   {
      status.setCode(status.STATUS_BAD_REQUEST, "The invitationType has not been set.");
      return;
   }
   
   // Get the role 
   var invitationType = json.get("invitationType");
   if (invitationType == null || invitationType.length == 0)
   {
      status.setCode(status.STATUS_BAD_REQUEST, "The invitationType is null or empty.");
      return;
   }
   
   if (!invitationType.match("[MODERATED]|[NOMINATED]"))
   {
      status.setCode(status.STATUS_BAD_REQUEST, "The invitationType has does not have a correct value.");
      return;
   }
   
   if (invitationType == "MODERATED")
   {
      // Check mandatory parameters
      if (!json.has("inviteeRoleName"))
      {
         status.setCode(status.STATUS_BAD_REQUEST, "The inviteeRoleName has not been set.");
         return;
      }
      
      if (!json.has("inviteeUserName"))
      {
         status.setCode(status.STATUS_BAD_REQUEST, "The inviteeUserName has not been set.");
         return;
      }
      
      // Get the role 
      var inviteeRoleName = json.get("inviteeRoleName");
      if (inviteeRoleName == null || inviteeRoleName == "")
      {
         status.setCode(status.STATUS_BAD_REQUEST, "The inviteeRoleName has not been set.");
         return;
      }
      
      var inviteeComments = json.get("inviteeComments");
      if (inviteeComments == null)
      {
         status.setCode(status.STATUS_BAD_REQUEST, "The inviteeComments has not been set.");
         return;
      }
      
      var inviteeUserName = json.get("inviteeUserName");
      if (inviteeUserName == null || inviteeUserName == "")
      {
         status.setCode(status.STATUS_BAD_REQUEST, "The userName has not been set.");
         return;
      }
      
      invitation = site.inviteModerated(inviteeComments, inviteeUserName, inviteeRoleName);
   }  
   
   if (invitationType == "NOMINATED")
   {
      // Get mandatory properties  
      if (!json.has("inviteeRoleName"))
      {
         status.setCode(status.STATUS_BAD_REQUEST, "The inviteeRoleName has not been set.");
         return;
      }
      var inviteeRoleName = json.get("inviteeRoleName");
      if (inviteeRoleName == null || inviteeRoleName == "")
      {
         status.setCode(status.STATUS_BAD_REQUEST, "The inviteeRoleName is null or empty.");
         return;
      }
      var acceptUrl = json.get("acceptURL");
      var rejectUrl = json.get("rejectURL"); 
      
      // Get the optional properties
      if (json.has("inviteeUserName") && json.get("inviteeUserName") != "")
      {
         invitation = site.inviteNominated(json.get("inviteeUserName"), inviteeRoleName, acceptUrl, rejectUrl); 
      } 
      else
      {
         // Get mandatory properties  
         if (!json.has("inviteeFirstName"))
         {
            status.setCode(status.STATUS_BAD_REQUEST, "The inviteeFirstName has not been set.");
            return;
         }
         if (!json.has("inviteeLastName"))
         {
            status.setCode(status.STATUS_BAD_REQUEST, "The inviteeLastName has not been set.");
            return;
         }
         if (!json.has("inviteeEmail"))
         {
            status.setCode(status.STATUS_BAD_REQUEST, "The inviteeEmail has not been set.");
            return;
         }
         
         var inviteeFirstName = json.get("inviteeFirstName")  ;
         var inviteeLastName = json.get("inviteeLastName") ;
         var inviteeEmail = json.get("inviteeEmail") ;
         invitation = site.inviteNominated(inviteeFirstName, inviteeLastName, inviteeEmail, inviteeRoleName, acceptUrl, rejectUrl); 
      }
   }
   
   // Pass the model to the results template
   model.site = site;
   model.invitation = invitation;
   
   status.code = status.STATUS_CREATED; 
}

main();