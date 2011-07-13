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
	
	if (shortNameFilter == null)
	{
		shortNameFilter = "";
	}

   if (sortBy == null)
   {
      sortBy = "displayName";
   }
	
	// Get the groups
	model.groups = groups.getGroupsInZone(shortNameFilter, zone, paging, sortBy);
   model.paging = paging;
}

main();
