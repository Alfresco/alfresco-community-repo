function main()
{
   // Get the filter parameters
   var nameFilter = args["nf"];
   var sitePreset = args["spf"];
   var sizeString = args["size"];
   var size = sizeString != null ? parseInt(sizeString) : -1;
   var asSiteAdmin = (args["admin"] == "true");

   // Get the list of sites
   var sites;
   if (asSiteAdmin)
   {
      // The user's access right is checked within the getSitesAsSiteAdmin method.
      sites = siteService.getSitesAsSiteAdmin(nameFilter, sitePreset, size);
   }
   else
   {
      sites = siteService.getSites(nameFilter, sitePreset, size);
   }
   model.sites = sites;
   model.roles = (args["roles"] !== null ? args["roles"] : "managers");
}

main();