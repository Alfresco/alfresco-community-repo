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
package org.alfresco.repo.web.scripts.site;

import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.util.GUID;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Unit test to test site Web Script API
 * 
 * @author Roy Wetherall
 */
public class TestSiteService extends BaseWebScriptTest
{    
    private static final String URL_SITES = "/api/sites";
    
    public void testCreateSite() throws Exception
    {
        String shortName  = GUID.generate();
        
        // == Create a new site ==
        
        JSONObject site = new JSONObject();
        site.put("sitePreset", "myPreset");
        site.put("shortName", shortName);
        site.put("title", "myTitle");
        site.put("description", "myDescription");
        site.put("isPublic", true);        
        
        MockHttpServletResponse response = postRequest(URL_SITES, 200, site.toString(), "application/json");
        
        JSONObject result = new JSONObject(response.getContentAsString());
        
        assertEquals("myPreset", result.get("sitePreset"));
        assertEquals(shortName, result.get("shortName"));
        assertEquals("myTitle", result.get("title"));
        assertEquals("myDescription", result.get("description"));
        assertTrue(result.getBoolean("isPublic"));
        
        // == Create a site with a duplicate shortName ==
        
        response = postRequest(URL_SITES, 500, site.toString(), "application/json");        
        result = new JSONObject(response.getContentAsString());
    }
    
    public void testGetSites() throws Exception
    {
        // == Test basic GET with no filters ==
        
        MockHttpServletResponse response = getRequest(URL_SITES, 200);        
        JSONArray result = new JSONArray(response.getContentAsString());
        
        // TODO formalise this test once i can be sure that i know what's already in the site store 
        //      ie: .. i need to clean up after myself in this test 
        
        System.out.println(response.getContentAsString());
    }

}
