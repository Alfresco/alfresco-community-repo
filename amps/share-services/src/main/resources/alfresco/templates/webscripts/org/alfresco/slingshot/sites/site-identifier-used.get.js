// See SHA-1612
// This WebScript has been added to support the ability to determine whether or not a site title or shortName
// has already been used. It runs with admin privileges but does not return anything other than a true/false
// response to prevent exploratory disclosure of private data.
function main()
{
   var foundMatch = false;

   var shortName = args["shortName"];
   var title = args["title"];

   var useShortName = !!shortName;
    
   // Get the list of sites
   var sites = siteService.findSites(shortName || title, -1);
   if (sites && sites.length)
   {
      for (var i=0; i<sites.length && !foundMatch; i++)
      {
         if (useShortName)
         {
            foundMatch = (sites[i].shortName.toLowerCase() == shortName.toLowerCase());
         }
         else
         {
            foundMatch = (sites[i].title.toLowerCase() == title.toLowerCase());
         }
      }
   }
   model.foundMatch = foundMatch;
}

main();