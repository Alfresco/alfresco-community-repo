/*
 * Copyright (C) 2005-2016 Alfresco Software Limited.
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
package org.alfresco.rest.api.tests;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.rest.api.People;
import org.alfresco.rest.api.Queries;
import org.alfresco.rest.api.QuickShareLinks;
import org.alfresco.rest.api.impl.QuickShareLinksImpl;
import org.alfresco.rest.api.model.QuickShareLink;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient.Paging;
import org.alfresco.rest.api.tests.client.data.Document;
import org.alfresco.rest.api.tests.client.data.Node;
import org.alfresco.rest.api.tests.util.RestApiUtil;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.alfresco.rest.api.tests.util.RestApiUtil.toJsonAsStringNonNull;
import static org.junit.Assert.*;

/**
 * API tests for:
 * <ul>
 * <li> {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/queries} </li>
 * </ul>
 *
 * @author janv
 */
public class QueriesApiTest extends AbstractBaseApiTest
{
    private static final String URL_QUERIES_LSN = "queries/live-search-nodes";

    private String user1;
    private String user2;
    private List<String> users = new ArrayList<>();

    protected MutableAuthenticationService authenticationService;
    protected PersonService personService;

    private final String RUNID = System.currentTimeMillis()+"";

    @Before
    public void setup() throws Exception
    {
        authenticationService = applicationContext.getBean("authenticationService", MutableAuthenticationService.class);
        personService = applicationContext.getBean("personService", PersonService.class);

        // note: createUser currently relies on repoService
        user1 = createUser("user1-" + RUNID);
        user2 = createUser("user2-" + RUNID);

        // We just need to clean the on-premise-users,
        // so the tests for the specific network would work.
        users.add(user1);
        users.add(user2);
    }

    @After
    public void tearDown() throws Exception
    {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        for (final String user : users)
        {
            transactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    if (personService.personExists(user))
                    {
                        authenticationService.deleteAuthentication(user);
                        personService.deletePerson(user);
                    }
                    return null;
                }
            });
        }
        users.clear();
        AuthenticationUtil.clearCurrentSecurityContext();
    }

    /**
     * Tests api for nodes live search
     *
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/queries/live-search-nodes}
     */
    @Test
    public void testLiveSearchNodes() throws Exception
    {
        String d1Id = null;
        String d2Id = null;

        try
        {
            // As user 1 ...

            Paging paging = getPaging(0, 100);

            Map<String, String> params = new HashMap<>(1);
            params.put(Queries.PARAM_TERM, "abc123");

            // Try to get nodes with search term 'abc123' - assume clean repo (ie. none to start with)
            HttpResponse response = getAll(URL_QUERIES_LSN, user1, paging, params, 200);
            List<Node> nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(0, nodes.size());

            // create doc d1 - in "My" folder
            String myFolderNodeId = getMyNodeId(user1);
            String content1Text = "The abc123 test document";
            String docName1 = "content" + RUNID + "_1.txt";
            Document doc1 = createTextFile(user1, myFolderNodeId, docName1, content1Text);
            d1Id = doc1.getId();

            // create doc d2 - in "Shared" folder
            String sharedFolderNodeId = getSharedNodeId(user1);
            String content2Text = "Another abc123 test document";
            String docName2 = "content" + RUNID + "_2.txt";
            Document doc2 = createTextFile(user1, sharedFolderNodeId, docName2, content2Text);
            d2Id = doc2.getId();

            //
            // find nodes
            //

            // term only (no root node)
            params = new HashMap<>(1);
            params.put(Queries.PARAM_TERM, "abc123");
            response = getAll(URL_QUERIES_LSN, user1, paging, params, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(2, nodes.size());
            assertEquals(d2Id, nodes.get(0).getId());
            assertEquals(d1Id, nodes.get(1).getId());

            // term with root node (for path-based / in-tree search)

            params = new HashMap<>(2);
            params.put(Queries.PARAM_TERM, "abc123");
            params.put(Queries.PARAM_ROOT_NODE_ID, sharedFolderNodeId);
            response = getAll(URL_QUERIES_LSN, user1, paging, params, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(1, nodes.size());
            assertEquals(d2Id, nodes.get(0).getId());

            params = new HashMap<>(2);
            params.put(Queries.PARAM_TERM, "abc123");
            params.put(Queries.PARAM_ROOT_NODE_ID, myFolderNodeId);
            response = getAll(URL_QUERIES_LSN, user1, paging, params, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(1, nodes.size());
            assertEquals(d1Id, nodes.get(0).getId());

            // -ve test - no params (ie. no term)
            getAll(URL_QUERIES_LSN, user1, paging, null, 400);

            // -ve test - no term
            params = new HashMap<>(1);
            params.put(Queries.PARAM_ROOT_NODE_ID, myFolderNodeId);
            getAll(URL_QUERIES_LSN, user1, paging, params, 400);

            // -ve test - unauthenticated - belts-and-braces ;-)
            getAll(URL_QUERIES_LSN, null, paging, params, 401);
        }
        finally
        {
            // some cleanup
            if (d1Id != null)
            {
                delete(URL_NODES, user1, d1Id, 204);
            }

            if (d2Id != null)
            {
                delete(URL_NODES, user1, d2Id, 204);
            }

        }
    }

    @Override
    public String getScope()
    {
        return "public";
    }
}
