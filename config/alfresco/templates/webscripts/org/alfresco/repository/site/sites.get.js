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
        
        if (size < sites.length)
        {
            // Only return the first n sites based on the passed page size
            var pagedSites = new Array(size);
            for (var index = 0; index < size; index++)
            {
                pagedSites[index] = sites[index];   
            }
            
            sites = pagedSites;
        }
    }
    
    model.sites = sites;
}

main();