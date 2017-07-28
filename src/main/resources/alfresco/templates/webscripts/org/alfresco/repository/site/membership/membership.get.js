function main()
{
   // Get the url values
   var urlElements = url.extension.split("/");
   var shortName = urlElements[0];
   var userName = urlElements[2];
   
   // Get the site
   var site = siteService.getSite(shortName);
   if (site == null)
   {
      // Site cannot be found
      status.setCode(status.STATUS_NOT_FOUND, "The site " + shortName + " does not exist.");
      return;
   }
   
   var authority;
   if (userName.match("^GROUP_"))
   {   
      authority = groups.getGroupForFullAuthorityName(userName);
      if (authority == null)
      {
         // Person cannot be found
         status.setCode(status.STATUS_NOT_FOUND, "The group with full  name " + userName + " does not exist.");
         return;
      }
   }
   else
   {
      authority = people.getPerson(userName);
      if (authority == null)
      {
         // Person cannot be found
         status.setCode(status.STATUS_NOT_FOUND, "The person with user name " + userName + " does not exist.");
         return;
      }
   }
   
   // Get the role info for the user
   var role = site.getMembersRoleInfo(userName);
   if (role == null)
   {
      // Person is not a member of the site
      status.setCode(status.STATUS_NOT_FOUND, "The person with user name " + userName + " is not a member of the site " + shortName);
      return;
   }
   
   // Pass the values to the template
   model.site = site;
   model.authority = authority;
   model.role = role;
}

main();