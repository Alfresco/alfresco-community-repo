function main()
{
   var savedSearches = [],
       siteId = url.templateArgs.site,
       siteNode = siteService.getSite(siteId),
       bPublic = args.p;

   if (siteNode === null)
   {
      status.setCode(status.STATUS_NOT_FOUND, "Site not found: '" + siteId + "'");
      return null;
   }
   
   var searchNode = siteNode.getContainer("Saved Searches");
   if (searchNode != null)
   {
      var kids, ssNode;
      
      if (bPublic == null || bPublic == "true")
      {
         // public searches are in the root of the folder
         kids = searchNode.children;
      }
      else
      {
         // user specific searches are in a sub-folder of username
         var userNode = searchNode.childByNamePath(person.properties.userName);
         if (userNode != null)
         {
            kids = userNode.children;
         }
      }
      
      if (kids)
      {
         for (var i = 0, ii = kids.length; i < ii; i++)
         {
            ssNode = kids[i];
            if (ssNode.isDocument)
            {
               savedSearches.push(
               {
                  name: ssNode.name,
                  description: ssNode.properties.description
               });
            }
         }
      }
   }
   
   model.savedSearches = savedSearches;
}

main();