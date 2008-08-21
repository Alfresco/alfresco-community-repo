function checkSite(site, sitePreset, shortName, title, description, isPublic)
{
	test.assertNotNull(site);
	test.assertEquals(sitePreset, site.sitePreset);
	test.assertEquals(shortName, site.shortName);
	test.assertEquals(title, site.title);
	test.assertEquals(description, site.description);
	test.assertEquals(isPublic, site.isPublic);
	test.assertNotNull(site.node);
	test.assertTrue(site.node.isTagScope);
}

function testCRUD()
{
	// Try and get a site that doesn't exist
	var site = siteService.getSite("siteShortNameCRUD");
	test.assertNull(site, "Site should not have been found.");
	
	// Try and create a site
	site = siteService.createSite("sitePreset", "siteShortNameCRUD", "siteTitle", "siteDescription", true);
	checkSite(site, "sitePreset", "siteShortNameCRUD", "siteTitle", "siteDescription", true);
	
	// Try and get the created site
	site = siteService.getSite("siteShortNameCRUD");
	checkSite(site, "sitePreset", "siteShortNameCRUD", "siteTitle", "siteDescription", true);
	
	// Try and update the values of the site
	site.title = "abc123abc";
	site.description = "abc123abc";
	site.isPublic = false;
	checkSite(site, "sitePreset", "siteShortNameCRUD", "abc123abc", "abc123abc", false);
	site.save();
	site = siteService.getSite("siteShortNameCRUD");
	checkSite(site, "sitePreset", "siteShortNameCRUD", "abc123abc", "abc123abc", false);
	
	// Delete the site
	site.deleteSite();
	site = siteService.getSite("siteShortNameCRUD");
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
	test.assertEquals("SiteManager", members["UserOne_SiteServiceImplTest"]);
	
	site.setMembership("UserTwo_SiteServiceImplTest", "SiteCollaborator");
	members = site.listMembers(null, null);
	test.assertNotNull(members);
	test.assertEquals(2, members.length);
	test.assertEquals("SiteManager", members["UserOne_SiteServiceImplTest"]);
	test.assertEquals("SiteCollaborator", members["UserTwo_SiteServiceImplTest"]);
	
	site.removeMembership("UserTwo_SiteServiceImplTest");
	members = site.listMembers(null, null);
	test.assertNotNull(members);
	test.assertEquals(1, members.length);
	test.assertEquals("SiteManager", members["UserOne_SiteServiceImplTest"]);
	
}

function testContainer()
{
	var site = siteService.getSite("siteShortName");
	test.assertNotNull(site);

    var hasContainer = site.hasContainer("folder.component");
    test.assertFalse(hasContainer);
    
    var container = site.getContainer("folder.component");
    test.assertNull(container);
    container = site.createContainer("folder.component");
    test.assertNotNull(container);
    
    var hasContainer2 = site.hasContainer("folder.component");
    test.assertTrue(hasContainer2);
    
    var container2 = site.getContainer("folder.component");
    test.assertNotNull(container2);
    test.assertEquals(container, container2);
    
    var container3 = site.getContainer("folder.component2");
    test.assertNull(container3);
    container3 = site.createContainer("folder.component2", "cm:folder");
    test.assertNotNull(container3)
    test.assertEquals("{http://www.alfresco.org/model/content/1.0}folder", container3.type);
    var container4 = site.getContainer("folder.component3");
    test.assertNull(container4);
    container4 = site.createContainer("folder.component3", "fm:forum");
    test.assertNotNull(container4);
    test.assertEquals("{http://www.alfresco.org/model/forum/1.0}forum", container4.type);
    
    var perms = Array();
    perms["GROUP_EVERYONE"] = "SiteCollaborator";
    var containerWithPerms = site.createContainer("folder.component4", null, perms);
    var setPerms = containerWithPerms.getPermissions();
    test.assertNotNull(setPerms);
    var bFound = false;
    for (index in setPerms)
    {
    	if (setPerms[index] == "ALLOWED;GROUP_EVERYONE;SiteCollaborator")
    	{
    		bFound = true;
    	}
    }
    if (bFound == false)
    {
    	test.fail("Unable to find set permission");
    }
}

function testPermissions()
{
	var site = siteService.createSite("sitePreset", "siteShortNameToo", "siteTitle", "siteDescription", false);
	test.assertNotNull(site);
    var container = site.createContainer("test.permissions");
    test.assertNotNull(container);
    
    // check the current permissions
    var setPerms = container.getPermissions();
    test.assertNotNull(setPerms);
    var bManagers = false;
    for (index in setPerms)
    {
    	if (setPerms[index] == "ALLOWED;GROUP_site_siteShortNameToo_SiteManager;SiteManager")
    	{
    		bManagers = true;
    	}
    }
    if (bManagers == false)
    {
       test.fail("Managers where not assigned to the site group successfully");
    }
    
    // allow all members collaborate
    site.allowAllMembersCollaborate(container);
    setPerms = container.getPermissions();
    test.assertNotNull(setPerms);
    bManagers = false;
    bContributor = false;
    for (index in setPerms)
    {
    	if (setPerms[index] == "ALLOWED;GROUP_site_siteShortNameToo_SiteManager;SiteManager")
    	{
    		bManagers = true;
    	}
    	if (setPerms[index] == "ALLOWED;GROUP_site_siteShortNameToo;SiteCollaborator")
    	{
    		bContributor = true;
    	}
    }
    if (bManagers == false || bContributor == false)
    {
       test.fail("Allow all members contribute failed");
    }    
    
    // deny all
    site.denyAllAccess(container);
    setPerms = container.getPermissions();
    test.assertNotNull(setPerms);
    bManagers = false;
    for (index in setPerms)
    {
    	if (setPerms[index] == "ALLOWED;GROUP_site_siteShortNameToo_SiteManager;SiteManager")
    	{
    		bManagers = true;
    	}
    }
    if (bManagers == false)
    {
       test.fail("Deny all access failed.");
    } 
    
    // reset permissions    
    site.resetAllPermissions(container);    
}

function testRolesAndGroups()
{
   var roles = siteService.listSiteRoles();
   test.assertNotNull(roles);
   test.assertFalse(roles.length == 0);
   
   var site = siteService.createSite("sitePreset", "sn", "siteTitle", "siteDescription", false);
   var siteGroup = site.siteGroup;
   test.assertNotNull(siteGroup);
   test.assertEquals("GROUP_site_sn", siteGroup);
   
   var groups = site.sitePermissionGroups;
   test.assertNotNull(groups);
   test.assertEquals("GROUP_site_sn_SiteManager", groups.SiteManager);
   test.assertEquals("GROUP_site_sn_SiteConsumer", groups.SiteConsumer);
   test.assertEquals("GROUP_site_sn_SiteCollaborator", groups.SiteCollaborator);
   
}

// Execute test's
testCRUD();
testListSites();
testMembership();
testContainer();
testPermissions();
testRolesAndGroups();