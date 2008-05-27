/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.site;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.jscript.ClasspathScriptLocation;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.ScriptLocation;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.util.BaseAlfrescoSpringTest;
import org.alfresco.util.TestWithUserUtils;

/**
 * Thumbnail service implementation unit test
 * 
 * @author Roy Wetherall
 */
public class SiteServiceImplTest extends BaseAlfrescoSpringTest 
{
    private static final String TEST_SITE_PRESET = "testSitePreset";
    private static final String TEST_SITE_PRESET_2 = "testSitePreset2";
    private static final String TEST_TITLE = "This is my title";
    private static final String TEST_DESCRIPTION = "This is my description";
    
    private static final String USER_ONE = "UserOne";
    private static final String USER_TWO = "UserTwo";
    private static final String USER_THREE = "UserThree";
    
    private SiteService siteService;    
    private ScriptService scriptService;
    private AuthenticationComponent authenticationComponent;
    
    /**
     * Called during the transaction setup
     */
    protected void onSetUpInTransaction() throws Exception
    {
        super.onSetUpInTransaction();
        
        // Get the required services
        this.siteService = (SiteService)this.applicationContext.getBean("siteService");
        this.scriptService = (ScriptService)this.applicationContext.getBean("ScriptService");
        this.authenticationComponent = (AuthenticationComponent)this.applicationContext.getBean("authenticationComponent");
        
        // Do the test's as userOne
        TestWithUserUtils.authenticateUser(USER_ONE, "PWD", this.authenticationService, this.authenticationComponent);
    }
	
    public void testCreateSite() throws Exception
    {
        // Create a public site
        SiteInfo siteInfo = this.siteService.createSite(TEST_SITE_PRESET, "mySiteTest", TEST_TITLE, TEST_DESCRIPTION, true);
        
        // Check the site
        checkSiteInfo(siteInfo, TEST_SITE_PRESET, "mySiteTest", TEST_TITLE, TEST_DESCRIPTION, true);     
    }
    
    private void checkSiteInfo( SiteInfo siteInfo, String expectedSitePreset, String expectedShortName, String expectedTitle, 
                                String expectedDescription, boolean expectedIsPublic)
    {
        assertNotNull(siteInfo);
        assertEquals(expectedSitePreset, siteInfo.getSitePreset());
        assertEquals(expectedShortName, siteInfo.getShortName());
        assertEquals(expectedTitle, siteInfo.getTitle());
        assertEquals(expectedDescription, siteInfo.getDescription());
        assertEquals(expectedIsPublic, siteInfo.getIsPublic());
    }
    
    public void testListSites() throws Exception
    {
        // TODO
        // - check filters
        // - check private excluded when not owner (or admin)
        
        // Check for no sites
        List<SiteInfo> sites = this.siteService.listSites(null, null);
        assertNotNull(sites);
        assertTrue(sites.isEmpty());
        
        // Create some sites
        this.siteService.createSite(TEST_SITE_PRESET, "mySiteOne", TEST_TITLE, TEST_DESCRIPTION, true);
        this.siteService.createSite(TEST_SITE_PRESET, "mySiteTwo", TEST_TITLE, TEST_DESCRIPTION, false);
        this.siteService.createSite(TEST_SITE_PRESET_2, "mySiteThree", TEST_TITLE, TEST_DESCRIPTION, true);
        this.siteService.createSite(TEST_SITE_PRESET_2, "mySiteFour", TEST_TITLE, TEST_DESCRIPTION, false);
        
        // Get all the sites
        sites = this.siteService.listSites(null, null);
        assertNotNull(sites);
        assertEquals(4, sites.size());
        // Do detailed check of the site info objects
        for (SiteInfo site : sites)
        {
            String shortName = site.getShortName();
            if (shortName.equals("mySiteOne") == true)
            {
                checkSiteInfo(site, TEST_SITE_PRESET, "mySiteOne", TEST_TITLE, TEST_DESCRIPTION, true);
            }
            else if (shortName.equals("mySiteTwo") == true)
            {
                checkSiteInfo(site, TEST_SITE_PRESET, "mySiteTwo", TEST_TITLE, TEST_DESCRIPTION, false);
            }
            else if (shortName.equals("mySiteThree") == true)
            {
                checkSiteInfo(site, TEST_SITE_PRESET_2, "mySiteThree", TEST_TITLE, TEST_DESCRIPTION, true);
            }
            else if (shortName.equals("mySiteFour") == true)
            {
                checkSiteInfo(site, TEST_SITE_PRESET_2, "mySiteFour", TEST_TITLE, TEST_DESCRIPTION, false);                
            }
            else
            {
                fail("The shortname " + shortName + " is not recognised");
            }
        }
        
    }
    
    public void testGetSite()
    {
        // Get a site that isn't there
        SiteInfo siteInfo = this.siteService.getSite("testGetSite");
        assertNull(siteInfo);
        
        // Create a test site
        this.siteService.createSite(TEST_SITE_PRESET, "testGetSite", TEST_TITLE, TEST_DESCRIPTION, true);
        
        // Get the test site
        siteInfo = this.siteService.getSite("testGetSite");
        assertNotNull(siteInfo);
        checkSiteInfo(siteInfo, TEST_SITE_PRESET, "testGetSite", TEST_TITLE, TEST_DESCRIPTION, true); 
    }
    
    public void testUpdateSite()
    {
        SiteInfo siteInfo = new SiteInfo(TEST_SITE_PRESET, "testUpdateSite", "changedTitle", "changedDescription", false);
        
        // update a site that isn't there
        try
        {
            this.siteService.updateSite(siteInfo);
            fail("Shouldn't be able to update a site that does not exist");
        }
        catch (AlfrescoRuntimeException exception)
        {
            // Expected
        }
        
        // Create a test site
        this.siteService.createSite(TEST_SITE_PRESET, "testUpdateSite", TEST_TITLE, TEST_DESCRIPTION, true);
        
        // Update the details of the site
        this.siteService.updateSite(siteInfo);
        siteInfo = this.siteService.getSite("testUpdateSite");
        checkSiteInfo(siteInfo, TEST_SITE_PRESET, "testUpdateSite", "changedTitle", "changedDescription", false); 
        
        // Update the permission again
        siteInfo.setIsPublic(true);
        this.siteService.updateSite(siteInfo);
        checkSiteInfo(siteInfo, TEST_SITE_PRESET, "testUpdateSite", "changedTitle", "changedDescription", true);         
    }
    
    public void testDeleteSite()
    {
        // delete a site that isn't there
        try
        {
            this.siteService.deleteSite("testDeleteSite");
            fail("Shouldn't be able to delete a site that does not exist");
        }
        catch (AlfrescoRuntimeException exception)
        {
            // Expected
        }
        
        // Create a test site
        this.siteService.createSite(TEST_SITE_PRESET, "testUpdateSite", TEST_TITLE, TEST_DESCRIPTION, true);
        assertNotNull(this.siteService.getSite("testUpdateSite"));
        
        // Delete the site
        this.siteService.deleteSite("testUpdateSite");
        assertNull(this.siteService.getSite("testUpdateSite"));
    }
    
    public void testIsPublic()
    {
        // Create a couple of sites as user one
        this.siteService.createSite(TEST_SITE_PRESET, "isPublicTrue", TEST_TITLE, TEST_DESCRIPTION, true);
        this.siteService.createSite(TEST_SITE_PRESET, "isPublicFalse", TEST_TITLE, TEST_DESCRIPTION, false);
        
        // Get the sites as user one
        List<SiteInfo> sites = this.siteService.listSites(null, null);
        assertNotNull(sites);
        assertEquals(2, sites.size());
        
        // Now get the sites as user two
        TestWithUserUtils.authenticateUser(USER_TWO, "PWD", this.authenticationService, this.authenticationComponent);
        sites = this.siteService.listSites(null, null);
        assertNotNull(sites);
        assertEquals(1, sites.size());
        checkSiteInfo(sites.get(0), TEST_SITE_PRESET, "isPublicTrue", TEST_TITLE, TEST_DESCRIPTION, true);
        
        // Make user 2 a member of the site
        TestWithUserUtils.authenticateUser(USER_ONE, "PWD", this.authenticationService, this.authenticationComponent);
        this.siteService.setMembership("isPublicFalse", USER_TWO, SiteModel.SITE_CONSUMER);
        
        // Now get the sites as user two
        TestWithUserUtils.authenticateUser(USER_TWO, "PWD", this.authenticationService, this.authenticationComponent);
        sites = this.siteService.listSites(null, null);
        assertNotNull(sites);
        assertEquals(2, sites.size());
    }
    
    public void testMembership()
    {
        // Create a site as user one
        this.siteService.createSite(TEST_SITE_PRESET, "testMembership", TEST_TITLE, TEST_DESCRIPTION, false);
        
        // Get the members of the site and check that user one is a manager
        Map<String, String> members = this.siteService.listMembers("testMembership", null, null);
        assertNotNull(members);
        assertEquals(1, members.size());
        assertTrue(members.containsKey(USER_ONE));
        assertEquals(SiteModel.SITE_MANAGER, members.get(USER_ONE));
     
        // Add user two as a consumer and user three as a collaborator
        this.siteService.setMembership("testMembership", USER_TWO, SiteModel.SITE_CONSUMER);
        this.siteService.setMembership("testMembership", USER_THREE, SiteModel.SITE_COLLABORATOR);
        
        // Get the members of the site
        members = this.siteService.listMembers("testMembership", null, null);
        assertNotNull(members);
        assertEquals(3, members.size());
        assertTrue(members.containsKey(USER_ONE));
        assertEquals(SiteModel.SITE_MANAGER, members.get(USER_ONE));
        assertTrue(members.containsKey(USER_TWO));
        assertEquals(SiteModel.SITE_CONSUMER, members.get(USER_TWO));
        assertTrue(members.containsKey(USER_THREE));
        assertEquals(SiteModel.SITE_COLLABORATOR, members.get(USER_THREE));
        
        // Change the membership of user two
        this.siteService.setMembership("testMembership", USER_TWO, SiteModel.SITE_COLLABORATOR);
        
        // Check the members of the site
        members = this.siteService.listMembers("testMembership", null, null);
        assertNotNull(members);
        assertEquals(3, members.size());
        assertTrue(members.containsKey(USER_ONE));
        assertEquals(SiteModel.SITE_MANAGER, members.get(USER_ONE));
        assertTrue(members.containsKey(USER_TWO));
        assertEquals(SiteModel.SITE_COLLABORATOR, members.get(USER_TWO));
        assertTrue(members.containsKey(USER_THREE));
        assertEquals(SiteModel.SITE_COLLABORATOR, members.get(USER_THREE));
        
        // Remove user two's membership
        this.siteService.removeMembership("testMembership", USER_TWO);
        
        // Check the members of the site
        members = this.siteService.listMembers("testMembership", null, null);
        assertNotNull(members);
        assertEquals(2, members.size());
        assertTrue(members.containsKey(USER_ONE));
        assertEquals(SiteModel.SITE_MANAGER, members.get(USER_ONE));
        assertTrue(members.containsKey(USER_THREE));
        assertEquals(SiteModel.SITE_COLLABORATOR, members.get(USER_THREE));
        
        // Check that a non-manager and non-member cannot edit the memberships
        TestWithUserUtils.authenticateUser(USER_TWO, "PWD", this.authenticationService, this.authenticationComponent);
        try
        {
            this.siteService.setMembership("testMembership", USER_TWO, SiteModel.SITE_COLLABORATOR);
            fail("A non member shouldnt be able to set memberships");
        }
        catch (AlfrescoRuntimeException e)
        {
            // As expected
        }
        try
        {
            this.siteService.removeMembership("testMembership", USER_THREE);
            fail("A non member shouldnt be able to remove a membership");
        }
        catch (AlfrescoRuntimeException e)
        {
            // As expected            
        }
        TestWithUserUtils.authenticateUser(USER_THREE, "PWD", this.authenticationService, this.authenticationComponent);
        try
        {
            this.siteService.setMembership("testMembership", USER_TWO, SiteModel.SITE_COLLABORATOR);
            fail("A member who isn't a manager shouldnt be able to set memberships");
        }
        catch (AlfrescoRuntimeException e)
        {
            // As expected
        }
        try
        {
            this.siteService.removeMembership("testMembership", USER_THREE);
            fail("A member who isn't a manager shouldnt be able to remove a membership");
        }
        catch (AlfrescoRuntimeException e)
        {
            // As expected            
        }
        
        // TODO .. try and change the permissions of the only site manager
        
        // TODO .. try and remove the only site manager and should get a failure
    }
        
    public void testContainer()
    {
        // Create a couple of sites as user one
        SiteInfo siteInfo = this.siteService.createSite(TEST_SITE_PRESET, "testContainer", TEST_TITLE, TEST_DESCRIPTION, true);

        boolean hasContainer = this.siteService.hasContainer(siteInfo.getShortName(), "folder.component");
        assertFalse(hasContainer);
        NodeRef container1 = this.siteService.getContainer(siteInfo.getShortName(), "folder.component");
        assertNotNull(container1);
        NodeRef container2 = this.siteService.getContainer(siteInfo.getShortName(), "folder.component");
        assertNotNull(container2);
        assertTrue(container1.equals(container2));
        boolean hasContainer2 = this.siteService.hasContainer(siteInfo.getShortName(), "folder.component");
        assertTrue(hasContainer2);
        boolean hasContainer3 = this.siteService.hasContainer(siteInfo.getShortName(), "folder.component2");
        assertFalse(hasContainer3);
        NodeRef container3 = this.siteService.getContainer(siteInfo.getShortName(), "folder.component2");
        assertNotNull(container3);
        assertFalse(container1.equals(container3));
        boolean hasContainer4 = this.siteService.hasContainer(siteInfo.getShortName(), "folder.component2");
        assertTrue(hasContainer4);
    }
    
    
    // == Test the JavaScript API ==
    
    public void testJSAPI() throws Exception
    {
        ScriptLocation location = new ClasspathScriptLocation("org/alfresco/repo/site/script/test_siteService.js");
        this.scriptService.executeScript(location, new HashMap<String, Object>(0));
    }

}
