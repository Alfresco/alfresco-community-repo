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
package org.alfresco.repo.node;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeRef.Status;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Tests basic {@link NodeService} functionality
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public class NodeServiceTest extends TestCase
{
    public static final String NAMESPACE = "http://www.alfresco.org/test/BaseNodeServiceTest";
    public static final String TEST_PREFIX = "test";
    public static final QName  TYPE_QNAME_TEST = QName.createQName(NAMESPACE, "multiprop");
    public static final QName  PROP_QNAME_NAME = QName.createQName(NAMESPACE, "name");
    public static final QName  ASSOC_QNAME_CHILDREN = QName.createQName(NAMESPACE, "child");
    
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    protected ServiceRegistry serviceRegistry;
    protected NodeService nodeService;
    private TransactionService txnService;
    
    /** populated during setup */
    protected NodeRef rootNodeRef;

    @Override
    protected void setUp() throws Exception
    {
        I18NUtil.setLocale(null);

        serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        nodeService = serviceRegistry.getNodeService();
        txnService = serviceRegistry.getTransactionService();
        
        AuthenticationUtil.setRunAsUserSystem();
        
        // create a first store directly
        RetryingTransactionCallback<NodeRef> createStoreWork = new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute()
            {
                StoreRef storeRef = nodeService.createStore(
                        StoreRef.PROTOCOL_WORKSPACE,
                        "Test_" + System.nanoTime());
                return nodeService.getRootNode(storeRef);
            }
        };
        rootNodeRef = txnService.getRetryingTransactionHelper().doInTransaction(createStoreWork);
    }
    
    /**
     * Clean up the test thread
     */
    @Override
    protected void tearDown()
    {
        AuthenticationUtil.clearCurrentSecurityContext();
        I18NUtil.setLocale(null);
    }
    
    public void testSetUp() throws Exception
    {
        assertNotNull(rootNodeRef);
    }
    
    public void testLocaleSupport() throws Exception
    {
        // Ensure that the root node has the default locale
        Locale locale = (Locale) nodeService.getProperty(rootNodeRef, ContentModel.PROP_LOCALE);
        assertNotNull("Locale property must occur on every node", locale);
        assertEquals("Expected default locale on the root node", I18NUtil.getLocale(), locale);
        assertTrue("Every node must have sys:localized", nodeService.hasAspect(rootNodeRef, ContentModel.ASPECT_LOCALIZED));
        
        // Now switch to a specific locale and create a new node
        I18NUtil.setLocale(Locale.CANADA_FRENCH);
        
        // Create a node using an explicit locale
        NodeRef nodeRef1 = nodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, getName()),
                ContentModel.TYPE_CONTAINER,
                Collections.singletonMap(ContentModel.PROP_LOCALE, (Serializable)Locale.GERMAN)).getChildRef();
        assertTrue("Every node must have sys:localized", nodeService.hasAspect(nodeRef1, ContentModel.ASPECT_LOCALIZED));
        assertEquals(
                "Didn't set the explicit locale during create. ",
                Locale.GERMAN, nodeService.getProperty(nodeRef1, ContentModel.PROP_LOCALE));
        
        // Create a node using the thread's locale
        NodeRef nodeRef2 = nodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, getName()),
                ContentModel.TYPE_CONTAINER).getChildRef();
        assertTrue("Every node must have sys:localized", nodeService.hasAspect(nodeRef2, ContentModel.ASPECT_LOCALIZED));
        assertEquals(
                "Didn't set the locale during create. ",
                Locale.CANADA_FRENCH, nodeService.getProperty(nodeRef2, ContentModel.PROP_LOCALE));
        
        // Switch Locale and modify ml:text property
        I18NUtil.setLocale(Locale.CHINESE);
        nodeService.setProperty(nodeRef2, ContentModel.PROP_DESCRIPTION, "Chinese description");
        I18NUtil.setLocale(Locale.FRENCH);
        nodeService.setProperty(nodeRef2, ContentModel.PROP_DESCRIPTION, "French description");
        
        // Expect that we have MLText (if we are ML aware)
        boolean wasMLAware = MLPropertyInterceptor.setMLAware(true);
        try
        {
            MLText checkDescription = (MLText) nodeService.getProperty(nodeRef2, ContentModel.PROP_DESCRIPTION);
            assertEquals("Chinese description", checkDescription.getValue(Locale.CHINESE));
            assertEquals("French description", checkDescription.getValue(Locale.FRENCH));
        }
        finally
        {
            MLPropertyInterceptor.setMLAware(wasMLAware);
        }
        // But the node locale must not have changed
        assertEquals(
                "Node modification should not affect node locale. ",
                Locale.CANADA_FRENCH, nodeService.getProperty(nodeRef2, ContentModel.PROP_LOCALE));
        
        // Now explicitly set the node's locale
        nodeService.setProperty(nodeRef2, ContentModel.PROP_LOCALE, Locale.ITALY);
        assertEquals(
                "Node locale must be settable. ",
                Locale.ITALY, nodeService.getProperty(nodeRef2, ContentModel.PROP_LOCALE));
        // But mltext must be unchanged
        assertEquals(
                "Canada-French must be closest to French. ",
                "French description", nodeService.getProperty(nodeRef2, ContentModel.PROP_DESCRIPTION));
        
        // Finally, ensure that setting Locale to 'null' is takes the node back to its original locale
        nodeService.setProperty(nodeRef2, ContentModel.PROP_LOCALE, null);
        assertEquals(
                "Node locale set to 'null' does nothing. ",
                Locale.ITALY, nodeService.getProperty(nodeRef2, ContentModel.PROP_LOCALE));
        nodeService.removeProperty(nodeRef2, ContentModel.PROP_LOCALE);
        assertEquals(
                "Node locale removal does nothing. ",
                Locale.ITALY, nodeService.getProperty(nodeRef2, ContentModel.PROP_LOCALE));
        
        // Mass-set the properties, changing the locale in the process
        Map<QName, Serializable> props = nodeService.getProperties(nodeRef2);
        props.put(ContentModel.PROP_LOCALE, Locale.GERMAN);
        nodeService.setProperties(nodeRef2, props);
        assertEquals(
                "Node locale not set in setProperties(). ",
                Locale.GERMAN, nodeService.getProperty(nodeRef2, ContentModel.PROP_LOCALE));
    }

    /**
     * Creates a string of parent-child nodes to fill the given array of nodes
     * 
     * @param workspaceRootNodeRef          the store to use
     * @param liveNodeRefs                  the node array to fill
     */
    private void buildNodeHierarchy(final NodeRef workspaceRootNodeRef, final NodeRef[] liveNodeRefs)
    {
        RetryingTransactionCallback<Void> setupCallback = new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                liveNodeRefs[0] = nodeService.createNode(
                        workspaceRootNodeRef,
                        ContentModel.ASSOC_CHILDREN,
                        QName.createQName(NAMESPACE, "depth-" + 0),
                        ContentModel.TYPE_FOLDER).getChildRef();
                for (int i = 1; i < liveNodeRefs.length; i++)
                {
                    Map<QName, Serializable> props = new HashMap<QName, Serializable>(3);
                    props.put(ContentModel.PROP_NAME, "depth-" + i);
                    liveNodeRefs[i] = nodeService.createNode(
                            liveNodeRefs[i-1],
                            ContentModel.ASSOC_CONTAINS,
                            QName.createQName(NAMESPACE, "depth-" + i),
                            ContentModel.TYPE_FOLDER,
                            props).getChildRef();
                }
                return null;
            }
        };
        txnService.getRetryingTransactionHelper().doInTransaction(setupCallback);
    }
    
    /**
     * Tests that two separate node trees can be deleted concurrently at the database level.
     * This is not a concurren thread issue; instead we delete a hierarchy and hold the txn
     * open while we delete another in a new txn, thereby testing that DB locks don't prevent
     * concurrent deletes.
     * <p/>
     * See: <a href="https://issues.alfresco.com/jira/browse/ALF-5714">ALF-5714</a>
     */
    public void testConcurrentArchive() throws Exception
    {
        final NodeRef workspaceRootNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        final NodeRef[] nodesOne = new NodeRef[10];
        buildNodeHierarchy(workspaceRootNodeRef, nodesOne);
        final NodeRef[] nodesTwo = new NodeRef[10];
        buildNodeHierarchy(workspaceRootNodeRef, nodesTwo);
        
        RetryingTransactionCallback<Void> outerCallback = new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Delete the first hierarchy
                nodeService.deleteNode(nodesOne[0]);
                // Keep the txn hanging around to maintain DB locks
                // and start a second transaction to delete another hierarchy
                RetryingTransactionCallback<Void> innerCallback = new RetryingTransactionCallback<Void>()
                {
                    @Override
                    public Void execute() throws Throwable
                    {
                        nodeService.deleteNode(nodesTwo[0]);
                        return null;
                    }
                };
                txnService.getRetryingTransactionHelper().doInTransaction(innerCallback, false, true);
                return null;
            }
        };
        txnService.getRetryingTransactionHelper().doInTransaction(outerCallback, false, true);
    }
    
    /**
     * Tests archive and restore of simple hierarchy, checking that references and IDs are
     * used correctly.
     */
    public void testArchiveAndRestore()
    {
        // First create a node structure (a very simple one) and record the references and IDs
        final NodeRef[] liveNodeRefs = new NodeRef[10];
        final NodeRef[] archivedNodeRefs = new NodeRef[10];
        
        final NodeRef workspaceRootNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        final NodeRef archiveRootNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE);

        buildNodeHierarchy(workspaceRootNodeRef, liveNodeRefs);

        // Get the node status details
        Long txnIdCreate = null;
        for (int i = 0; i < liveNodeRefs.length; i++)
        {
            StoreRef archivedStoreRef = archiveRootNodeRef.getStoreRef();
            archivedNodeRefs[i] = new NodeRef(archivedStoreRef, liveNodeRefs[i].getId());

            Status liveStatus = nodeService.getNodeStatus(liveNodeRefs[i]);
            Status archivedStatus = nodeService.getNodeStatus(archivedNodeRefs[i]);
            
            // Check that live node statuses are correct
            assertNotNull("'Live' node " + i + " status does not exist.", liveStatus);
            assertFalse("'Live' node " + i + " should be node be deleted", liveStatus.isDeleted());
            assertNull("'Archived' node " + i + " should not (yet) exist.", archivedStatus);
            
            // Nodes in the hierarchy must be in the same txn
            if (txnIdCreate == null)
            {
                txnIdCreate = liveStatus.getDbTxnId();
            }
            else
            {
                // Make sure that the DB Txn ID is the same
                assertEquals(
                        "DB TXN ID should have been the same for the hierarchy. ",
                        txnIdCreate, liveStatus.getDbTxnId());
            }
        }
        
        // Archive the top-level node
        nodeService.deleteNode(liveNodeRefs[0]);
        
        // Recheck the nodes and make sure that all the 'live' nodes are deleted
        Long txnIdDelete = null;
        for (int i = 0; i < liveNodeRefs.length; i++)
        {
            Status liveStatus = nodeService.getNodeStatus(liveNodeRefs[i]);
            Status archivedStatus = nodeService.getNodeStatus(archivedNodeRefs[i]);
            
            // Check that the ghosted nodes are marked as deleted and the archived nodes are not
            assertNotNull("'Live' node " + i + " status does not exist.", liveStatus);
            assertTrue("'Live' node " + i + " should be deleted (ghost entries)", liveStatus.isDeleted());
            assertNotNull("'Archived' node " + i + " does not exist.", archivedStatus);
            assertFalse("'Archived' node " + i + " should be undeleted", archivedStatus.isDeleted());

            // Check that both old (ghosted deletes) and new nodes are in the same txn
            if (txnIdDelete == null)
            {
                txnIdDelete = liveStatus.getDbTxnId();
            }
            else
            {
                // Make sure that the DB Txn ID is the same
                assertEquals(
                        "DB TXN ID should have been the same for the deleted (ghost) nodes. ",
                        txnIdDelete, liveStatus.getDbTxnId());
            }
            assertEquals(
                    "DB TXN ID should be the same for deletes across the hierarchy",
                    txnIdDelete, archivedStatus.getDbTxnId());
        }
        
        // Restore the top-level node
        nodeService.restoreNode(archivedNodeRefs[0], workspaceRootNodeRef, null, null);
        
        // Recheck the nodes and make sure that all the 'archived' nodes are deleted and the 'live' nodes are back
        Long txnIdRestore = null;
        for (int i = 0; i < liveNodeRefs.length; i++)
        {
            Status liveStatus = nodeService.getNodeStatus(liveNodeRefs[i]);
            StoreRef archivedStoreRef = archiveRootNodeRef.getStoreRef();
            archivedNodeRefs[i] = new NodeRef(archivedStoreRef, liveNodeRefs[i].getId());
            Status archivedStatus = nodeService.getNodeStatus(archivedNodeRefs[i]);
            
            // Check that the ghosted nodes are marked as deleted and the archived nodes are not
            assertNotNull("'Live' node " + i + " status does not exist.", liveStatus);
            assertFalse("'Live' node " + i + " should not be deleted", liveStatus.isDeleted());
            assertNotNull("'Archived' node " + i + " does not exist.", archivedStatus);
            assertTrue("'Archived' node " + i + " should be deleted (ghost entry)", archivedStatus.isDeleted());

            // Check that both old (ghosted deletes) and new nodes are in the same txn
            if (txnIdRestore == null)
            {
                txnIdRestore = liveStatus.getDbTxnId();
            }
            else
            {
                // Make sure that the DB Txn ID is the same
                assertEquals(
                        "DB TXN ID should have been the same for the restored nodes. ",
                        txnIdRestore, liveStatus.getDbTxnId());
            }
            assertEquals(
                    "DB TXN ID should be the same for the ex-archived (now-ghost) nodes. ",
                    txnIdRestore, archivedStatus.getDbTxnId());
        }
    }
    
    public void testGetAssocById()
    {
        // Get a node association that doesn't exist
        AssociationRef assocRef = nodeService.getAssoc(Long.MAX_VALUE);
        assertNull("Should get null for missing ID of association. ", assocRef);
    }
    
    public void testDuplicateChildNodeName()
    {
        final NodeRef[] liveNodeRefs = new NodeRef[3];
        final NodeRef workspaceRootNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        buildNodeHierarchy(workspaceRootNodeRef, liveNodeRefs);
        
        // Get the name of the last node
        final String lastName = (String) nodeService.getProperty(liveNodeRefs[2], ContentModel.PROP_NAME);
        // Now create a node with the same name
        RetryingTransactionCallback<NodeRef> newNodeCallback = new RetryingTransactionCallback<NodeRef>()
        {
            @Override
            public NodeRef execute() throws Throwable
            {
                Map<QName, Serializable> props = new HashMap<QName, Serializable>(3);
                props.put(ContentModel.PROP_NAME, lastName);
                return nodeService.createNode(
                        liveNodeRefs[1],
                        ContentModel.ASSOC_CONTAINS,
                        QName.createQName(NAMESPACE, "duplicate"),
                        ContentModel.TYPE_FOLDER,
                        props).getChildRef();
            }
        };
        try
        {
            txnService.getRetryingTransactionHelper().doInTransaction(newNodeCallback);
            fail("Duplicate child node name not detected.");
        }
        catch (DuplicateChildNodeNameException e)
        {
            // Expected
        }
    }
    
    /**
     * Checks that the node caches react correct when a node is deleted
     */
    public void testCaches_DeleteNode()
    {
        final NodeRef[] liveNodeRefs = new NodeRef[10];
        final NodeRef workspaceRootNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);

        buildNodeHierarchy(workspaceRootNodeRef, liveNodeRefs);
        nodeService.addAspect(liveNodeRefs[3], ContentModel.ASPECT_TEMPORARY, null);
        
        // Create a child under node 2
        Map<QName, Serializable> props = new HashMap<QName, Serializable>(3);
        props.put(ContentModel.PROP_NAME, "Secondary");
        NodeRef secondaryNodeRef = nodeService.createNode(
                liveNodeRefs[2],
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NAMESPACE, "secondary"),
                ContentModel.TYPE_FOLDER,
                props).getChildRef();
        // Make it a child of node 3
        nodeService.addChild(liveNodeRefs[3], secondaryNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NAMESPACE, "secondary"));
        // Make it a child of node 4
        nodeService.addChild(liveNodeRefs[4], secondaryNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NAMESPACE, "secondary"));
        
        // Check
        List<ChildAssociationRef> parentAssocsPre = nodeService.getParentAssocs(secondaryNodeRef);
        assertEquals("Incorrect number of parent assocs", 3, parentAssocsPre.size());
        
        // Delete node 3 (should affect 2 of the parent associations);
        nodeService.deleteNode(liveNodeRefs[3]);
        
        // Check
        List<ChildAssociationRef> parentAssocsPost = nodeService.getParentAssocs(secondaryNodeRef);
        assertEquals("Incorrect number of parent assocs", 1, parentAssocsPost.size());
    }
}
