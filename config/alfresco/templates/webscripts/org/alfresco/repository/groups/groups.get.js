/**
 * List/Search groups
 */

function main ()
{
	// Get the args
	var shortNameFilter = args["shortNameFilter"];
	var zone = args["zone"];
	
	if(shortNameFilter == null)
	{
		shortNameFilter = "";
	}
	
	if(zone == null)
	{
	    // Do the search
	    model.groups = groups.searchGroups(shortNameFilter);
	   
	}
	else
	{
	    // Do the search
	    model.groups = groups.searchGroupsInZone(shortNameFilter, zone);
	}
}

main();