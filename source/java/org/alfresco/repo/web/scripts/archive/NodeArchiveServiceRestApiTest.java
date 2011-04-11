/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
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

    private NodeRef undeletedTestNode;
    private NodeRef deletedTestNode;
    
    // Injected services
    private NodeService nodeService;
    private NodeArchiveService nodeArchiveService;
    private Repository repositoryHelper;
    private RetryingTransactionHelper transactionHelper;

    private StoreRef nodesOriginalStoreRef;
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        // Initialise the required services.
        nodeService = (NodeService) getServer().getApplicationContext().getBean("NodeService");
        nodeArchiveService = (NodeArchiveService) getServer().getApplicationContext().getBean("nodeArchiveService"); // Intentionally small 'n'.
        repositoryHelper = (Repository) getServer().getApplicationContext().getBean("repositoryHelper");
        transactionHelper = (RetryingTransactionHelper)getServer().getApplicationContext().getBean("retryingTransactionHelper");  

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        // Create some nodes which we will delete as part of later test methods.
        undeletedTestNode = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
                {
                    public NodeRef execute() throws Throwable
                    {
                        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
                        String cmName = getClass().getSimpleName() + "_" + System.currentTimeMillis();
                        props.put(ContentModel.PROP_NAME, cmName);
                        props.put(ContentModel.PROP_TITLE, TEST_TITLE);
                        props.put(ContentModel.PROP_DESCRIPTION, TEST_DESCRIPTION);
                        ChildAssociationRef chAssRef = nodeService.createNode(repositoryHelper.getCompanyHome(),
                                ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS,
                                ContentModel.TYPE_CONTENT, props);
                        
                        return chAssRef.getChildRef();
                    }
                });
        
        // We need to remember the StoreRef where this node originally lived. i.e. workspace://SpacesStore
        nodesOriginalStoreRef = undeletedTestNode.getStoreRef();

        // This will ensure that there is always at least some NodeRefs in the 'trash' i.e. the archive store.
        deletedTestNode = transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
                {
                    public NodeRef execute() throws Throwable
                    {
                        // Create the test node.
                        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
                        String cmName = getClass().getSimpleName() + "_" + System.currentTimeMillis();
                        props.put(ContentModel.PROP_NAME, cmName);
                        props.put(ContentModel.PROP_TITLE, TEST_TITLE);
                        props.put(ContentModel.PROP_DESCRIPTION, TEST_DESCRIPTION);
                        ChildAssociationRef chAssRef = nodeService.createNode(repositoryHelper.getCompanyHome(),
                                ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS,
                                ContentModel.TYPE_CONTENT, props);
                        
                        // And intentionally delete it again. This will move it to the archive store.
                        nodeService.deleteNode(chAssRef.getChildRef());
                        
                        // At his point the chAssRef.getChildRef NodeRef will point to the location of the
                        // node before it got deleted. We need to store it's NodeRef *after* deletion, which
                        // will point to the archive store.
                        NodeRef archivedNode = nodeArchiveService.getArchivedNode(chAssRef.getChildRef());
                        
                        return archivedNode;
                    }
                });
    }
    
    @Override
    public void tearDown() throws Exception
    {
        super.tearDown();

        // Tidy up any test nodes that are hanging around.
        transactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
            {
                public Void execute() throws Throwable
                {
                    if (undeletedTestNode != null && nodeService.exists(undeletedTestNode))
                    {
                        nodeService.deleteNode(undeletedTestNode);
                    }
                    return null;
                }
            });
    }

    /**
     * This test calls the GET REST API to read some deleted items from the archive store
     * and checks the various JSON data fields.
     */
    public void testGetDeletedItems() throws Exception
    {
        JSONObject jsonRsp = getArchivedNodes();
        
        JSONObject dataObj = (JSONObject)jsonRsp.get(DATA);
        assertNotNull("JSON 'data' object was null", dataObj);
        
        JSONArray deletedNodesArray = (JSONArray)dataObj.get(AbstractArchivedNodeWebScript.DELETED_NODES);
        assertNotNull("JSON 'deletedNodesArray' object was null", deletedNodesArray);
        assertTrue("Unexpectedly found 0 items in archive store", 0 != deletedNodesArray.length());
        
        // We'll identify a deleted item that we put in the archive during setUp().
        JSONObject deletedNodeToTest = null;
        for (int i = 0; i < deletedNodesArray.length(); i++)
        {
            JSONObject nextJSONObj = (JSONObject)deletedNodesArray.get(i);
            String nodeRefString = nextJSONObj.getString(AbstractArchivedNodeWebScript.NODEREF);
            if (nodeRefString.equals(deletedTestNode.toString()))
            {
                deletedNodeToTest = nextJSONObj;
                break;
            }
        }
        assertNotNull("Failed to find an expected NodeRef within the archive store.", deletedNodeToTest);

        assertEquals(AuthenticationUtil.getAdminUserName(), deletedNodeToTest.getString(AbstractArchivedNodeWebScript.ARCHIVED_BY));
        assertEquals(TEST_TITLE, deletedNodeToTest.getString(AbstractArchivedNodeWebScript.TITLE));
        assertEquals(TEST_DESCRIPTION, deletedNodeToTest.getString(AbstractArchivedNodeWebScript.DESCRIPTION));
        assertNotNull(deletedNodeToTest.getString(AbstractArchivedNodeWebScript.NAME));
        assertNotNull(deletedNodeToTest.getString(AbstractArchivedNodeWebScript.NODEREF));
        assertNotNull(deletedNodeToTest.getString(AbstractArchivedNodeWebScript.DISPLAY_PATH));
        assertNotNull(deletedNodeToTest.getString(AbstractArchivedNodeWebScript.ARCHIVED_DATE));
        assertNotNull(deletedNodeToTest.getString(AbstractArchivedNodeWebScript.FIRST_NAME));
        assertNotNull(deletedNodeToTest.getString(AbstractArchivedNodeWebScript.LAST_NAME));
        assertNotNull(deletedNodeToTest.getString(AbstractArchivedNodeWebScript.NODE_TYPE));
        
        // Check that the results come back sorted by archivedDate (most recent first).
        Date previousDate = null;
        for (int i = 0; i < deletedNodesArray.length(); i++)
        {
            JSONObject nextJSONObj = deletedNodesArray.getJSONObject(i);
            String nextArchivedDateString = nextJSONObj.getString(AbstractArchivedNodeWebScript.ARCHIVED_DATE);
            
            final String ftlDatePattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
            DateFormat df = new SimpleDateFormat(ftlDatePattern);
            
            Date nextArchivedDate = df.parse(nextArchivedDateString);
            
            // Each date should be 'older' than the previous one.
            if (previousDate != null)
            {
                assertTrue("Archived Dates were not reverse-sorted.", nextArchivedDate.before(previousDate));
            }
            previousDate = nextArchivedDate;
        }
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
            if (archivedNode.getString(AbstractArchivedNodeWebScript.NODEREF).equals(deletedTestNode.toString()))
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
     * This method gives the 'archive' REST URL for the specified StoreRef.
     */
    private String getArchiveUrl(StoreRef storeRef)
    {
        String result = MessageFormat.format(ARCHIVE_URL_FORMAT, storeRef.getProtocol(), storeRef.getIdentifier());
        return result;
    }
}
