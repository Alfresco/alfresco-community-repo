/**
 * Implementation of list memberships
 */

function main()
{
   // Get the site id
   var shortName = url.extension.split("/")[0];
   var site = siteService.getSite(shortName);
   
   // get the filters
   var nameFilter = args["nf"];
   var roleFilter = args["rf"];
   var authorityType = args["authorityType"];
   var sizeString = args["size"];
   var collapseGroups = false;
   
   if (authorityType != null)
   {
      if (authorityType.match("USER|GROUP") == null)
      {
         status.setCode(status.STATUS_BAD_REQUEST, "The 'authorityType' argument must be either USER or GROUP.");
         return;
      }
      if (authorityType == "USER")
      {
         collapseGroups = true;
      }
   }
   
   var sizeSearch = 0;
   if(sizeString != null)
   {
      sizeSearch = parseInt(sizeString);
   }
   
   // Get the filtered memberships
   // Comes back as a Map<String, String> containing the full authority name and role
   var memberships = site.listMembers(nameFilter, roleFilter, sizeSearch, collapseGroups);
   
   // Get a list of all the users resolved to person nodes
   var authorities = Array(memberships.length);
   var roles = {};
   
   for (userName in memberships)
   {
      var membershipRole = memberships[userName];
      if (userName.match("^GROUP_"))
      {
         if (authorityType == null || authorityType == "GROUP")
         {
            // make sure the keys are strings - as usernames may be all numbers!
            authorities['_' + userName] = groups.getGroupForFullAuthorityName(userName);
            roles['_' + userName] = membershipRole;
         }
      }
      else
      {
         if (authorityType == null || authorityType == "USER")
         {
            // make sure the keys are strings - as usernames may be all numbers!
            authorities['_' + userName] = people.getPerson(userName);
            roles['_' + userName] = membershipRole;
         }
      }
   }
   
   // Pass the information to the template
   model.site = site;
   model.roles = roles;
   model.authorities = authorities;
}

main();