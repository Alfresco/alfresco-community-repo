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
   var sites = siteService.listUserSites(userName, size);

   if (filter)
   {
      var filterObj = {},
         filteredSites = [];

      if (filter == "recent")
      {
         var recentSites = preferenceService.getPreferences(userName, "org.alfresco.share.sites.recent")
         filterObj = recentSites.org.alfresco.share.sites.recent;
      }
      else if (filter == "favourites")
      {
         var favouriteSites = preferenceService.getPreferences(userName, "org.alfresco.share.sites.favourites");
         if(favouriteSites['org'] != null)
         {
            filterObj = favouriteSites.org.alfresco.share.sites.favourites;
         }
      }

      for (var i = 0; i < sites.length; i++)
      {
         for (var key in filterObj)
         {
            if (filterObj[key] == sites[i].shortName || key == sites[i].shortName) 
            {
               if (filter != "favourites" ||
                   filterObj[key] == true)
               {
                  filteredSites.push(sites[i]);
               }
            }
         }
      }

      model.sites = filteredSites;
   }
   else
   {
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