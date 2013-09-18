function main()
{
   // Get the user name of the person to get
   var userName = url.templateArgs.userid;
    
   // Get the person who has that user name
   var person = people.getPerson(userName);
    
   if (person === null)
   {
      // Return 404 - Not Found
      status.setCode(status.STATUS_NOT_FOUND, "Person " + userName + " does not exist");
      return;
   }

   // Get the filter param
   var filter = url.templateArgs.filter

   if (filter)
   {
      if (filter !== "recent" && filter !== "favourites")
      {
         // Return 404 - Not Found
         status.setCode(status.STATUS_NOT_FOUND, "Filter " + filter + " is not recognised");
         return;
      }
   }

   // Get the list of sites
   var size = 0,
      sizeString = args["size"];
   if (sizeString != null)
   {
      size = parseInt(sizeString);
   }

   if (filter)
   {
      var sites = siteService.listUserSites(userName);
      var filterObj = {},
         filteredSites = [];

      if (filter == "recent")
      {
         var recentSites = preferenceService.getPreferences(userName, "org.alfresco.share.sites.recent");
         if (recentSites['org'] != null)
         {
            filterObj = recentSites.org.alfresco.share.sites.recent;
         }
      }
      else if (filter == "favourites")
      {
         var favouriteSites = preferenceService.getPreferences(userName, "org.alfresco.share.sites.favourites");
         if(favouriteSites['org'] != null)
         {
            filterObj = favouriteSites.org.alfresco.share.sites.favourites;
         }
      }

      var i = 0;
      while (i < sites.length)
      {
         for (var key in filterObj)
         {
            if (filterObj[key] == sites[i].shortName || key == sites[i].shortName)
            {
               if (filter != "favourites" ||
                   filterObj[key] == true)
               {
                  filteredSites.push(sites[i]);
                  
                  // If the caller of this webscript has requested a specific result size (non-zero) then do not return more than they asked for
                  if (size > 0 && filteredSites.length == size)
                  {
                     break;
                  }
                  
               }
            }
         }
         i++;
      }

      model.sites = filteredSites;
   }
   else
   {
      var sites = siteService.listUserSites(userName, size);

      // Sort sites alphabetically by title, ignoring case.
      sites.sort(function(a,b)
      {
         if(a.title.toLowerCase() < b.title.toLowerCase()) return -1;
         if(a.title.toLowerCase() > b.title.toLowerCase()) return 1;
      })
      model.sites = sites;
   }

   model.roles = (args["roles"] !== null ? args["roles"] : "managers");
}

main();