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
package org.alfresco.repo.web.scripts.preference;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.PropertyMap;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.TestWebScriptServer.DeleteRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * Unit test to test preference Web Script API
 * 
 * @author Roy Wetherall
 */
public class PreferenceServiceTest extends BaseWebScriptTest
{
    private MutableAuthenticationService authenticationService;

    private AuthenticationComponent authenticationComponent;

    private PersonService personService;

    private static final String USER_ONE = "PreferenceTestOne" + System.currentTimeMillis();

    private static final String USER_BAD = "PreferenceTestBad" + System.currentTimeMillis();

    private static final String URL = "/api/people/" + USER_ONE + "/preferences";;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        this.authenticationService = (MutableAuthenticationService) getServer().getApplicationContext().getBean(
                "AuthenticationService");
        this.authenticationComponent = (AuthenticationComponent) getServer().getApplicationContext().getBean(
                "authenticationComponent");
        this.personService = (PersonService) getServer().getApplicationContext().getBean("PersonService");

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
        sendRequest(new PostRequest("/api/people/" + USER_BAD + "/preferences", jsonObject.toString(),
                "application/json"), 401);

        // Test error conditions
        sendRequest(new GetRequest("/api/people/noExistUser/preferences"), 404);
    }

    private JSONObject getPreferenceObj() throws JSONException
    {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("stringValue", "value");
        jsonObject.put("numberValue", 10);
        jsonObject.put("numberValue2", 3.142);
        return jsonObject;
    }

    private void checkJSONObject(JSONObject jsonObject) throws JSONException
    {
        assertEquals("value", jsonObject.get("stringValue"));
        assertEquals(10, jsonObject.get("numberValue"));
        assertEquals(3.142, jsonObject.get("numberValue2"));
    }

}
