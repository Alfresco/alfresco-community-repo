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

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.rest.api.model.ActionDefinition;
import org.alfresco.rest.api.tests.client.PublicApiClient;
import org.alfresco.rest.api.tests.client.PublicApiClient.ListResponse;
import org.alfresco.rest.api.tests.client.PublicApiException;
import org.alfresco.rest.api.tests.client.RequestContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.EmptyStackException;
import java.util.Iterator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class TestActions extends AbstractBaseApiTest
{
    private PublicApiClient.Actions actions;
    private RepoService.TestNetwork account1;
    private Iterator<RepoService.TestNetwork> accountsIt;
    private Iterator<String> account1PersonIt;
    
    @Before
    public void setUp() throws Exception
    {
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
    public void canGetActionDefinitionsForNode() throws PublicApiException
    {
        final String person1 = account1PersonIt.next();
        publicApiClient.setRequestContext(new RequestContext(account1.getId(), person1));
        
        ListResponse<ActionDefinition> actionDefs = actions.getActionDefinitionsForNode("-my-", null);
        assertNotNull("Action definition list should not be null", actionDefs);
        assertFalse("Action definition list should not be empty", actionDefs.getList().isEmpty());
    }
}
