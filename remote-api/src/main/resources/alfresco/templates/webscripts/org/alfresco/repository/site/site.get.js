function main()
{
   // Get the shortname
   var shortName = url.extension;
   
   // Get the site
   var site = siteService.getSite(shortName);
   
   if (site != null)
   {
      // Pass the site to the template
      model.site = site;
   }
   else
   {
      // Return 404
      status.setCode(404, "Site " + shortName + " does not exist");
      return;
   }
}

main();