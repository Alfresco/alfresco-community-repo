/**
 * List/Search root groups
 */ 

function main ()
{
   // Get the args
   var shortNameFilter = args["shortNameFilter"];
   var zone = args["zone"];
     
   if(zone == null)
   {
       if(shortNameFilter == null)
       {
           model.groups = groups.getAllRootGroups();
       }
       else
       {
           // Do the search
           model.groups = groups.searchRootGroups(shortNameFilter);
       }
   }
   else
   {
       if(shortNameFilter == null)
       {
           model.groups = groups.getAllRootGroupsInZone(zone);
       }
       else
       {
           // Do the search
           model.groups = groups.searchRootGroupsInZone(shortNameFilter, zone);
       }
   }
}

main();