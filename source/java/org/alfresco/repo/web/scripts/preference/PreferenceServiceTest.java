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
package org.alfresco.repo.web.scripts.preference;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.PropertyMap;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.DeleteRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Unit test to test preference Web Script API
 * 
 * @author Roy Wetherall
 */
public class PreferenceServiceTest extends BaseWebScriptTest
{    
    private AuthenticationService authenticationService;
    private AuthenticationComponent authenticationComponent;
    private PersonService personService;
    
    private static final String USER_ONE = "PreferenceTestOne" + System.currentTimeMillis();
    private static final String USER_BAD = "PreferenceTestBad" + System.currentTimeMillis();
    
    private static final String URL = "/api/people/" + USER_ONE + "/preferences";;
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        this.authenticationService = (AuthenticationService)getServer().getApplicationContext().getBean("AuthenticationService");
        this.authenticationComponent = (AuthenticationComponent)getServer().getApplicationContext().getBean("authenticationComponent");
        this.personService = (PersonService)getServer().getApplicationContext().getBean("PersonService");
        
        this.authenticationComponent.setSystemUserAsCurrentUser();
        
        // Create users
        createUser(USER_ONE);
        createUser(USER_BAD);
        
        // Do tests as user one
        this.authenticationComponent.setCurrentUser(USER_ONE);
    }
    
    private void createUser(String userName)
    {
        if (this.authenticationService.authenticationExists(userName) == false)
        {
            this.authenticationService.createAuthentication(userName, "PWD".toCharArray());
            
            PropertyMap ppOne = new PropertyMap(4);
            ppOne.put(ContentModel.PROP_USERNAME, userName);
            ppOne.put(ContentModel.PROP_FIRSTNAME, "firstName");
            ppOne.put(ContentModel.PROP_LASTNAME, "lastName");
            ppOne.put(ContentModel.PROP_EMAIL, "email@email.com");
            ppOne.put(ContentModel.PROP_JOBTITLE, "jobTitle");
            
            this.personService.createPerson(ppOne);
        }        
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        
    }
    
    public void testPreferences() throws Exception
    {
        // Get the preferences before they have been set
        
        Response resp = sendRequest(new GetRequest(URL), 200);
        JSONObject jsonResult = new JSONObject(resp.getContentAsString());
        
        assertNotNull(jsonResult);
        assertFalse(jsonResult.keys().hasNext());        
        
        // Set some preferences
        
        JSONObject jsonObject = getPreferenceObj();
        jsonObject.put("comp1", getPreferenceObj());
        
        resp = sendRequest(new PostRequest(URL, jsonObject.toString(), "application/json"), 200);
        assertEquals(0, resp.getContentLength());
        
        // Get the preferences
        
        resp = sendRequest(new GetRequest(URL), 200);
        jsonResult = new JSONObject(resp.getContentAsString());
        assertNotNull(jsonResult);
        assertTrue(jsonResult.keys().hasNext());
        
        checkJSONObject(jsonResult);
        checkJSONObject(jsonResult.getJSONObject("comp1"));
        
        // Update some of the preferences
        
        jsonObject.put("stringValue", "updated");
        jsonObject.put("comp2", getPreferenceObj());
        
        resp = sendRequest(new PostRequest(URL, jsonObject.toString(), "application/json"), 200);
        assertEquals(0, resp.getContentLength());
        
        // Get the preferences
        
        resp = sendRequest(new GetRequest(URL), 200);
        jsonResult = new JSONObject(resp.getContentAsString());
        assertNotNull(jsonResult);
        assertTrue(jsonResult.keys().hasNext());
        
        jsonObject.put("stringValue", "updated");
        jsonObject.put("numberValue", 10);
        jsonObject.put("numberValue2", 3.142);
        checkJSONObject(jsonResult.getJSONObject("comp1"));
        checkJSONObject(jsonResult.getJSONObject("comp2"));
        
        // Filter the preferences retrieved
        
        resp = sendRequest(new GetRequest(URL + "?pf=comp2"), 200);
        jsonResult = new JSONObject(resp.getContentAsString());
        assertNotNull(jsonResult);
        assertTrue(jsonResult.keys().hasNext());
        
        checkJSONObject(jsonResult.getJSONObject("comp2"));
        assertFalse(jsonResult.has("comp1"));
        assertFalse(jsonResult.has("stringValue"));
        
        // Clear some of the preferences
        sendRequest(new DeleteRequest(URL + "?pf=comp1"), 200);
        
        resp = sendRequest(new GetRequest(URL), 200);
        jsonResult = new JSONObject(resp.getContentAsString());
        assertNotNull(jsonResult);
        assertTrue(jsonResult.keys().hasNext());
        
        checkJSONObject(jsonResult.getJSONObject("comp2"));
        assertFalse(jsonResult.has("comp1")); 
        
        // Clear all the preferences
        sendRequest(new DeleteRequest(URL), 200);
        
        resp = sendRequest(new GetRequest(URL), 200);
        jsonResult = new JSONObject(resp.getContentAsString());
        assertNotNull(jsonResult);
        assertFalse(jsonResult.keys().hasNext());

        // Test trying to update another user's permissions
        sendRequest(new PostRequest("/api/people/" + USER_BAD + "/preferences", jsonObject.toString(), "application/json"), 500);
        
        // Test error conditions
        sendRequest(new GetRequest("/api/people/noExistUser/preferences"), 404);
    }
    
    private JSONObject getPreferenceObj()
        throws JSONException
    {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("stringValue", "value");
        jsonObject.put("numberValue", 10);
        jsonObject.put("numberValue2", 3.142);
        return jsonObject;
    }
    
    private void checkJSONObject(JSONObject jsonObject)
        throws JSONException
    {
        assertEquals("value", jsonObject.get("stringValue"));
        assertEquals(10, jsonObject.get("numberValue"));
        assertEquals(3.142, jsonObject.get("numberValue2"));
    }

}
