// Get the filter parameters
var nameFilter = args["namefilter"];
var sitePreset = args["sitepresetfilter"];

// Get the list of sites
var sites = siteService.listSites(nameFilter, sitePreset);

// Pass the queried sites to the template
model.sites = sites;