function checkSite(site, sitePreset, shortName, title, description, isPublic)
{
	test.assertNotNull(site);
	test.assertEquals(sitePreset, site.sitePreset);
	test.assertEquals(shortName, site.shortName);
	test.assertEquals(title, site.title);
	test.assertEquals(description, site.description);
	test.assertEquals(isPublic, site.isPublic);
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
	// Create a couple of sites
	siteService.createSite("sitePreset", "siteShortName", "siteTitle", "siteDescription", true);
	siteService.createSite("sitePreset", "siteShortName2", "siteTitle", "siteDescription", true);
	
	// List all the site
	var sites = siteService.listSites(null, null);
	
	// Check the list
	test.assertNotNull(sites);
	test.assertEquals(2, sites.length);
	
	// TODO .. check the filters
}

function testMembership()
{
	var site = siteService.getSite("siteShortName");
	test.assertNotNull(site);
	
	var members = site.listMembers(null, null);
	test.assertNotNull(members);
	test.assertEquals(1, members.length);
	test.assertEquals("SiteManager", members["UserOne"]);
	
	site.setMembership("UserTwo", "SiteCollaborator");
	members = site.listMembers(null, null);
	test.assertNotNull(members);
	test.assertEquals(2, members.length);
	test.assertEquals("SiteManager", members["UserOne"]);
	test.assertEquals("SiteCollaborator", members["UserTwo"]);
	
	site.removeMembership("UserTwo");
	members = site.listMembers(null, null);
	test.assertNotNull(members);
	test.assertEquals(1, members.length);
	test.assertEquals("SiteManager", members["UserOne"]);
	
}

// Execute test's
testCRUD();
testListSites();
testMembership();