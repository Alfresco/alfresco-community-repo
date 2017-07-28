function main()
{
   // Get the site 
   var shortName = url.extension.split("/")[0];
   var site = siteService.getSite(shortName);
   if (site == null)
   {
      // Site cannot be found
      status.setCode(status.STATUS_NOT_FOUND, "The site " + shortName + " does not exist.");
      return;
   }
   
   // Get the role 
   var role = json.get("role");
   if (role == null)
   {
      status.setCode(status.STATUS_BAD_REQUEST, "The role has not been set.");
      return;
   }
   
   // Are we adding a person?
   if (json.has("person"))
   {
      // Get the user name
      var userName = json.getJSONObject("person").get("userName");
      if (userName == null)
      {
         status.setCode(status.STATUS_BAD_REQUEST, "The user name has not been set.");
         return;
      }
      var person = people.getPerson(userName);
      if (person == null)
      {
         status.setCode(status.STATUS_BAD_REQUEST, "The person with user name " + userName + " could not be found.");
         return;
      }
   
      // Set the membership details
      site.setMembership(userName, role);
      // Pass the details to the template
      model.site = site;
      model.role = role;
      model.authority = person;
      return;
   }
   
   // Are we adding a group ?
   if (json.has("group"))
   {
      // Get the user name
      var groupName = json.getJSONObject("group").get("fullName");
      if (groupName == null)
      {
         status.setCode(status.STATUS_BAD_REQUEST, "The fullName for the group has not been set.");
         return;
      }
      
      if (groupName.match("^GROUP_") == null)
      {
         status.setCode(status.STATUS_BAD_REQUEST, "Group Authority names should begin with 'GROUP_'.");
         return;  
      }
         
      var group = groups.getGroupForFullAuthorityName(groupName);
      if (group == null)
      {
         status.setCode(status.STATUS_BAD_REQUEST, "The group with group name " + groupName + " could not be found.");
         return;
      }
   
      // Set the membership details
      site.setMembership(groupName, role);
      // Pass the details to the template
      model.site = site;
      model.role = role;
      model.authority = group;
      return;
   }
   
   if (json.has("authority"))
   {
      // Get the user name
      var authorityName = json.getJSONObject("authority").get("fullName");
      if (authorityName == null)
      {
         status.setCode(status.STATUS_BAD_REQUEST, "The fullName for the authority has not been set.");
         return;
      }
      
      var authority;
      if (authorityName.match("^GROUP_") != null)
      {
         // authority is a group
         authority = groups.getGroupForFullAuthorityName(authorityName);
      }
      else
      {
         // assume a person
         authority = people.getPerson(authorityName);
      }
      
      if (authority == null)
      {
         status.setCode(status.STATUS_BAD_REQUEST, "The authority with name " + authorityName + " could not be found.");
         return;
      }
      
      // Set the membership details
      site.setMembership(authorityName, role);
      // Pass the details to the template
      model.site = site;
      model.role = role;
      model.authority = authority;
      
      return;
   }
   
   // Neither person or group specified.
   status.setCode(status.STATUS_BAD_REQUEST, "person or group has not been set.");
   return;
}

main();