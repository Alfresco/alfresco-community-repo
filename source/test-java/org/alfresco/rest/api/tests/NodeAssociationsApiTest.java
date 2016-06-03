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
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.Queries;
import org.alfresco.rest.api.model.AssocTarget;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient;
import org.alfresco.rest.api.tests.client.PublicApiClient.Paging;
import org.alfresco.rest.api.tests.client.RequestContext;
import org.alfresco.rest.api.tests.client.data.ContentInfo;
import org.alfresco.rest.api.tests.client.data.Document;
import org.alfresco.rest.api.tests.client.data.Folder;
import org.alfresco.rest.api.tests.client.data.Node;
import org.alfresco.rest.api.tests.client.data.Tag;
import org.alfresco.rest.api.tests.util.RestApiUtil;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.alfresco.rest.api.tests.util.RestApiUtil.toJsonAsStringNonNull;
import static org.junit.Assert.*;

/**
 * API tests for Node Associations
 *
 * Peer Associations  (source -> target)
 * <ul>
 * <li> {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/{sourceId}/targets</li>
 * <li> {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/{targetId}/sources</li>
 * </ul>
 *
 * TODO - Child Associations (parent -> child)  - primary vs secondary
 *
 * <ul>
 * <li> {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/{parentId}/children</li>
 * <li> {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/{parentId}/secondary-children</li>
 * <li> {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/{childId}/parents</li>
 * </ul>
 *
 * Note: please also refer to NodeApiTests for specific tests for "managing" primary parent/child association.

 * @author janv
 */
public class NodeAssociationsApiTest extends AbstractBaseApiTest
{
    private static final String URL_TARGETS = "targets";
    private static final String URL_SOURCES = "sources";

    private static final String ASPECT_CM_REFERENCING = "cm:referencing";
    private static final String ASSOC_TYPE_CM_REFERENCES = "cm:references";

    private static final String ASPECT_CM_PARTABLE = "cm:partable";
    private static final String ASSOC_TYPE_CM_PARTS = "cm:parts";

    private static final String PARAM_ASSOC_TYPE = "assocType";

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

    protected String getNodeTargetsUrl(String nodeId)
    {
        return URL_NODES + "/" + nodeId + "/" + URL_TARGETS;
    }

    protected String getNodeSourcesUrl(String nodeId)
    {
        return URL_NODES + "/" + nodeId + "/" + URL_SOURCES;
    }

    /**
     * Tests basic api to manage (add, list, remove) peer node associations
     *
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/queries/live-search-nodes}
     */
    @Test
    public void testPeerNodeAssocs() throws Exception
    {
        String myFolderNodeId = getMyNodeId(user1);

        // create folder
        String f1Id = createFolder(user1, myFolderNodeId, "f1").getId();

        // create content node
        Node n = new Node();
        n.setName("o1");
        n.setNodeType(TYPE_CM_CONTENT);
        n.setAspectNames(Arrays.asList(ASPECT_CM_REFERENCING, ASPECT_CM_PARTABLE));
        HttpResponse response = post(getNodeChildrenUrl(f1Id), user1, toJsonAsStringNonNull(n), 201);
        String o1Id = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class).getId();

        // create ano' folder
        String f2Id = createFolder(user1, myFolderNodeId, "f2").getId();

        // create ano' content node
        n = new Node();
        n.setName("o2");
        n.setNodeType(TYPE_CM_CONTENT);
        n.setAspectNames(Arrays.asList(ASPECT_CM_REFERENCING, ASPECT_CM_PARTABLE));
        response = post(getNodeChildrenUrl(f2Id), user1, toJsonAsStringNonNull(n), 201);
        String o2Id = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class).getId();


        try
        {
            // As user 1 ...

            Paging paging = getPaging(0, 100);

            // -ve test - unauthenticated - belts-and-braces ;-)
            getAll(getNodeTargetsUrl(f1Id), null, paging, null, 401);
            getAll(getNodeSourcesUrl(f1Id), null, paging, null, 401);

            // empty lists - before

            response = getAll(getNodeTargetsUrl(o1Id), user1, paging, null, 200);
            List<Node> nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(0, nodes.size());

            response = getAll(getNodeSourcesUrl(o1Id), user1, paging, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(0, nodes.size());

            response = getAll(getNodeTargetsUrl(o2Id), user1, paging, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(0, nodes.size());

            response = getAll(getNodeSourcesUrl(o2Id), user1, paging, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(0, nodes.size());

            // -ve test - unauthenticated - belts-and-braces ;-)
            AssocTarget tgt = new AssocTarget(o2Id, ASSOC_TYPE_CM_REFERENCES);
            post(getNodeTargetsUrl(o1Id), null, toJsonAsStringNonNull(tgt), 401);

            // create two assocs in one direction (from src to tgt)

            tgt = new AssocTarget(o2Id, ASSOC_TYPE_CM_REFERENCES);
            post(getNodeTargetsUrl(o1Id), user1, toJsonAsStringNonNull(tgt), 201);

            tgt = new AssocTarget(o2Id, ASSOC_TYPE_CM_PARTS);
            post(getNodeTargetsUrl(o1Id), user1, toJsonAsStringNonNull(tgt), 201);

            response = getAll(getNodeTargetsUrl(o1Id), user1, paging, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(2, nodes.size());

            response = getAll(getNodeSourcesUrl(o1Id), user1, paging, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(0, nodes.size());

            response = getAll(getNodeTargetsUrl(o2Id), user1, paging, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(0, nodes.size());

            response = getAll(getNodeSourcesUrl(o2Id), user1, paging, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(2, nodes.size());

            // create two assocs in the other direction (from tgt to src)

            tgt = new AssocTarget(o1Id, ASSOC_TYPE_CM_REFERENCES);
            post(getNodeTargetsUrl(o2Id), user1, toJsonAsStringNonNull(tgt), 201);

            tgt = new AssocTarget(o1Id, ASSOC_TYPE_CM_PARTS);
            post(getNodeTargetsUrl(o2Id), user1, toJsonAsStringNonNull(tgt), 201);

            response = getAll(getNodeTargetsUrl(o1Id), user1, paging, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(2, nodes.size());

            response = getAll(getNodeSourcesUrl(o1Id), user1, paging, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(2, nodes.size());

            response = getAll(getNodeTargetsUrl(o2Id), user1, paging, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(2, nodes.size());

            response = getAll(getNodeSourcesUrl(o2Id), user1, paging, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(2, nodes.size());

            // test basic list filter

            Map<String, String> params = new HashMap<>();
            params.put("where", "(assocType='"+ASSOC_TYPE_CM_REFERENCES+"')");

            response = getAll(getNodeTargetsUrl(o1Id), user1, paging, params, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(1, nodes.size());
            assertEquals(o2Id, nodes.get(0).getId());
            assertEquals(ASSOC_TYPE_CM_REFERENCES, nodes.get(0).getAssociation().getAssocType());

            response = getAll(getNodeSourcesUrl(o1Id), user1, paging, params, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(1, nodes.size());
            assertEquals(o2Id, nodes.get(0).getId());
            assertEquals(ASSOC_TYPE_CM_REFERENCES, nodes.get(0).getAssociation().getAssocType());

            params = new HashMap<>();
            params.put("where", "(assocType='"+ASSOC_TYPE_CM_PARTS+"')");

            response = getAll(getNodeTargetsUrl(o2Id), user1, paging, params, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(1, nodes.size());
            assertEquals(o1Id, nodes.get(0).getId());
            assertEquals(ASSOC_TYPE_CM_PARTS, nodes.get(0).getAssociation().getAssocType());

            response = getAll(getNodeSourcesUrl(o2Id), user1, paging, params, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(1, nodes.size());
            assertEquals(o1Id, nodes.get(0).getId());
            assertEquals(ASSOC_TYPE_CM_PARTS, nodes.get(0).getAssociation().getAssocType());


            // -ve test - unauthenticated - belts-and-braces ;-)
            delete(getNodeTargetsUrl(o1Id), null, o2Id, 401);

            // remove assocs - specific type - in one direction
            params = new HashMap<>(2);
            params.put(PARAM_ASSOC_TYPE, ASSOC_TYPE_CM_REFERENCES);
            delete(getNodeTargetsUrl(o1Id), user1, o2Id, params, 204);

            response = getAll(getNodeTargetsUrl(o1Id), user1, paging, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(1, nodes.size());

            response = getAll(getNodeSourcesUrl(o1Id), user1, paging, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(2, nodes.size());

            response = getAll(getNodeTargetsUrl(o2Id), user1, paging, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(2, nodes.size());

            response = getAll(getNodeSourcesUrl(o2Id), user1, paging, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(1, nodes.size());

            params = new HashMap<>(2);
            params.put(PARAM_ASSOC_TYPE, ASSOC_TYPE_CM_PARTS);
            delete(getNodeTargetsUrl(o1Id), user1, o2Id, params, 204);

            response = getAll(getNodeTargetsUrl(o1Id), user1, paging, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(0, nodes.size());

            response = getAll(getNodeSourcesUrl(o1Id), user1, paging, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(2, nodes.size());

            response = getAll(getNodeTargetsUrl(o2Id), user1, paging, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(2, nodes.size());

            response = getAll(getNodeSourcesUrl(o2Id), user1, paging, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(0, nodes.size());

            // remove assocs - both types at once (ie. no assocType param) - in the other direction
            delete(getNodeTargetsUrl(o2Id), user1, o1Id, 204);

            // empty lists - after

            response = getAll(getNodeTargetsUrl(o1Id), user1, paging, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(0, nodes.size());

            response = getAll(getNodeSourcesUrl(o1Id), user1, paging, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(0, nodes.size());

            response = getAll(getNodeTargetsUrl(o2Id), user1, paging, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(0, nodes.size());

            response = getAll(getNodeSourcesUrl(o2Id), user1, paging, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(0, nodes.size());


            // -ve test - model integrity
            tgt = new AssocTarget(f2Id, ASSOC_TYPE_CM_REFERENCES);
            post(getNodeTargetsUrl(o1Id), user1, toJsonAsStringNonNull(tgt), 422);

            // -ve test - duplicate assoc
            tgt = new AssocTarget(o1Id, ASSOC_TYPE_CM_REFERENCES);
            post(getNodeTargetsUrl(o2Id), user1, toJsonAsStringNonNull(tgt), 201);
            post(getNodeTargetsUrl(o2Id), user1, toJsonAsStringNonNull(tgt), 409);
        }
        finally
        {
            // some cleanup
            Map<String, String> params = Collections.singletonMap("permanent", "true");
            delete(URL_NODES, user1, f1Id, params, 204);
            delete(URL_NODES, user1, f2Id, params, 204);
        }
    }

    @Override
    public String getScope()
    {
        return "public";
    }
}
