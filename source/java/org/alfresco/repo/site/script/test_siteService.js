function checkSite(site, sitePreset, shortName, title, description, isPublic)
{
	test.assertNotNull(site, "Site should not be null");
	test.assertEquals(sitePreset, site.sitePreset, "Site preset incorrect for site " + shortName);
	test.assertEquals(shortName, site.shortName, "Site shortname incorrect");
	test.assertEquals(title, site.title, "Site title incorrect for site " + shortName);
	test.assertEquals(description, site.description, "Site description incorrect for site " + shortName);
	test.assertEquals(isPublic, site.isPublic, "Site ispublic incorrect for site " + shortName);
}

function testCRUD()
{
	// Try and get a site that doesn't exist
	var site = siteService.getSite("siteShortName");
	test.assertNull(site, "Site should not have been found.");
	
	// Try and create a site
	site = siteService.createSite("sitePreset", "siteShortName", "siteTitle", "siteDescription", true);
	checkSite(site, "sitePreset", "siteShortName", "siteTitle", "siteDescription", true);
	
	// Try and get the created site
	site = siteService.getSite("siteShortName");
	checkSite(site, "sitePreset", "siteShortName", "siteTitle", "siteDescription", true);
	
	// Try and update the values of the site
	site.title = "abc123abc";
	site.description = "abc123abc";
	site.isPublic = false;
	checkSite(site, "sitePreset", "siteShortName", "abc123abc", "abc123abc", false);
	site.save();
	site = siteService.getSite("siteShortName");
	checkSite(site, "sitePreset", "siteShortName", "abc123abc", "abc123abc", false);
	
	// Delete the site
	site.deleteSite();
	site = siteService.getSite("siteShortName");
	test.assertNull(site, "");
}

function testListSites()
{
	// TODO
}

// Execute test's
testCRUD();
testListSites();