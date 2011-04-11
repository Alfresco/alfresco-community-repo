/**
 * List/Search root groups
 */ 

function main ()
{
   // Get the args
   var shortNameFilter = args["shortNameFilter"];
   var zone = args["zone"];
   var maxItems= args["maxItems"];
   var skipCount= args["skipCount"];
     
     if(maxItems == null)
     {
         maxItems = -1;
     }
     
     if(skipCount == null)
     {
         skipCount = -1;
     }
     
   if(shortNameFilter == null)
   {
      model.groups = groups.getAllRootGroupsInZone(zone, maxItems, skipCount);
   }
   else
   {
      // Do the search
      model.groups = groups.searchRootGroupsInZone(shortNameFilter, zone, maxItems, skipCount);
   }
}

main();