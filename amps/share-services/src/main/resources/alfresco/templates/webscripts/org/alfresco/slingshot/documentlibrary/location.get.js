<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/filters.lib.js">
<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/parse-args.lib.js">

/**
 * Main entry point: Return scope of nodeRef
 *
 * @method resolvePath
 */
function resolveLocations()
{
   // Use helper function to get the arguments
   var parsedArgs = ParseArgs.getParsedArgs();
   if (parsedArgs === null)
   {
      return;
   }
   var libraryRoot = parsedArgs.libraryRoot ? parsedArgs.libraryRoot : companyhome, 
      repoLocation = Common.getLocation(parsedArgs.rootNode, libraryRoot),
      siteLocation = Common.getLocation(parsedArgs.rootNode, null),
      fileName = parsedArgs.rootNode.properties["name"];

   var locations =
   {
      repo:
      {
         path: repoLocation.path,
         file: fileName ? fileName : null
      }
   };

   if (siteLocation.site)
   {
      locations.site =
      {
         path: siteLocation.path,
         file: fileName ? fileName : null,
         site: siteLocation.site,
         siteTitle: siteLocation.siteTitle,
         container: siteLocation.container
      };
   }

   return locations;
}

/**
 *
 */
model.locations = resolveLocations();