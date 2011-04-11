/**
 * List/Search groups
 */

function main ()
{
	// Get the args
	var shortNameFilter = args["shortNameFilter"];
	var zone = args["zone"];
	var maxItems = args["maxItems"];
	var skipCount = args["skipCount"];
	
	if(shortNameFilter == null)
	{
		shortNameFilter = "";
	}
	
	if(maxItems== null)
	{
		maxItems = -1;
	}
	
	if(skipCount== null)
	{
		skipCount = -1;
	}
	
	// Do the search
	model.groups = groups.searchGroupsInZone(shortNameFilter, zone, maxItems, skipCount);
}

main();