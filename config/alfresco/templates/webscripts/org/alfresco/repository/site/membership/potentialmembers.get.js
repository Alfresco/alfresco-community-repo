function main()
{
   // Get the args
   var site = siteService.getSite(url.templateArgs.shortname),
      filter = (args.filter != null) ? args.filter : (args.shortNameFilter != null) ? args.shortNameFilter : "",
      maxResults = (args.maxResults == null) ? 0 : parseInt(args.maxResults, 10),
      authorityType = args.authorityType,
      zone = args.zone;

   if (authorityType != null)
   {
      if (authorityType.match("USER|GROUP") == null)
      {
         status.setCode(status.STATUS_BAD_REQUEST, "The 'authorityType' argument must be either USER or GROUP.");
         return;
      }
   }

   var peopleFound = [],
      groupsFound = [],
      notAllowed = [],
      i, ii, name;

   if (authorityType == null || authorityType == "USER")
   {
      // Get the collection of people
      peopleFound = people.getPeople(filter, maxResults);

      // Filter this collection for site membership
      for (i = 0, ii = peopleFound.length; i < ii; i++)
      {
         name = search.findNode(peopleFound[i]).properties.userName;
         if (site.getMembersRole(name) != null)
         {
            notAllowed.push(name);
         }
      }

      model.peopleFound = peopleFound;
   }

   if (authorityType == null || authorityType == "GROUP")
   {
      // Get the collection of groups
      if (zone == null)
      {
          groupsFound = groups.searchGroups(filter);
      }
      else
      {
          groupsFound = groups.searchGroupsInZone(filter, zone);
      }

      // Filter this collection for site membership
      for (i = 0, ii = groupsFound.length; i < ii; i++)
      {
         name = groupsFound[i].fullName;
         if (site.getMembersRole(name) != null)
         {
            notAllowed.push(name);
         }
      }

      model.groupsFound = groupsFound;
   }
   
   model.notAllowed = notAllowed;
}

main();