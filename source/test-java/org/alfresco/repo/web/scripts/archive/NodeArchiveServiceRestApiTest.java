/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts.archive;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.StoreArchiveMap;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyMap;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;
import org.springframework.extensions.webscripts.TestWebScriptServer.DeleteRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PutRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * This class tests the ReST API of the {@link NodeArchiveService}.
 * 
 * @author Neil McErlean
 * @since 3.5
 */
public class NodeArchiveServiceRestApiTest extends BaseWebScriptTest
{
    // Miscellaneous constants used throughout this test class.
    private static final String DATA = "data";
    private static final String ARCHIVE_URL_FORMAT = "/api/archive/{0}/{1}";
    
    private static final String TEST_TITLE = "FooBarTitle";
    private static final String TEST_DESCRIPTION = "This is a FooBar description";
    
    private static final String USER_ONE = "UserOne";
    private static final String USER_TWO = "UserTwo";    

    private NodeRef adminUndeletedTestNode;
    private NodeRef adminDeletedTestNode;
    private NodeRef user1_DeletedTestNode;
    private NodeRef user2_DeletedTestNode;
    private NodeRef workStoreRootNodeRef;
    
    // Injected services
    private NodeService nodeService;
    private NodeArchiveService nodeArchiveService;
    private RetryingTransactionHelper transactionHelper;
    private PersonService personService;
    private MutableAuthenticationService authenticationService;
    private StoreArchiveMap archiveMap; 

    private StoreRef nodesOriginalStoreRef;
    
    private List<StoreRef> createdStores = new ArrayList<StoreRef>();
    private List<NodeRef> createdNodes = new ArrayList<NodeRef>();
    private List<String> createdPeople = new ArrayList<String>(2);
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        // Initialise the required services.
        nodeService = (NodeService) getServer().getApplicationContext().getBean("NodeService");
        nodeArchiveService = (NodeArchiveService) getServer().getApplicationContext().getBean("nodeArchiveService"); // Intentionally small 'n'.
        transactionHelper = (RetryingTransactionHelper) getServer().getApplicationContext().getBean("retryingTransactionHelper");
        personService = (PersonService) getServer().getApplicationContext().getBean("PersonService");
        authenticationService = (MutableAuthenticationService) getServer().getApplicationContext().getBean("AuthenticationService");
        archiveMap = (StoreArchiveMap) getServer().getApplicationContext().getBean("storeArchiveMap");

        // Create Root node for the tests
        workStoreRootNodeRef = createTestStoreAndGetRootNode();

        // Create users
        createUser(USER_ONE);
        createUser(USER_TWO);

        // Create some nodes which we will delete as part of later test methods.
        adminUndeletedTestNode = createTestNode(AuthenticationUtil.getAdminUserName(), createNodeName(), false);
        createdNodes.add(adminUndeletedTestNode);
        // We need to remember the StoreRef where this node originally lived. i.e. workspace://SpacesStore
        nodesOriginalStoreRef = adminUndeletedTestNode.getStoreRef();
        // This will ensure that there is always at least some NodeRefs in the 'trash' i.e. the archive store.
        adminDeletedTestNode = createTestNode(AuthenticationUtil.getAdminUserName(), createNodeName(), true);
        createdNodes.add(adminDeletedTestNode);

        // User_1 creates and deletes a node
        user1_DeletedTestNode = createTestNode(USER_ONE, createNodeName(), true);
        createdNodes.add(user1_DeletedTestNode);

        // User_2 creates and deletes a node
        user2_DeletedTestNode = createTestNode(USER_TWO, createNodeName(), true);
        createdNodes.add(user2_DeletedTestNode);
    }
    
    @Override
    public void tearDown() throws Exception
    {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        super.tearDown();

        // Tidy up any test nodes that are hanging around.
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
                    {
                        public Void execute() throws Throwable
                        {
                            AuthenticationUtil.runAs(new RunAsWork<Void>()
                            {
                                public Void doWork() throws Exception
                                {
                                    for (NodeRef nodeRef : createdNodes)
                                    {
                                        if (nodeRef != null && nodeService.exists(nodeRef))
                                        {
                                            nodeService.deleteNode(nodeRef);
                                        }
                                    }

                                    for (StoreRef store : createdStores)
                                    {
                                        if (store != null && nodeService.exists(store))
                                        {
                                            nodeService.deleteStore(store);
                                        }
                                    }
                                    return null;
                                }
                            }, AuthenticationUtil.getSystemUserName());

                            return null;
                        }
                    });

        // Delete users
        for (String userName : this.createdPeople)
        {
            personService.deletePerson(userName);
        }
        // Clear the lists
        this.createdPeople.clear();
        this.createdNodes.clear();
        this.createdStores.clear();

        AuthenticationUtil.clearCurrentSecurityContext();
    }

    /**
     * This test calls the GET REST API to read some deleted items from the archive store
     * and checks the various JSON data fields.
     */
    public void testGetDeletedItems() throws Exception
    {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        JSONObject jsonRsp = getArchivedNodes();
        
        JSONObject dataObj = (JSONObject)jsonRsp.get(DATA);
        assertNotNull("JSON 'data' object was null", dataObj);
        
        JSONArray deletedNodesArray = (JSONArray)dataObj.get(AbstractArchivedNodeWebScript.DELETED_NODES);
        assertNotNull("JSON 'deletedNodesArray' object was null", deletedNodesArray);
        assertTrue("Unexpectedly found 0 items in archive store", 0 != deletedNodesArray.length());
        
        // We'll identify a deleted item that we put in the archive during setUp().
        //Admin can retrieve all the users' deleted nodes
        for (int i = 0; i < deletedNodesArray.length(); i++)
        {
            JSONObject nextJSONObj = (JSONObject)deletedNodesArray.get(i);
            String nodeRefString = nextJSONObj.getString(AbstractArchivedNodeWebScript.NODEREF);
            if (nodeRefString.equals(adminDeletedTestNode.toString()))
            {
                doTest(nextJSONObj, AuthenticationUtil.getAdminUserName());  
            }
            else if (nodeRefString.equals(user1_DeletedTestNode.toString()))
            {
                doTest(nextJSONObj, USER_ONE);
            }
            else if (nodeRefString.equals(user2_DeletedTestNode.toString()))
            {
                doTest(nextJSONObj, USER_TWO); 
            }
        }        
        // Check that the results come back sorted by archivedDate (most recent first).
        Date previousDate = null;
        for (int i = 0; i < deletedNodesArray.length(); i++)
        {
            JSONObject nextJSONObj = deletedNodesArray.getJSONObject(i);
            String nextArchivedDateString = nextJSONObj.getString(AbstractArchivedNodeWebScript.ARCHIVED_DATE);           
            
            //org.joda.time.DateTime default Chronology is ISOChronology (ISO8601 standard).
            Date nextArchivedDate = new DateTime(nextArchivedDateString).toDate();
            
            // Each date should be 'older' than the previous one.
            if (previousDate != null)
            {
                assertTrue("Archived Dates were not reverse-sorted.", nextArchivedDate.before(previousDate));
            }
            previousDate = nextArchivedDate;
        }       
    }
    
    private void doTest(JSONObject deletedNodeToTest, String archivedBy) throws Exception
    {
        assertNotNull("Failed to find an expected NodeRef within the archive store.", deletedNodeToTest);

        assertEquals(archivedBy, deletedNodeToTest.getString(AbstractArchivedNodeWebScript.ARCHIVED_BY));
        assertEquals(TEST_TITLE, deletedNodeToTest.getString(AbstractArchivedNodeWebScript.TITLE));
        assertEquals(TEST_DESCRIPTION, deletedNodeToTest.getString(AbstractArchivedNodeWebScript.DESCRIPTION));
        assertNotNull(deletedNodeToTest.getString(AbstractArchivedNodeWebScript.NAME));
        assertNotNull(deletedNodeToTest.getString(AbstractArchivedNodeWebScript.NODEREF));
        assertNotNull(deletedNodeToTest.getString(AbstractArchivedNodeWebScript.DISPLAY_PATH));
        assertNotNull(deletedNodeToTest.getString(AbstractArchivedNodeWebScript.ARCHIVED_DATE));
        assertNotNull(deletedNodeToTest.getString(AbstractArchivedNodeWebScript.FIRST_NAME));
        assertNotNull(deletedNodeToTest.getString(AbstractArchivedNodeWebScript.LAST_NAME));
        assertNotNull(deletedNodeToTest.getString(AbstractArchivedNodeWebScript.NODE_TYPE));        
    }

    /**
     * This test calls the GET REST API to read some deleted items for the current user from the archive store
     */
    public void testGetDeletedItemsAsNonAdminUser() throws Exception
    {
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);

        JSONObject jsonRsp = getArchivedNodes();
        JSONObject dataObj = (JSONObject) jsonRsp.get(DATA);
        assertNotNull("JSON 'data' object was null", dataObj);

        JSONArray deletedNodesArray = (JSONArray) dataObj.get(AbstractArchivedNodeWebScript.DELETED_NODES);
        assertNotNull("JSON 'deletedNodesArray' object was null", deletedNodesArray);
        // User_One deleted only 1 node and he doesn't have permission to see other users' archived data.
        assertTrue("Unexpectedly found more than 1 item in the archive store.", 1 == deletedNodesArray.length());

        JSONObject archivedNode = (JSONObject) deletedNodesArray.get(0);
        assertEquals(USER_ONE, archivedNode.getString(AbstractArchivedNodeWebScript.ARCHIVED_BY));

        // Now test, User_Two archived data
        AuthenticationUtil.setFullyAuthenticatedUser(USER_TWO);

        jsonRsp = getArchivedNodes();
        dataObj = (JSONObject) jsonRsp.get(DATA);
        assertNotNull("JSON 'data' object was null", dataObj);

        deletedNodesArray = (JSONArray) dataObj.get(AbstractArchivedNodeWebScript.DELETED_NODES);
        assertNotNull("JSON 'deletedNodesArray' object was null", deletedNodesArray);
        // User_Two deleted only 1 node and he doesn't have permission to see other users' archived data.
        assertTrue("Unexpectedly found more than 1 item in the archive store.",1 == deletedNodesArray.length());

        archivedNode = (JSONObject) deletedNodesArray.get(0);
        assertEquals(USER_TWO, archivedNode.getString(AbstractArchivedNodeWebScript.ARCHIVED_BY));
    }
    
    
    /**
     * This method makes a REST call to get all the nodes currently in the archive store.
     */
    private JSONObject getArchivedNodes() throws IOException, JSONException,
            UnsupportedEncodingException
    {
        String url = this.getArchiveUrl(nodesOriginalStoreRef);
        Response rsp = sendRequest(new GetRequest(url), 200);

        JSONObject jsonRsp = new JSONObject(new JSONTokener(rsp.getContentAsString()));
        return jsonRsp;
    }
    
    /**
     * This test method purges some deleted nodes from the archive store.
     */
    public void testPurgeDeletedItems() throws Exception
    {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        JSONObject archivedNodesJson = getArchivedNodes();
        JSONObject dataJsonObj = archivedNodesJson.getJSONObject("data");
        JSONArray archivedNodesArray = dataJsonObj.getJSONArray(AbstractArchivedNodeWebScript.DELETED_NODES);
        
        int archivedNodesLength = archivedNodesArray.length();
        assertTrue("Insufficient archived nodes for test to run.", archivedNodesLength > 1);
        
        // Take a specific archived node and purge it.
        JSONObject requiredNodeInArchive = null;
        for (int i = 0; i < archivedNodesLength; i++)
        {
            JSONObject archivedNode = archivedNodesArray.getJSONObject(i);
            // We ensure in #setUp() that this NodeRef will be in the archive store.
            if (archivedNode.getString(AbstractArchivedNodeWebScript.NODEREF).equals(adminDeletedTestNode.toString()))
            {
                requiredNodeInArchive = archivedNode;
                break;
            }
            else if (archivedNode.getString(AbstractArchivedNodeWebScript.NODEREF).equals(user1_DeletedTestNode.toString()))
            {
                requiredNodeInArchive = archivedNode;
                break;
            }
            else if (archivedNode.getString(AbstractArchivedNodeWebScript.NODEREF).equals(user2_DeletedTestNode.toString()))
            {
                requiredNodeInArchive = archivedNode;
                break;
            }
        }
        assertNotNull("Expected node not found in archive", requiredNodeInArchive);
        
        // So we have identified a specific Node in the archive that we want to delete permanently (purge).
        String nodeRefString = requiredNodeInArchive.getString(AbstractArchivedNodeWebScript.NODEREF);
        assertTrue("nodeRef string is invalid", NodeRef.isNodeRef(nodeRefString));
        NodeRef nodeRef = new NodeRef(nodeRefString);
        
        // This is not the StoreRef where the node originally lived e.g. workspace://SpacesStore
        // This is its current StoreRef i.e. archive://SpacesStore
        final StoreRef currentStoreRef = nodeRef.getStoreRef();
        
        String deleteUrl = getArchiveUrl(currentStoreRef) + "/" + nodeRef.getId();
        
        // Send the DELETE REST call.
        Response rsp = sendRequest(new DeleteRequest(deleteUrl), 200);
        
        JSONObject jsonRsp = new JSONObject(new JSONTokener(rsp.getContentAsString()));
        JSONObject dataObj = jsonRsp.getJSONObject("data");
        JSONArray purgedNodesArray = dataObj.getJSONArray(ArchivedNodesDelete.PURGED_NODES);
        assertEquals("Only expected one NodeRef to have been purged.", 1, purgedNodesArray.length());
        
        
        // Now we'll purge all the other nodes in the archive that came from the same StoreRef.
        String deleteAllUrl = getArchiveUrl(this.nodesOriginalStoreRef);
        
        rsp = sendRequest(new DeleteRequest(deleteAllUrl), 200);
        jsonRsp = new JSONObject(new JSONTokener(rsp.getContentAsString()));
        
        dataObj = jsonRsp.getJSONObject("data");
        purgedNodesArray = dataObj.getJSONArray(ArchivedNodesDelete.PURGED_NODES);
        
        // Now retrieve all items from the archive store. There should be none.
        assertEquals("Archive store was unexpectedly not empty", 0, getArchivedNodesCount());
    }
    
    /**
     * This test method purges some deleted nodes from the archive store for the current user.
     */
    public void testPurgeDeletedItemsAsNonAdminUser() throws Exception
    {
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);

        String deleteUrl = getArchiveUrl(user2_DeletedTestNode.getStoreRef()) + "/" + user2_DeletedTestNode.getId();

        // User_One has the nodeRef of the node deleted by User_Two. User_One is
        // not an Admin, so he must not be allowed to purge a node which he doesn’t own.
        Response rsp = sendRequest(new DeleteRequest(deleteUrl), 403);
        assertEquals(403, rsp.getStatus());

        // Now User_One gets his own archived node and tries to purge it
        JSONObject jsonRsp = getArchivedNodes();
        JSONObject dataObj = (JSONObject) jsonRsp.get(DATA);
        JSONArray deletedNodesArray = (JSONArray) dataObj.get(AbstractArchivedNodeWebScript.DELETED_NODES);

        // User_One deleted only 1 node and he doesn't have permission to see other users' archived data.
        assertEquals("Unexpectedly found more than 1 item in the archive store.", 1, deletedNodesArray.length());
        JSONObject archivedNode = (JSONObject) deletedNodesArray.get(0);

        // So we have identified a specific Node in the archive that we want to delete permanently (purge).
        String nodeRefString = archivedNode.getString(AbstractArchivedNodeWebScript.NODEREF);
        assertTrue("nodeRef string is invalid", NodeRef.isNodeRef(nodeRefString));

        NodeRef nodeRef = new NodeRef(nodeRefString);

        // This is its current StoreRef i.e. archive://SpacesStore
        deleteUrl = getArchiveUrl(nodeRef.getStoreRef()) + "/" + nodeRef.getId();

        // Send the DELETE REST call.
        rsp = sendRequest(new DeleteRequest(deleteUrl), 200);

        jsonRsp = new JSONObject(new JSONTokener(rsp.getContentAsString()));
        dataObj = jsonRsp.getJSONObject("data");
        JSONArray purgedNodesArray = dataObj.getJSONArray(ArchivedNodesDelete.PURGED_NODES);

        assertEquals("Only expected one NodeRef to have been purged.", 1, purgedNodesArray.length());

        // User_Two is authenticated
        AuthenticationUtil.setFullyAuthenticatedUser(USER_TWO);

        // Now we'll purge all the nodes in the archive that User_Two owns.
        // This should only purge USER_TWO's archived nodes.
        String deleteAllUrl = getArchiveUrl(this.nodesOriginalStoreRef);

        rsp = sendRequest(new DeleteRequest(deleteAllUrl), 200);
        jsonRsp = new JSONObject(new JSONTokener(rsp.getContentAsString()));

        dataObj = jsonRsp.getJSONObject("data");
        purgedNodesArray = dataObj.getJSONArray(ArchivedNodesDelete.PURGED_NODES);
        // User_Two deleted only 1 node.
        assertEquals("Only expected one NodeRef to have been purged.", 1, purgedNodesArray.length());

        // Test no other nodes have been deleted by User_Two 'Delete all archived nodes operation'.
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        jsonRsp = getArchivedNodes();
        dataObj = (JSONObject) jsonRsp.get(DATA);
        assertNotNull("JSON 'data' object was null", dataObj);

        deletedNodesArray = (JSONArray) dataObj.get(AbstractArchivedNodeWebScript.DELETED_NODES);
        assertNotNull("JSON 'deletedNodesArray' object was null", deletedNodesArray);
        assertEquals("There is 1 item in the archive store which was deleted by the Admin.", 1, deletedNodesArray.length());
    }

    /**
     * This method makes a REST call to retrieve the contents of the archive store, returning
     * the number of individual nodes contained in it.
     */
    private int getArchivedNodesCount() throws IOException, JSONException,
            UnsupportedEncodingException
    {
        JSONObject archiveContents = getArchivedNodes();
        JSONObject datatObject = archiveContents.getJSONObject(DATA);
        JSONArray deletedNodesArray = datatObject.getJSONArray(AbstractArchivedNodeWebScript.DELETED_NODES);
        return deletedNodesArray.length();
    }
    
    /**
     * This test method restores some deleted nodes from the archive store.
     */
    public void testRestoreDeletedItems() throws Exception
    {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        JSONObject archivedNodesJson = getArchivedNodes();
        JSONObject dataJsonObj = archivedNodesJson.getJSONObject("data");
        JSONArray archivedNodesArray = dataJsonObj.getJSONArray(AbstractArchivedNodeWebScript.DELETED_NODES);
        
        int archivedNodesLength = archivedNodesArray.length();
        assertTrue("Insufficient archived nodes for test to run.", archivedNodesLength > 1);
        
        // Take a specific archived node and restore it.
        JSONObject firstArchivedNode = archivedNodesArray.getJSONObject(0);
        
        // So we have identified a specific Node in the archive that we want to restore.
        String nodeRefString = firstArchivedNode.getString(AbstractArchivedNodeWebScript.NODEREF);
        assertTrue("nodeRef string is invalid", NodeRef.isNodeRef(nodeRefString));
        NodeRef nodeRef = new NodeRef(nodeRefString);
        
        // This is not the StoreRef where the node originally lived e.g. workspace://SpacesStore
        // This is its current StoreRef i.e. archive://SpacesStore
        final StoreRef currentStoreRef = nodeRef.getStoreRef();
        
        String restoreUrl = getArchiveUrl(currentStoreRef) + "/" + nodeRef.getId();
        
        
        int archivedNodesCountBeforeRestore = getArchivedNodesCount();

        // Send the PUT REST call.
        String jsonString = new JSONStringer().object()
            .key("restoreLocation").value("")
            .endObject()
        .toString();
        Response rsp = sendRequest(new PutRequest(restoreUrl, jsonString, "application/json"), 200);
        
        assertEquals("Expected archive to shrink by one", archivedNodesCountBeforeRestore - 1, getArchivedNodesCount());
    }
    
    /**
     * This test method restores some deleted nodes from the archive store for the current user.
     */
    public void testRestoreDeletedItemsAsNonAdminUser() throws Exception
    {
        AuthenticationUtil.setFullyAuthenticatedUser(USER_ONE);

        String restoreUrl = getArchiveUrl(user2_DeletedTestNode.getStoreRef()) + "/" + user2_DeletedTestNode.getId();

        String jsonString = new JSONStringer().object().key("restoreLocation").value("").endObject().toString();
        
        // User_One has the nodeRef of the node deleted by User_Two. User_One is
        // not an Admin, so he must not be allowed to restore a node which he doesn’t own.
        Response rsp = sendRequest(new PutRequest(restoreUrl, jsonString, "application/json"), 403);
        assertEquals(403, rsp.getStatus());
        
        // Now User_One gets his own archived node and tries to restore it
        JSONObject jsonRsp = getArchivedNodes();
        JSONObject dataObj = (JSONObject) jsonRsp.get(DATA);
        JSONArray deletedNodesArray = (JSONArray) dataObj.get(AbstractArchivedNodeWebScript.DELETED_NODES);

        // User_One deleted only 1 node and he doesn't have permission to see other users' archived data.
        assertEquals("Unexpectedly found more than 1 item in the archive store.", 1, deletedNodesArray.length());
        JSONObject archivedNode = (JSONObject) deletedNodesArray.get(0);

        // So we have identified a specific Node in the archive that we want to restore.
        String nodeRefString = archivedNode.getString(AbstractArchivedNodeWebScript.NODEREF);
        assertTrue("nodeRef string is invalid", NodeRef.isNodeRef(nodeRefString));

        NodeRef nodeRef = new NodeRef(nodeRefString);

        // This is its current StoreRef i.e. archive://SpacesStore
        restoreUrl = getArchiveUrl(nodeRef.getStoreRef()) + "/" + nodeRef.getId();
        
        int archivedNodesCountBeforeRestore = getArchivedNodesCount();

        // Send the PUT REST call.
        jsonString = new JSONStringer().object().key("restoreLocation").value("").endObject().toString();
        rsp = sendRequest(new PutRequest(restoreUrl, jsonString, "application/json"), 200);
        
        assertEquals("Expected archive to shrink by one", archivedNodesCountBeforeRestore - 1, getArchivedNodesCount());        
    }

    /**
     * This method gives the 'archive' REST URL for the specified StoreRef.
     */
    private String getArchiveUrl(StoreRef storeRef)
    {
        String result = MessageFormat.format(ARCHIVE_URL_FORMAT, storeRef.getProtocol(), storeRef.getIdentifier());
        return result;
    }
    
    private NodeRef createTestStoreAndGetRootNode()
    {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        
        StoreRef workStoreRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, getName() + System.currentTimeMillis());
        workStoreRootNodeRef = nodeService.getRootNode(workStoreRef);
        StoreRef archiveStoreRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "archive" + getName() + System.currentTimeMillis());
        archiveMap.put(workStoreRef, archiveStoreRef);
        
        createdStores.add(workStoreRef);
        createdStores.add(archiveStoreRef);
        
        return workStoreRootNodeRef;
    }
    
    private void createUser(String userName)
    {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
        if (this.authenticationService.authenticationExists(userName) == false)
        {
            this.authenticationService.createAuthentication(userName, "password".toCharArray());

            PropertyMap map = new PropertyMap(5);
            map.put(ContentModel.PROP_USERNAME, userName);
            map.put(ContentModel.PROP_FIRSTNAME, "firstName");
            map.put(ContentModel.PROP_LASTNAME, "lastName");
            map.put(ContentModel.PROP_EMAIL, "email@email.com");
            map.put(ContentModel.PROP_JOBTITLE, "jobTitle");

            this.personService.createPerson(map);
            this.createdPeople.add(userName);
        }
    }
    
    private NodeRef createTestNode(final String authenticatedUser, final String cmName, final boolean deleteNode)
    {
        AuthenticationUtil.setFullyAuthenticatedUser(authenticatedUser);
        
        return transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
                    {           
                        public NodeRef execute() throws Throwable
                        { 
                            return AuthenticationUtil.runAs(new RunAsWork<NodeRef>()
                            {
                                public NodeRef doWork() throws Exception
                                {

                                    // Create the test node.
                                    Map<QName, Serializable> props = new HashMap<QName, Serializable>();
                                    props.put(ContentModel.PROP_NAME, cmName);
                                    props.put(ContentModel.PROP_TITLE, TEST_TITLE);
                                    props.put(ContentModel.PROP_DESCRIPTION, TEST_DESCRIPTION);
                                    NodeRef nodeRef = nodeService.createNode(
                                                workStoreRootNodeRef,
                                                ContentModel.ASSOC_CHILDREN, 
                                                ContentModel.ASSOC_CHILDREN,
                                                ContentModel.TYPE_CONTENT, props).getChildRef();

                                    if (deleteNode)
                                    {
                                        // And intentionally delete it again.
                                        // This will move it to the archive store.
                                        nodeService.deleteNode(nodeRef);

                                        // At his point the chAssRef.getChildRef  NodeRef will point to the 
                                        // location of the node before it got deleted. We need to store 
                                        // it's NodeRef *after* deletion, which will point to the archive store.
                                        NodeRef archivedNode = nodeArchiveService.getArchivedNode(nodeRef);

                                        return archivedNode;
                                    }

                                    return nodeRef;

                                }
                            }, AuthenticationUtil.getSystemUserName());
                        }
                    });

    }
    
    private String createNodeName()
    {
        return getClass().getSimpleName() + "_" + System.currentTimeMillis();
    }
}
