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

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.jscript.ClasspathScriptLocation;
import org.alfresco.service.cmr.repository.ScriptLocation;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.util.BaseAlfrescoSpringTest;

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
    
    private SiteService siteService;
    
    private ScriptService scriptService;
    
    /**
     * Called during the transaction setup
     */
    protected void onSetUpInTransaction() throws Exception
    {
        super.onSetUpInTransaction();
        
        // Get the required services
        this.siteService = (SiteService)this.applicationContext.getBean("siteService");
        this.scriptService = (ScriptService)this.applicationContext.getBean("ScriptService");
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
        
    // == Test the JavaScript API ==
    
    public void testJSAPI() throws Exception
    {
        ScriptLocation location = new ClasspathScriptLocation("org/alfresco/repo/site/script/test_siteService.js");
        this.scriptService.executeScript(location, new HashMap<String, Object>(0));
    }

}
