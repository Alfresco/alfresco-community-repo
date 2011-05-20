/**
 * List/Search groups
 */

function main ()
{
	// Get the args
	var shortNameFilter = args["shortNameFilter"];
	var zone = args["zone"];
   var sortBy = args["sortBy"];
   var paging = utils.createPaging(args);
	
	if(shortNameFilter == null)
	{
		shortNameFilter = "";
	}

   if(sortBy == null)
   {
      sortBy = "authorityName";
   }
	
	// Do the search
	model.groups = groups.searchGroupsInZone(shortNameFilter, zone, paging, sortBy);
   model.paging = paging;
}

main();
