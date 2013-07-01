function main()
{
   // Ensure the user has Create Site capability
   if (!siteService.hasCreateSitePermissions())
   {
      status.setCode(status.STATUS_FORBIDDEN, "error.noPermissions");
      return;
   }
   
   // Get the details of the site
   if (json.has("shortName") == false || json.get("shortName").length == 0)
   {
      status.setCode(status.STATUS_BAD_REQUEST, "Short name missing when creating site");
      return;
   }
   var shortName = json.get("shortName");
   
   // See if the shortName is available
   if (siteService.hasSite(shortName))
   {
      status.setCode(status.STATUS_BAD_REQUEST, "error.duplicateShortName");
      return;
   }
   
   if (json.has("sitePreset") == false || json.get("sitePreset").length == 0)
   {
      status.setCode(status.STATUS_BAD_REQUEST, "Site preset missing when creating site");
      return;
   }
   var sitePreset = json.get("sitePreset");
   
   var title = null;
   if (json.has("title"))
   {
      title = json.get("title");
   }
      
   var description = null;
   if (json.has("description"))
   {
      description = json.get("description");
   }
   
   var sitetype = null;
   if (json.has("type") == true)
   {
      sitetype = json.get("type");
   }
   
   // Use the visibility flag before the isPublic flag
   var visibility = siteService.PUBLIC_SITE;
   if (json.has("visibility"))
   {
      visibility = json.get("visibility");
   }
   else if (json.has("isPublic"))
   {
      var isPublic = json.getBoolean("isPublic");
      if (isPublic == true)
      {
         visibility = siteService.PUBLIC_SITE;
      }
      else
      {
         visibility = siteService.PRIVATE_SITE;
      }
   }
   
   // Create the site 
   var site = null;
   if (sitetype == null)
   {
      site = siteService.createSite(sitePreset, shortName, title, description, visibility);
   }
   else
   {
      site = siteService.createSite(sitePreset, shortName, title, description, visibility, sitetype);
   }
   
   // Put the created site into the model
   model.site = site;
}

main();