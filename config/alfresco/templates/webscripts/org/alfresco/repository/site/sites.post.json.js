function main()
{
   // The commented out code below checks if the current user has the necessary permissions to
   // create a site and retrns a 401 status if they do not.
   //
   // However, the presentation tier currently handles 500 errors, but not 400 errors.
   // Therefore the UNAUTHORIZED status is not currently returned.
   // If a user who does not have permission to create a site tries to do so, a dialog
   // appears in Share telling them AccessDenied. You do not have the appropriate permissions
   // to perform this operation.
   // TODO If we can fix up create-site.js in Slingshot to handle 401s, we can comment this back in.


   // Irrespective of the checks below, the currently authenticated user needs to have
   // permission to create a site.
//   if (siteService.hasCreateSitePermissions() == false)
//   {
//      status.setCode(status.STATUS_UNAUTHORIZED, "User does not have permission to create sites.");
//      return;
//   }
   
   // Get the details of the site
   if (json.has("shortName") == false || json.get("shortName").length == 0)
   {
      status.setCode(status.STATUS_BAD_REQUEST, "Short name missing when creating site");
      return;
   }
   var shortName = json.get("shortName");
   
   // See if the shortName is available
   var site = siteService.getSite(shortName);
   if (site != null)
   {
      status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, "error.duplicateShortName");
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