/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.rest.api.tests;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.rest.api.model.ActionDefinition;
import org.alfresco.rest.api.tests.client.PublicApiClient;
import org.alfresco.rest.api.tests.client.PublicApiClient.ListResponse;
import org.alfresco.rest.api.tests.client.RequestContext;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class TestActions extends AbstractBaseApiTest
{
    private NodeService nodeService;
    private PublicApiClient.Actions actions;
    private RepoService.TestNetwork account1;
    private Iterator<RepoService.TestNetwork> accountsIt;
    private Iterator<String> account1PersonIt;
    private final static Map<String, String> emptyParams = Collections.EMPTY_MAP;

    @Before
    public void setUp() throws Exception
    {
        nodeService = applicationContext.getBean("NodeService", NodeService.class);
        actions = publicApiClient.actions();

        accountsIt = getTestFixture().getNetworksIt();
        account1 = accountsIt.next();
        account1PersonIt = account1.getPersonIds().iterator();
        
        // Capture authentication pre-test, so we can restore it again afterwards.
        AuthenticationUtil.pushAuthentication();
    }

    @After
    public void tearDown()
    {
        // Restore authentication to pre-test state.
        try
        {
            AuthenticationUtil.popAuthentication();
        }
        catch(EmptyStackException e)
        {
            // Nothing to do.
        }
    }
    
    @Override
    public String getScope()
    {
        return "public";
    }
    
    @Test
    public void canGetActionDefinitionsForNode() throws Exception
    {
        final String person1 = account1PersonIt.next();
        publicApiClient.setRequestContext(new RequestContext(account1.getId(), person1));
        
        // Get the actions available on the -my- node-ref alias
        {
            ListResponse<ActionDefinition> actionDefs = actions.getActionDefinitionsForNode("-my-", emptyParams, 200);
            
            assertNotNull("Action definition list should not be null", actionDefs);
            assertFalse("Action definition list should not be empty", actionDefs.getList().isEmpty());
        }

        AuthenticationUtil.setFullyAuthenticatedUser(person1);
        
        // Get the actions available using a specific node ID
        {
            String myNode = getMyNodeId();
            NodeRef validNode = nodeService.createNode(
                    new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, myNode),
                    ContentModel.ASSOC_CONTAINS,
                    QName.createQName("test", "test-node"),
                    ContentModel.TYPE_CONTENT).getChildRef();
            
            ListResponse<ActionDefinition> actionDefs = actions.getActionDefinitionsForNode(validNode.getId(), emptyParams, 200);
            
            assertNotNull("Action definition list should not be null", actionDefs);
            assertFalse("Action definition list should not be empty", actionDefs.getList().isEmpty());
        }
        
        // Non-existent node ID
        {
            NodeRef nodeRef = new NodeRef(
                    StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
                    "750a2867-ecfa-478c-8343-fa0e39d27be3");
            assertFalse("Test pre-requisite: node must not exist", nodeService.exists(nodeRef));
            
            actions.getActionDefinitionsForNode(nodeRef.getId(), emptyParams, 404);
        }
    }
}
