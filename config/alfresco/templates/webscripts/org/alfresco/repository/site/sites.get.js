function main()
{
	// Get the filter parameters
	var nameFilter = args["nf"];
	var sitePreset = args["spf"];
	var sizeString = args["size"];
	
	// Get the list of sites
	var sites = siteService.listSites(nameFilter, sitePreset);
	
	if (sizeString != null)
	{
		var size = parseInt(sizeString);
		
		if (size != NaN && size < sites.length)
		{
			// TODO this is a tempory implementaion to support preview client
			// Only return the first n sites based on the passed page size
			var pagedSites = Array();
			for (var index = 0; index < size; index++)
			{
				pagedSites[index] = sites[index];	
			}
			
			sites = pagedSites;
		}
	}
	
	// Add the sites to the model
	model.sites = sites;
}

main();
