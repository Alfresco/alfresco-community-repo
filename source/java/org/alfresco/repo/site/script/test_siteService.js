var failure = "";

// Try and create a site
var site = siteService.createSite("sitePreset", "siteShortName", "siteTitle", "siteDescription", true);

// Check that the site details are correct
if (site.sitePreset != "sitePreset")
{
	failure += "\nSite preset is not set on created site";
}
if (site.shortName != "siteShortName")
{
	failure += "\nSite short name is not set on created site";
}
if (site.title != "siteTitle")
{
	failure += "\nSite title is not set on created site";
}
if (site.description != "siteDescription")
{
	failure += "\nSite description is not set on created site";
}
if (site.isPublic != true)
{
	failure += "\nCreated site should be marked public";
}

// Return the failure message
failure;