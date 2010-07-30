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
package org.alfresco.repo.web.scripts.replication;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.person.TestPersonManager;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.action.ActionTrackingService;
import org.alfresco.service.cmr.replication.ReplicationService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.GUID;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * Tests for the Replication Webscripts
 * @author Nick Burch
 */
public class ReplicationRestApiTest extends BaseWebScriptTest
{
    private static final String URL_DEFINITION = "api/replication-definition/";
    private static final String URL_DEFINITIONS = "api/replication-definitions";
    private static final String URL_RUNNING_ACTION = "api/running-action/";
    
    private static final String USER_NORMAL = "Normal" + GUID.generate();
    
    private TestPersonManager personManager;
    private ReplicationService replicationService;
    private ActionTrackingService actionTrackingService;
    
    public void testReplicationDefinitionsGet() throws Exception
    {
        Response response;
        
        
        // Not allowed if you're not an admin
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getGuestUserName());
        response = sendRequest(new GetRequest(URL_DEFINITIONS), Status.STATUS_UNAUTHORIZED);
        assertEquals(Status.STATUS_UNAUTHORIZED, response.getStatus());
        
        AuthenticationUtil.setFullyAuthenticatedUser(USER_NORMAL);
        response = sendRequest(new GetRequest(URL_DEFINITIONS), Status.STATUS_UNAUTHORIZED);
        assertEquals(Status.STATUS_UNAUTHORIZED, response.getStatus());
        
       
        // If no definitions exist, you don't get anything back
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        response = sendRequest(new GetRequest(URL_DEFINITIONS), 200);
        assertEquals(Status.STATUS_OK, response.getStatus());
        
        String jsonStr = response.getContentAsString();
        JSONObject json = new JSONObject(jsonStr);
        JSONArray results = json.getJSONArray("data");
        assertNotNull(results);
        assertTrue(results.length() == 0);

        
        // Add a definition, it should show up
        // TODO

        
        // Change the status to running, and re-check
        // TODO
        
       
        // Add a 2nd and 3rd
        // TODO
        
       
        // Cancel one of these
        // TODO
        
    }
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        ApplicationContext appContext = getServer().getApplicationContext();

        replicationService = (ReplicationService)appContext.getBean("ReplicationService");
        actionTrackingService = (ActionTrackingService)appContext.getBean("actionTrackingService");
        
        MutableAuthenticationService authenticationService = (MutableAuthenticationService)appContext.getBean("AuthenticationService");
        PersonService personService = (PersonService)appContext.getBean("PersonService");
        NodeService nodeService = (NodeService)appContext.getBean("NodeService");
        personManager = new TestPersonManager(authenticationService, personService, nodeService);

        personManager.createPerson(USER_NORMAL);
    }
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        personManager.clearPeople();
    }
}
