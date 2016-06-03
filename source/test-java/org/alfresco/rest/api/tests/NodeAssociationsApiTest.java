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

import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.AssocChild;
import org.alfresco.rest.api.model.AssocTarget;
import org.alfresco.rest.api.nodes.NodesEntityResource;
import org.alfresco.rest.api.tests.client.HttpResponse;
import org.alfresco.rest.api.tests.client.PublicApiClient.Paging;
import org.alfresco.rest.api.tests.client.RequestContext;
import org.alfresco.rest.api.tests.client.data.Association;
import org.alfresco.rest.api.tests.client.data.Node;
import org.alfresco.rest.api.tests.util.JacksonUtil;
import org.alfresco.rest.api.tests.util.RestApiUtil;
import org.alfresco.rest.framework.jacksonextensions.JacksonHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
 * Child Associations (parent -> child) - secondary (*)
 *
 * <ul>
 * <li> {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/{parentId}/children</li>
 * <li> {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/{parentId}/secondary-children</li>
 * <li> {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/{childId}/parents</li>
 * </ul>
 *
 * (*) for primary child assocs, please refer to NodeApiTest - eg. create/delete node (primary child), list children, move, copy etc
 *
 * @author janv
 */
public class NodeAssociationsApiTest extends AbstractBaseApiTest
{
    private static final String PARAM_ASSOC_TYPE = "assocType";

    // peer assocs

    private static final String URL_TARGETS = "targets";
    private static final String URL_SOURCES = "sources";

    private static final String ASPECT_CM_REFERENCING = "cm:referencing";
    private static final String ASSOC_TYPE_CM_REFERENCES = "cm:references";

    private static final String ASPECT_CM_PARTABLE = "cm:partable";
    private static final String ASSOC_TYPE_CM_PARTS = "cm:parts";

    // child assocs

    private static final String URL_SECONDARY_CHILDREN = "secondary-children";
    private static final String URL_PARENTS = "parents";

    private static final String ASSOC_TYPE_CM_CONTAINS = "cm:contains";

    private static final String ASPECT_CM_PREFERENCES = "cm:preferences";
    private static final String ASSOC_TYPE_CM_PREFERENCE_IMAGE = "cm:preferenceImage";


    private String user1;
    private String user2;
    private List<String> users = new ArrayList<>();

    protected MutableAuthenticationService authenticationService;
    protected PermissionService permissionService;
    protected PersonService personService;

    private final String RUNID = System.currentTimeMillis()+"";


    @Before
    public void setup() throws Exception
    {
        authenticationService = applicationContext.getBean("authenticationService", MutableAuthenticationService.class);
        permissionService = applicationContext.getBean("permissionService", PermissionService.class);
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

    protected String getNodeSecondaryChildrenUrl(String nodeId)
    {
        return URL_NODES + "/" + nodeId + "/" + URL_SECONDARY_CHILDREN;
    }

    protected String getNodeParentsUrl(String nodeId)
    {
        return URL_NODES + "/" + nodeId + "/" + URL_PARENTS;
    }

    /**
     * Tests basic api to manage (add, list, remove) node peer associations (ie. source node -> target node)
     *
     * <p>POST:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<sourceNodeId>/targets}
     *
     * <p>DELETE:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<sourceNodeId>/targets/<targetNodeId>}
     *
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<sourceNodeId>/targets}
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<targetNodeId>/sources}
     */
    @Test
    public void testNodePeerAssocs() throws Exception
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

            // create two assocs in one direction (from src to tgt)

            AssocTarget tgt = new AssocTarget(o2Id, ASSOC_TYPE_CM_REFERENCES);
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

            params = new HashMap<>(1);
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


            //
            // -ve tests - add assoc
            //

            {
                // -ve test - unauthenticated - belts-and-braces ;-)
                tgt = new AssocTarget(o2Id, ASSOC_TYPE_CM_REFERENCES);
                post(getNodeTargetsUrl(o1Id), null, toJsonAsStringNonNull(tgt), 401);

                // -ve test - model integrity
                tgt = new AssocTarget(f2Id, ASSOC_TYPE_CM_REFERENCES);
                post(getNodeTargetsUrl(o1Id), user1, toJsonAsStringNonNull(tgt), 422);

                // -ve test - duplicate assoc
                tgt = new AssocTarget(o1Id, ASSOC_TYPE_CM_REFERENCES);
                post(getNodeTargetsUrl(o2Id), user1, toJsonAsStringNonNull(tgt), 201);
                post(getNodeTargetsUrl(o2Id), user1, toJsonAsStringNonNull(tgt), 409);

                tgt = new AssocTarget(o1Id, "cm:unknowntype");
                post(getNodeTargetsUrl(o2Id), user1, toJsonAsStringNonNull(tgt), 400);
            }

            //
            // -ve test - list assocs
            //

            {
                // -ve test - unauthenticated - belts-and-braces ;-)
                getAll(getNodeTargetsUrl(f1Id), null, paging, null, 401);
                getAll(getNodeSourcesUrl(f1Id), null, paging, null, 401);

                getAll(getNodeTargetsUrl(UUID.randomUUID().toString()), user1, paging, null, 404);
                getAll(getNodeSourcesUrl(UUID.randomUUID().toString()), user1, paging, null, 404);

                params = new HashMap<>(1);
                params.put("where", "(assocType='cm:unknownassoctype')");

                getAll(getNodeTargetsUrl(o1Id), user1, paging, params, 400);
                getAll(getNodeSourcesUrl(o1Id), user1, paging, params, 400);

                // TODO paging - in-built sort order ?
            }


            //
            // -ve test - remove assoc(s)
            //

            {
                // -ve test - unauthenticated - belts-and-braces ;-)
                delete(getNodeTargetsUrl(o1Id), null, o2Id, 401);

                delete(getNodeTargetsUrl(UUID.randomUUID().toString()), user1, o2Id, null, 404);
                delete(getNodeTargetsUrl(o1Id), user1, UUID.randomUUID().toString(), null, 404);

                // -ve test -nothing to delete - for any assoc type
                delete(getNodeTargetsUrl(o1Id), user1, o2Id, null, 404);

                // -ve test - nothing to delete - for given assoc type
                params = new HashMap<>(2);
                params.put(PARAM_ASSOC_TYPE, ASSOC_TYPE_CM_REFERENCES);
                delete(getNodeTargetsUrl(o1Id), user1, o2Id, params, 404);

                // -ve test - unknown assoc type
                params = new HashMap<>(2);
                params.put(PARAM_ASSOC_TYPE, "cm:unknowntype");
                delete(getNodeTargetsUrl(o1Id), user1, o2Id, params, 400);
            }
        }
        finally
        {
            // some cleanup
            Map<String, String> params = Collections.singletonMap("permanent", "true");
            delete(URL_NODES, user1, f1Id, params, 204);
            delete(URL_NODES, user1, f2Id, params, 204);
        }
    }

    @Test
    public void testNodePeerAssocsPermissions() throws Exception
    {
        // as user 1 - create folder in "Shared Files" area and content within the folder

        String sharedFolderNodeId = getSharedNodeId(user1);

        String sf1Id = createFolder(user1, sharedFolderNodeId, "shared folder "+RUNID).getId();

        Node n = new Node();
        n.setName("shared content "+RUNID);
        n.setNodeType(TYPE_CM_CONTENT);
        n.setAspectNames(Arrays.asList(ASPECT_CM_REFERENCING, ASPECT_CM_PARTABLE));
        HttpResponse response = post(getNodeChildrenUrl(sf1Id), user1, toJsonAsStringNonNull(n), 201);
        String so1Id = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class).getId();


        // as user 1 - create folder in user's home (My Files) area and content within the folder

        String u1myNodeId = getMyNodeId(user1);

        String u1f1Id = createFolder(user1, u1myNodeId, "f1").getId();

        n = new Node();
        n.setName("o1");
        n.setNodeType(TYPE_CM_CONTENT);
        n.setAspectNames(Arrays.asList(ASPECT_CM_REFERENCING, ASPECT_CM_PARTABLE));
        response = post(getNodeChildrenUrl(u1f1Id), user1, toJsonAsStringNonNull(n), 201);
        String u1o1Id = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class).getId();

        // as user 2 - create folder in user's home (My Files) area and content within the folder

        String u2myNodeId = getMyNodeId(user2);

        String u2f1Id = createFolder(user2, u2myNodeId, "f1").getId();

        n = new Node();
        n.setName("o1");
        n.setNodeType(TYPE_CM_CONTENT);
        n.setAspectNames(Arrays.asList(ASPECT_CM_REFERENCING, ASPECT_CM_PARTABLE));
        response = post(getNodeChildrenUrl(u2f1Id), user2, toJsonAsStringNonNull(n), 201);
        String u2o1Id = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class).getId();

        try
        {
            Paging paging = getPaging(0, 100);

            // empty lists - before

            response = getAll(getNodeTargetsUrl(u1f1Id), user1, paging, null, 200);
            List<Node> nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(0, nodes.size());

            response = getAll(getNodeTargetsUrl(u2f1Id), user2, paging, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(0, nodes.size());

            // Create some assocs

            AssocTarget tgt = new AssocTarget(u1o1Id, ASSOC_TYPE_CM_REFERENCES);
            post(getNodeTargetsUrl(u1f1Id), user1, toJsonAsStringNonNull(tgt), 201);

            tgt = new AssocTarget(u2o1Id, ASSOC_TYPE_CM_REFERENCES);
            post(getNodeTargetsUrl(u2f1Id), user2, toJsonAsStringNonNull(tgt), 201);

            response = getAll(getNodeTargetsUrl(u1f1Id), user1, paging, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(1, nodes.size());

            response = getAll(getNodeTargetsUrl(u2f1Id), user2, paging, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(1, nodes.size());

            // -ve tests
            {
                // source/target not readable

                // list
                getAll(getNodeTargetsUrl(u1f1Id), user2, paging, null, 403);
                getAll(getNodeSourcesUrl(u1o1Id), user2, paging, null, 403);

                // create
                tgt = new AssocTarget(u2o1Id, ASSOC_TYPE_CM_REFERENCES);
                post(getNodeTargetsUrl(u1f1Id), user1, toJsonAsStringNonNull(tgt), 403);

                tgt = new AssocTarget(u1o1Id, ASSOC_TYPE_CM_REFERENCES);
                post(getNodeTargetsUrl(u2f1Id), user1, toJsonAsStringNonNull(tgt), 403);

                // remove
                delete(getNodeTargetsUrl(u1f1Id), user2, u2o1Id, null, 403);
                delete(getNodeTargetsUrl(u2f1Id), user2, u1o1Id, null, 404);
            }


            // Test listing targets (with permissions applied)

            // update permission
            // TODO refactor with remote permission api calls (use v0 until we have v1 ?)
            AuthenticationUtil.setFullyAuthenticatedUser(user1);
            permissionService.setPermission(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, sf1Id), user2, PermissionService.EDITOR, true);

            // TODO improve - admin-related tests
            publicApiClient.setRequestContext(new RequestContext("-default-", "admin", "admin"));
            response = publicApiClient.get(getScope(), "nodes/"+sf1Id+"/targets", null, null, null, createParams(paging, null));
            checkStatus(200, response.getStatusCode());
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(0, nodes.size());

            // user 1
            tgt = new AssocTarget(u1o1Id, ASSOC_TYPE_CM_REFERENCES);
            post(getNodeTargetsUrl(sf1Id), user1, toJsonAsStringNonNull(tgt), 201);

            // user 2
            tgt = new AssocTarget(u2o1Id, ASSOC_TYPE_CM_REFERENCES);
            post(getNodeTargetsUrl(sf1Id), user2, toJsonAsStringNonNull(tgt), 201);

            // TODO improve - admin-related tests
            publicApiClient.setRequestContext(new RequestContext("-default-", "admin", "admin"));
            response = publicApiClient.get(getScope(), "nodes/"+sf1Id+"/targets", null, null, null, createParams(paging, null));
            checkStatus(200, response.getStatusCode());
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(2, nodes.size());

            response = getAll(getNodeTargetsUrl(sf1Id), user1, paging, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(1, nodes.size());
            assertEquals(u1o1Id, nodes.get(0).getId());

            response = getAll(getNodeTargetsUrl(sf1Id), user2, paging, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(1, nodes.size());
            assertEquals(u2o1Id, nodes.get(0).getId());


            // Test listing sources (with permissions applied)

            // update permission
            // TODO refactor with remote permission api calls (use v0 until we have v1 ?)
            AuthenticationUtil.setFullyAuthenticatedUser(user1);
            permissionService.setPermission(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, sf1Id), user2, PermissionService.EDITOR, true);

            // TODO improve - admin-related tests
            publicApiClient.setRequestContext(new RequestContext("-default-", "admin", "admin"));
            response = publicApiClient.get(getScope(), "nodes/"+so1Id+"/sources", null, null, null, createParams(paging, null));
            checkStatus(200, response.getStatusCode());
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(0, nodes.size());

            // user 1
            tgt = new AssocTarget(so1Id, ASSOC_TYPE_CM_REFERENCES);
            post(getNodeTargetsUrl(u1f1Id), user1, toJsonAsStringNonNull(tgt), 201);

            // user 2
            tgt = new AssocTarget(so1Id, ASSOC_TYPE_CM_REFERENCES);
            post(getNodeTargetsUrl(u2f1Id), user2, toJsonAsStringNonNull(tgt), 201);

            // TODO improve - admin-related tests
            publicApiClient.setRequestContext(new RequestContext("-default-", "admin", "admin"));
            response = publicApiClient.get(getScope(), "nodes/"+so1Id+"/sources", null, null, null, createParams(paging, null));
            checkStatus(200, response.getStatusCode());
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(2, nodes.size());

            response = getAll(getNodeSourcesUrl(so1Id), user1, paging, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(1, nodes.size());
            assertEquals(u1f1Id, nodes.get(0).getId());

            response = getAll(getNodeSourcesUrl(so1Id), user2, paging, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(1, nodes.size());
            assertEquals(u2f1Id, nodes.get(0).getId());
        }
        finally
        {
            // some cleanup
            Map<String, String> params = Collections.singletonMap("permanent", "true");
            delete(URL_NODES, user1, u1f1Id, params, 204);
            delete(URL_NODES, user2, u2f1Id, params, 204);
            delete(URL_NODES, user1, sf1Id, params, 204);
        }
    }

    /**
     * Tests basic api to manage (add, list, remove) node secondary child associations (ie. parent node -> child node)
     *
     * Note: refer to NodeApiTest for tests for primary child association
     *
     * <p>POST:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<parentNodeId>/secondary-children}
     *
     * <p>DELETE:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<parentNodeId>/secondary-children/<childNodeId>}
     *
     * <p>GET:</p>
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<parentNodeId>/secondary-children}
     * {@literal <host>:<port>/alfresco/api/<networkId>/public/alfresco/versions/1/nodes/<childNodeId>/parents}
     */
    @Test
    public void testNodeSecondaryChildAssocs() throws Exception
    {
        String myFolderNodeId = getMyNodeId(user1);

        // create folder
        Node n = new Node();
        n.setName("f1");
        n.setNodeType(TYPE_CM_FOLDER);
        n.setAspectNames(Arrays.asList(ASPECT_CM_PREFERENCES));
        HttpResponse response = post(getNodeChildrenUrl(myFolderNodeId), user1, toJsonAsStringNonNull(n), 201);
        String f1Id = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class).getId();

        // create content node
        String o1Name = "o1";
        n = new Node();
        n.setName(o1Name);
        n.setNodeType(TYPE_CM_CONTENT);
        response = post(getNodeChildrenUrl(f1Id), user1, toJsonAsStringNonNull(n), 201);
        String o1Id = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class).getId();

        // create ano' folder
        String f2Id = createFolder(user1, myFolderNodeId, "f2").getId();

        // create ano' content node
        String o2Name = "o2";
        n = new Node();
        n.setName(o2Name);
        n.setNodeType(TYPE_CM_CONTENT);
        response = post(getNodeChildrenUrl(f2Id), user1, toJsonAsStringNonNull(n), 201);
        String o2Id = RestApiUtil.parseRestApiEntry(response.getJsonResponse(), Node.class).getId();


        try
        {
            // As user 1 ...

            Paging paging = getPaging(0, 100);

            // lists - before

            response = getAll(getNodeSecondaryChildrenUrl(f1Id), user1, paging, null, 200);
            List<Node> nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(0, nodes.size());

            // primary parent only
            response = getAll(getNodeParentsUrl(o2Id), user1, paging, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(1, nodes.size());
            assertEquals(f2Id, nodes.get(0).getId());
            assertEquals(ASSOC_TYPE_CM_CONTAINS, nodes.get(0).getAssociation().getAssocType());
            assertTrue(nodes.get(0).getAssociation().getIsPrimary());

            // create secondary child assoc
            AssocChild secChild = new AssocChild(o2Id, ASSOC_TYPE_CM_CONTAINS);
            post(getNodeSecondaryChildrenUrl(f1Id), user1, toJsonAsStringNonNull(secChild), 201);

            // create ano' secondary child assoc (different type) between the same two nodes
            secChild = new AssocChild(o2Id, ASSOC_TYPE_CM_PREFERENCE_IMAGE);
            post(getNodeSecondaryChildrenUrl(f1Id), user1, toJsonAsStringNonNull(secChild), 201);


            response = getAll(getNodeSecondaryChildrenUrl(f1Id), user1, paging, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            int i = 0;
            for (Node node : nodes)
            {
                Association nodeAssoc = node.getAssociation();
                if (nodeAssoc.getAssocType().equals(ASSOC_TYPE_CM_CONTAINS))
                {
                    i++;
                }
                else if ( nodeAssoc.getAssocType().equals(ASSOC_TYPE_CM_PREFERENCE_IMAGE))
                {
                    i++;
                }
                assertEquals(o2Id, node.getId());
                assertFalse(nodeAssoc.getIsPrimary());
            }
            assertEquals(2, i);

            response = getAll(getNodeParentsUrl(o2Id), user1, paging, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            i = 0;
            for (Node node : nodes)
            {
                String nodeId = node.getId();
                Association nodeAssoc = node.getAssociation();
                if (nodeId.equals(f2Id))
                {
                    assertEquals(ASSOC_TYPE_CM_CONTAINS, nodeAssoc.getAssocType());
                    assertTrue(nodeAssoc.getIsPrimary());
                    i++;
                }
                else if (nodeId.equals(f1Id))
                {
                    if (nodeAssoc.getAssocType().equals(ASSOC_TYPE_CM_CONTAINS))
                    {
                        i++;
                    }
                    else if ( nodeAssoc.getAssocType().equals(ASSOC_TYPE_CM_PREFERENCE_IMAGE))
                    {
                        i++;
                    }
                    assertFalse(nodeAssoc.getIsPrimary());
                }
            }
            assertEquals(3, i);

            // test list filter - assocType (/secondary-children & /parents)

            Map<String, String> params = new HashMap<>();
            params.put("where", "(assocType='"+ASSOC_TYPE_CM_CONTAINS+"')");

            response = getAll(getNodeSecondaryChildrenUrl(f1Id), user1, paging, params, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(1, nodes.size());
            assertEquals(o2Id, nodes.get(0).getId());
            assertEquals(ASSOC_TYPE_CM_CONTAINS, nodes.get(0).getAssociation().getAssocType());

            response = getAll(getNodeParentsUrl(o2Id), user1, paging, params, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            i = 0;
            for (Node node : nodes)
            {
                String nodeId = node.getId();
                Association nodeAssoc = node.getAssociation();
                if (nodeId.equals(f2Id))
                {
                    assertEquals(ASSOC_TYPE_CM_CONTAINS, nodeAssoc.getAssocType());
                    assertTrue(nodeAssoc.getIsPrimary());
                    i++;
                }
                else if (nodeId.equals(f1Id))
                {
                    assertEquals(ASSOC_TYPE_CM_CONTAINS, nodeAssoc.getAssocType());
                    assertFalse(nodeAssoc.getIsPrimary());
                    i++;
                }
            }
            assertEquals(2, i);

            params = new HashMap<>();
            params.put("where", "(assocType='"+ASSOC_TYPE_CM_PREFERENCE_IMAGE+"')");

            response = getAll(getNodeSecondaryChildrenUrl(f1Id), user1, paging, params, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(1, nodes.size());
            assertEquals(o2Id, nodes.get(0).getId());
            assertEquals(ASSOC_TYPE_CM_PREFERENCE_IMAGE, nodes.get(0).getAssociation().getAssocType());
            assertFalse(nodes.get(0).getAssociation().getIsPrimary());

            response = getAll(getNodeParentsUrl(o2Id), user1, paging, params, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(1, nodes.size());
            assertEquals(f1Id, nodes.get(0).getId());
            assertEquals(ASSOC_TYPE_CM_PREFERENCE_IMAGE, nodes.get(0).getAssociation().getAssocType());
            assertFalse(nodes.get(0).getAssociation().getIsPrimary());


            // test list filter - isPrimary (/children)

            // note: see NodeApiTest for other filters related to /nodes/{parentId}/children filters

            // note: currently collapses same nodeIds (o2Id x 2) into one - makes sense in terms of File/Folder to avoid duplicate names
            response = getAll(getNodeChildrenUrl(f1Id), user1, paging, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(2, nodes.size());
            List<String> nodeIds = Arrays.asList(new String[] { nodes.get(0).getId(), nodes.get(1).getId()} );
            assertTrue(nodeIds.contains(o1Id));
            assertTrue(nodeIds.contains(o2Id));

            params = new HashMap<>();
            params.put("where", "(isPrimary=true)");

            response = getAll(getNodeChildrenUrl(f1Id), user1, paging, params, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(1, nodes.size());
            assertEquals(o1Id, nodes.get(0).getId());

            params = new HashMap<>();
            params.put("where", "(isPrimary=false)");

            // note: currently collapses same nodeIds (o2Id x 2) into one - makes sense in terms of File/Folder to avoid duplicate names
            response = getAll(getNodeChildrenUrl(f1Id), user1, paging, params, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(1, nodes.size());
            assertEquals(o2Id, nodes.get(0).getId());


            // test list filter - isPrimary (/parents)

            response = getAll(getNodeParentsUrl(o2Id), user1, paging, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(3, nodes.size());

            params = new HashMap<>();
            params.put("where", "(isPrimary=true)");

            response = getAll(getNodeParentsUrl(o2Id), user1, paging, params, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(1, nodes.size());
            assertEquals(f2Id, nodes.get(0).getId());

            params = new HashMap<>();
            params.put("where", "(isPrimary=false)");

            response = getAll(getNodeParentsUrl(o2Id), user1, paging, params, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            i = 0;
            for (Node node : nodes)
            {
                String nodeId = node.getId();
                Association nodeAssoc = node.getAssociation();
                if (nodeId.equals(f1Id))
                {
                    if (nodeAssoc.getAssocType().equals(ASSOC_TYPE_CM_CONTAINS))
                    {
                        i++;
                    }
                    else if ( nodeAssoc.getAssocType().equals(ASSOC_TYPE_CM_PREFERENCE_IMAGE))
                    {
                        i++;
                    }
                    assertFalse(nodeAssoc.getIsPrimary());
                }
            }
            assertEquals(2, i);

            // combined filter
            params = new HashMap<>();
            params.put("where", "(isPrimary=false and assocType='"+ASSOC_TYPE_CM_PREFERENCE_IMAGE+"')");

            response = getAll(getNodeParentsUrl(o2Id), user1, paging, params, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(1, nodes.size());
            assertEquals(f1Id, nodes.get(0).getId());
            assertFalse(nodes.get(0).getAssociation().getIsPrimary());
            assertEquals(ASSOC_TYPE_CM_PREFERENCE_IMAGE, nodes.get(0).getAssociation().getAssocType());


            //


            // remove one secondary child assoc

            params = new HashMap<>(2);
            params.put(PARAM_ASSOC_TYPE, ASSOC_TYPE_CM_CONTAINS);

            delete(getNodeSecondaryChildrenUrl(f1Id), user1, o2Id, params, 204);

            response = getAll(getNodeSecondaryChildrenUrl(f1Id), user1, paging, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(1, nodes.size());

            response = getAll(getNodeParentsUrl(o2Id), user1, paging, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(2, nodes.size());

            params = new HashMap<>(2);
            params.put(PARAM_ASSOC_TYPE, ASSOC_TYPE_CM_PREFERENCE_IMAGE);

            // remove other secondary child assoc
            delete(getNodeSecondaryChildrenUrl(f1Id), user1, o2Id, params, 204);

            response = getAll(getNodeSecondaryChildrenUrl(f1Id), user1, paging, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(0, nodes.size());

            response = getAll(getNodeParentsUrl(o2Id), user1, paging, null, 200);
            nodes = RestApiUtil.parseRestApiEntries(response.getJsonResponse(), Node.class);
            assertEquals(1, nodes.size());

            // TODO +ve test delete of multiple secondary child assocs (if assoc type is not specified)

            //
            // -ve tests - add assoc
            //

            {
                // -ve test - unauthenticated - belts-and-braces ;-)
                secChild = new AssocChild(o2Id, ASSOC_TYPE_CM_CONTAINS);
                post(getNodeSecondaryChildrenUrl(f1Id), null, toJsonAsStringNonNull(secChild), 401);

                // -ve test - model integrity
                secChild = new AssocChild(o2Id, ASSOC_TYPE_CM_CONTAINS);
                post(getNodeSecondaryChildrenUrl(o1Id), user1, toJsonAsStringNonNull(secChild), 422);

                // -ve test - duplicate assoc
                secChild = new AssocChild(o2Id, ASSOC_TYPE_CM_CONTAINS);
                post(getNodeSecondaryChildrenUrl(f1Id), user1, toJsonAsStringNonNull(secChild), 201);
                post(getNodeSecondaryChildrenUrl(f1Id), user1, toJsonAsStringNonNull(secChild), 409);
                delete(getNodeSecondaryChildrenUrl(f1Id), user1, o2Id, null, 204); // cleanup

                secChild = new AssocChild(o2Id, "cm:unknowntype");
                post(getNodeSecondaryChildrenUrl(f1Id), user1, toJsonAsStringNonNull(secChild), 400);
            }

            //
            // -ve test - list assocs
            //

            {
                // -ve test - unauthenticated - belts-and-braces ;-)
                getAll(getNodeSecondaryChildrenUrl(f1Id), null, paging, null, 401);
                getAll(getNodeParentsUrl(o2Id), null, paging, null, 401);

                getAll(getNodeSecondaryChildrenUrl(UUID.randomUUID().toString()), user1, paging, null, 404);
                getAll(getNodeParentsUrl(UUID.randomUUID().toString()), user1, paging, null, 404);

                params = new HashMap<>(1);
                params.put("where", "(assocType='cm:unknownassoctype')");

                getAll(getNodeSecondaryChildrenUrl(o1Id), user1, paging, params, 400);
                getAll(getNodeParentsUrl(o1Id), user1, paging, params, 400);

                // TODO paging - in-built sort order ?
            }

            //
            // -ve test - remove assoc(s)
            //

            {
                // unauthenticated - belts-and-braces ;-)
                delete(getNodeSecondaryChildrenUrl(f1Id), null, o2Id, null, 401);

                delete(getNodeSecondaryChildrenUrl(UUID.randomUUID().toString()), user1, o2Id, null, 404);
                delete(getNodeSecondaryChildrenUrl(f1Id), user1, UUID.randomUUID().toString(), null, 404);

                // nothing to delete - for any assoc type
                delete(getNodeSecondaryChildrenUrl(f1Id), user1, o2Id, null, 404);

                // nothing to delete - for given assoc type
                params = new HashMap<>(2);
                params.put(PARAM_ASSOC_TYPE, ASSOC_TYPE_CM_PREFERENCE_IMAGE);
                delete(getNodeSecondaryChildrenUrl(f1Id), user1, o2Id, params, 404);

                // unknown assoc type
                params = new HashMap<>(2);
                params.put(PARAM_ASSOC_TYPE, "cm:unknowntype");
                delete(getNodeSecondaryChildrenUrl(o1Id), user1, o2Id, params, 400);

                // TODO 400 - try to delete primary assoc using /secondary-children
            }
        }
        finally
        {
            // some cleanup
            Map<String, String> params = Collections.singletonMap(Nodes.PARAM_PERMANENT, "true");
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
