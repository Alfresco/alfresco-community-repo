/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.preference;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.ClasspathScriptLocation;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.ScriptLocation;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.BaseAlfrescoSpringTest;
import org.alfresco.util.TestWithUserUtils;

/**
 * Thumbnail service implementation unit test
 * 
 * @author Roy Wetherall
 */
public class PreferenceServiceImplTest extends BaseAlfrescoSpringTest 
{
    private static final String USER_ONE = "userOne";
    private static final String USER_BAD = "userBad";
    
    private ScriptService scriptService;
    private NodeService nodeService;
    private AuthenticationComponent authenticationComponent;
    private PreferenceService preferenceService;
    private PersonService personService;
    private ContentService contentService;
    
    /**
     * Called during the transaction setup
     */
    protected void onSetUpInTransaction() throws Exception
    {
        super.onSetUpInTransaction();
        
        // Get the required services
        this.scriptService = (ScriptService)this.applicationContext.getBean("ScriptService");
        this.nodeService = (NodeService)this.applicationContext.getBean("NodeService");
        this.authenticationComponent = (AuthenticationComponent)this.applicationContext.getBean("authenticationComponent");
        this.preferenceService = (PreferenceService)this.applicationContext.getBean("PreferenceService");
        this.personService = (PersonService)this.applicationContext.getBean("PersonService");
        this.contentService = (ContentService)this.applicationContext.getBean("ContentService");
        
        // Do the test's as userOne        
        TestWithUserUtils.authenticateUser(USER_ONE, "PWD", this.authenticationService, this.authenticationComponent);
    }
	
    public void testPreferences() throws Exception
    {
        //assertEquals(USER_ONE, AuthenticationUtil.getCurrentUserName());
        
        // Try and get preferences before they have been set
        Map<String, Serializable> prefs = this.preferenceService.getPreferences(USER_ONE);
        assertNotNull(prefs);
        assertEquals(0, prefs.size());
        
        //assertEquals(USER_ONE, AuthenticationUtil.getCurrentUserName());
        
        // Lets set some preferences for the user
        prefs = new HashMap<String, Serializable>(5);
        prefs.put("alfresco.one.alpha", "string");
        prefs.put("alfresco.one.beta", 100);
        prefs.put("alfresco.two.alpha", 3.142);
        prefs.put("alfresco.two.beta", this.rootNodeRef);
        prefs.put("alfresco.two.gamma", new Date());
        prefs.put("atTheRoot", "thisIsAtTheRoot");
        this.preferenceService.setPreferences(USER_ONE, prefs);
        
        //assertEquals(USER_ONE, AuthenticationUtil.getCurrentUserName());
        
        NodeRef personNodeRef = this.personService.getPerson(USER_ONE);
        ContentReader reader = this.contentService.getReader(personNodeRef, ContentModel.PROP_PREFERENCE_VALUES);
        System.out.println("JSON: " + reader.getContentString());
        
        // Try and get all the preferences
        prefs = this.preferenceService.getPreferences(USER_ONE, null);
        assertNotNull(prefs);
        assertEquals(6, prefs.size());
        
        // Try and get some of the preferences
        prefs =  this.preferenceService.getPreferences(USER_ONE, "alfresco.two");
        assertNotNull(prefs);
        assertEquals(3, prefs.size());
        
        //assertEquals(USER_ONE, AuthenticationUtil.getCurrentUserName());
        
        // Clear some of the preferences 
        this.preferenceService.clearPreferences(USER_ONE, "alfresco.two");
        prefs = this.preferenceService.getPreferences(USER_ONE, null);
        assertNotNull(prefs);
        assertEquals(3, prefs.size());
        
        //assertEquals(USER_ONE, AuthenticationUtil.getCurrentUserName());
        
        // Clear all the preferences
        this.preferenceService.clearPreferences(USER_ONE);
        prefs = this.preferenceService.getPreferences(USER_ONE);
        assertNotNull(prefs);
        assertEquals(0, prefs.size());
        
        //assertEquals(USER_ONE, AuthenticationUtil.getCurrentUserName());
        
    }
    
    public void xtestBadUser()
    {
        assertEquals(USER_ONE, authenticationComponent.getCurrentUserName());
        
        try
        {
            // Lets set some preferences for the user
            Map<String, Serializable> prefs = new HashMap<String, Serializable>(5);
            prefs.put("alfresco.one.alpha", "string");
            prefs.put("alfresco.one.beta", 100);
            prefs.put("alfresco.two.alpha", 3.142);
            prefs.put("alfresco.two.beta", this.rootNodeRef);
            prefs.put("alfresco.two.gamma", new Date());
            prefs.put("atTheRoot", "thisIsAtTheRoot");
            this.preferenceService.setPreferences(USER_BAD, prefs);
            
            fail("This should have raised an exception since we are trying to update preferences that are not our own!");
        }
        catch (Exception exception)
        {
            // this is OK :)
        }
        
    }
    
    // == Test the JavaScript API ==
    
    public void testJSAPI() throws Exception
    {
        //assertEquals(USER_ONE, authenticationComponent.getCurrentUserName());
        
        ScriptLocation location = new ClasspathScriptLocation("org/alfresco/repo/preference/script/test_preferenceService.js");
        this.scriptService.executeScript(location, new HashMap<String, Object>(0));
    }
}
