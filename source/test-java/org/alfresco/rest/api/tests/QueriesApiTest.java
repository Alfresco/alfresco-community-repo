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
import org.alfresco.rest.api.Queries;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue( Map<K, V> map )
    {
        List<Map.Entry<K, V>> list =
                new LinkedList<>( map.entrySet() );
        Collections.sort( list, new Comparator<Map.Entry<K, V>>()
        {
            public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
            {
                return (o1.getValue()).compareTo( o2.getValue() );
            }
        } );

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list)
        {
            result.put( entry.getKey(), entry.getValue() );
        }
        return result;
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
        int myDocCount = 5;
        List<String> myDocIds = new ArrayList<>(myDocCount);

        int sharedDocCount = 3;
        List<String> sharedDocIds = new ArrayList<>(sharedDocCount);

        int totalCount = myDocCount + sharedDocCount;

        String testTerm = "abc123";

        try
        {
            // As user 1 ...

            Paging paging = getPaging(0, 100);

            Map<String, String> params = new HashMap<>(1);
            params.put(Queries.PARAM_TERM, testTerm);

            // Try to get nodes with search term 'abc123' - assume clean repo (ie. none to start with)
            HttpResponse response = getAll(URL_QUERIES_LSN, user1, paging, params, 200);
            List<Node> nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(0, nodes.size());

            String myFolderNodeId = getMyNodeId(user1);
            String sharedFolderNodeId = getSharedNodeId(user1);

            String name = "name";
            String title = "title";
            String descrip = "descrip";

            Map<String,String> idNameMap = new HashMap<>();

            int nameIdx = myDocCount;
            for (int i = 1; i <= myDocCount; i++)
            {
                // create doc - in "My" folder
                String contentText = "My " + testTerm + " test document " + user1 + " document " + i;

                String num = String.format("%05d", nameIdx);
                String docName = name+num+name;

                Map<String,String> docProps = new HashMap<>(2);
                docProps.put("cm:title", title+num+title);
                docProps.put("cm:description", descrip+num+descrip);

                Document doc = createTextFile(user1, myFolderNodeId, docName, contentText, "UTF-8", docProps);

                myDocIds.add(doc.getId());
                idNameMap.put(doc.getId(), docName);

                nameIdx--;
            }

            nameIdx = sharedDocCount;
            for (int i = 1; i <= sharedDocCount; i++)
            {
                // create doc - in "Shared" folder
                String contentText = "Shared " + testTerm + " test document";

                String num = String.format("%05d", nameIdx);
                String docName = name+num+name;

                Map<String,String> docProps = new HashMap<>(2);
                docProps.put("cm:title", title+num+title);
                docProps.put("cm:description", descrip+num+descrip);

                Document doc = createTextFile(user1, sharedFolderNodeId, docName, contentText, "UTF-8", docProps);

                sharedDocIds.add(doc.getId());
                idNameMap.put(doc.getId(), docName);

                nameIdx--;
            }

            List<String> idsSortedByNameAsc = new ArrayList<>(sortByValue(idNameMap).keySet());

            List<String> idsSortedByNameDescCreatedAtAsc = new ArrayList<>(totalCount);
            for (int i = 0; i < totalCount; i++)
            {
                if (i < myDocCount)
                {
                    idsSortedByNameDescCreatedAtAsc.add(myDocIds.get(i));
                }
                if (i < sharedDocCount)
                {
                    idsSortedByNameDescCreatedAtAsc.add(sharedDocIds.get(i));
                }
            }

            List<String> allIds = new ArrayList<>(totalCount);
            allIds.addAll(myDocIds);
            allIds.addAll(sharedDocIds);

            //
            // find nodes
            //

            // Search hits based on FTS (content)
            params = new HashMap<>(1);
            params.put(Queries.PARAM_TERM, testTerm);
            response = getAll(URL_QUERIES_LSN, user1, paging, params, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            checkNodeIds(nodes, allIds, null);

            // Search hits based on FTS (content) - with a root node (for path-based / in-tree search) - here "Shared" folder
            params = new HashMap<>(2);
            params.put(Queries.PARAM_TERM, testTerm);
            params.put(Queries.PARAM_ROOT_NODE_ID, sharedFolderNodeId);
            response = getAll(URL_QUERIES_LSN, user1, paging, params, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            checkNodeIds(nodes, sharedDocIds, null);

            // Search hits based on FTS (content) - with root node (for path-based / in-tree search) - here user's home folder ("My")
            params = new HashMap<>(2);
            params.put(Queries.PARAM_TERM, testTerm);
            params.put(Queries.PARAM_ROOT_NODE_ID, myFolderNodeId);
            response = getAll(URL_QUERIES_LSN, user1, paging, params, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            checkNodeIds(nodes, myDocIds, null);

            // Search hits based on cm:name
            String term = name+String.format("%05d", 1)+name;
            params = new HashMap<>(1);
            params.put(Queries.PARAM_TERM, "\""+term+"\"");
            response = getAll(URL_QUERIES_LSN, user1, paging, params, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(2, nodes.size());
            assertEquals(term, nodes.get(0).getName());
            assertEquals(term, nodes.get(1).getName());

            // Search hits based on cm:title
            term = title+String.format("%05d", 2)+title;
            params = new HashMap<>(1);
            params.put(Queries.PARAM_TERM, "\""+term+"\"");
            response = getAll(URL_QUERIES_LSN, user1, paging, params, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(2, nodes.size());
            assertEquals(term, nodes.get(0).getProperties().get("cm:title"));
            assertEquals(term, nodes.get(1).getProperties().get("cm:title"));

            // Search hits based on cm:description
            term = descrip+String.format("%05d", 3)+descrip;
            params = new HashMap<>(1);
            params.put(Queries.PARAM_TERM, "\""+term+"\"");
            response = getAll(URL_QUERIES_LSN, user1, paging, params, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(2, nodes.size());
            assertEquals(term, nodes.get(0).getProperties().get("cm:description"));
            assertEquals(term, nodes.get(1).getProperties().get("cm:description"));

            // test sort order

            // default sort order (modifiedAt desc)
            params = new HashMap<>(1);
            params.put(Queries.PARAM_TERM, testTerm);
            response = getAll(URL_QUERIES_LSN, user1, paging, params, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            checkNodeIds(nodes, allIds, false);

            // sort order - modifiedAt asc
            params = new HashMap<>(1);
            params.put(Queries.PARAM_TERM, testTerm);
            params.put("orderBy", "modifiedAt asc");
            response = getAll(URL_QUERIES_LSN, user1, paging, params, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            checkNodeIds(nodes, allIds, false);

            // sort order - modifiedAt desc
            params = new HashMap<>(2);
            params.put(Queries.PARAM_TERM, testTerm);
            params.put("orderBy", "modifiedAt desc");
            response = getAll(URL_QUERIES_LSN, user1, paging, params, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            checkNodeIds(nodes, allIds, true);

            // sort order - createdAt asc
            params = new HashMap<>(2);
            params.put(Queries.PARAM_TERM, testTerm);
            params.put("orderBy", "createdAt asc");
            response = getAll(URL_QUERIES_LSN, user1, paging, params, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            checkNodeIds(nodes, allIds, true);

            // sort order - createdAt desc
            params = new HashMap<>(2);
            params.put(Queries.PARAM_TERM, testTerm);
            params.put("orderBy", "createdAt desc");
            response = getAll(URL_QUERIES_LSN, user1, paging, params, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            checkNodeIds(nodes, allIds, false);

            // sort order - name asc
            params = new HashMap<>(2);
            params.put(Queries.PARAM_TERM, testTerm);
            params.put("orderBy", "name asc");
            response = getAll(URL_QUERIES_LSN, user1, paging, params, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            checkNodeIds(nodes, idsSortedByNameAsc, true);

            // sort order - name desc
            params = new HashMap<>(2);
            params.put(Queries.PARAM_TERM, testTerm);
            params.put("orderBy", "name desc");
            response = getAll(URL_QUERIES_LSN, user1, paging, params, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            checkNodeIds(nodes, idsSortedByNameAsc, false);

            // sort order - name desc, createdAt asc
            params = new HashMap<>(2);
            params.put(Queries.PARAM_TERM, testTerm);
            params.put("orderBy", "name desc, createdAt asc");
            response = getAll(URL_QUERIES_LSN, user1, paging, params, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            checkNodeIds(nodes, idsSortedByNameDescCreatedAtAsc, false);

            // sort order - name asc, createdAt asc
            params = new HashMap<>(2);
            params.put(Queries.PARAM_TERM, testTerm);
            params.put("orderBy", "name asc, createdAt desc");
            response = getAll(URL_QUERIES_LSN, user1, paging, params, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            checkNodeIds(nodes, idsSortedByNameDescCreatedAtAsc, true);

            // basic paging test

            paging = getPaging(0, 100);

            params = new HashMap<>(1);
            params.put(Queries.PARAM_TERM, testTerm);
            response = getAll(URL_QUERIES_LSN, user1, paging, params, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            checkNodeIds(nodes, allIds, false);

            paging = getPaging(0, myDocCount);
            params = new HashMap<>(1);
            params.put(Queries.PARAM_TERM, testTerm);
            response = getAll(URL_QUERIES_LSN, user1, paging, params, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            checkNodeIds(nodes, myDocIds, false);

            paging = getPaging(myDocCount, sharedDocCount);
            params = new HashMap<>(1);
            params.put(Queries.PARAM_TERM, testTerm);
            response = getAll(URL_QUERIES_LSN, user1, paging, params, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            checkNodeIds(nodes, sharedDocIds, false);

            // TODO sanity check modifiedAt (for now modifiedAt=createdAt)
            // TODO sanity check tag search
            // TODO sanity check nodeType query param

            // -ve test - no params (ie. no term)
            getAll(URL_QUERIES_LSN, user1, paging, null, 400);

            // -ve test - no term
            params = new HashMap<>(1);
            params.put(Queries.PARAM_ROOT_NODE_ID, myFolderNodeId);
            getAll(URL_QUERIES_LSN, user1, paging, params, 400);

            // -ve test - term too short
            params = new HashMap<>(1);
            params.put(Queries.PARAM_TERM, "ab");
            getAll(URL_QUERIES_LSN, user1, paging, params, 400);

            // -ve test - term is still too short
            params = new HashMap<>(1);
            params.put(Queries.PARAM_TERM, "  \"a b *\"  ");
            getAll(URL_QUERIES_LSN, user1, paging, params, 400);

            // -ve test - invalid sort field
            params = new HashMap<>(2);
            params.put(Queries.PARAM_TERM, testTerm);
            params.put("orderBy", "invalid asc");
            getAll(URL_QUERIES_LSN, user1, paging, params, 400);

            // -ve test - unauthenticated - belts-and-braces ;-)
            getAll(URL_QUERIES_LSN, null, paging, params, 401);
        }
        finally
        {
            // some cleanup
            for (String docId : myDocIds)
            {
                delete(URL_NODES, user1, docId, 204);
            }

            for (String docId : sharedDocIds)
            {
                delete(URL_NODES, user1, docId, 204);
            }
        }
    }

    private void checkNodeIds(List<Node> nodes, List<String> nodeIds, Boolean asc)
    {
        assertEquals(nodeIds.size(), nodes.size());

        if (asc == null)
        {
            // ignore order
            for (Node node : nodes)
            {
                assertTrue(nodeIds.contains(node.getId()));
            }
        }
        else if (asc)
        {
            int i = 0;
            for (Node node : nodes)
            {
                nodeIds.get(i).equals(node.getId());
                i++;
            }
        }
        else
        {
            int i = nodeIds.size() - 1;
            for (Node node : nodes)
            {
                nodeIds.get(i).equals(node.getId());
                i--;
            }
        }

    }

    @Override
    public String getScope()
    {
        return "public";
    }
}
