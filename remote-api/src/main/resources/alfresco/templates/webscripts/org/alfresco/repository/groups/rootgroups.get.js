/**
 * List/Search root groups
 */ 

function main ()
{
   // Get the args
   var shortNameFilter = args["shortNameFilter"];
   var zone = args["zone"];
   var sortBy = args["sortBy"];
   var paging = utils.createPaging(args);
     
   if(sortBy == null)
   {
      sortBy = "authorityName";
   }
	
   if(shortNameFilter == null)
   {
      model.groups = groups.getAllRootGroupsInZone(zone, paging, sortBy);
      model.paging = paging;
   }
   else
   {
      // Do the search
      model.groups = groups.searchRootGroupsInZone(shortNameFilter, zone, paging, sortBy);
      model.paging = paging;
   }
}

main();
