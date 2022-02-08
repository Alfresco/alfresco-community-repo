/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.repo.node;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.cache.TransactionalCache;
import org.alfresco.repo.cache.TransactionalCache.ValueHolder;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.domain.contentdata.ContentDataDAO;
import org.alfresco.repo.domain.contentdata.ContentUrlEntity;
import org.alfresco.repo.domain.node.Node;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.node.NodeEntity;
import org.alfresco.repo.domain.node.NodeVersionKey;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.domain.query.CannedQueryDAO;
import org.alfresco.repo.domain.query.CannedQueryDAOTest;
import org.alfresco.repo.node.NodeServicePolicies.BeforeCreateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.BeforeSetNodeTypePolicy;
import org.alfresco.repo.node.NodeServicePolicies.BeforeUpdateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnSetNodeTypePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy;
import org.alfresco.repo.node.db.NodeHierarchyWalker;
import org.alfresco.repo.node.db.NodeHierarchyWalker.VisitedNode;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.Policy;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeRef.Status;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyMap;
import org.alfresco.util.test.junitrules.ApplicationContextInit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.RuleChain;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Tests basic {@link NodeService} functionality
 * 
 * @author Derek Hulley
 * @since 4.0
 */

@Category(OwnJVMTestsCategory.class)
public class NodeServiceTest
{
    public static final String NAMESPACE = "http://www.alfresco.org/test/BaseNodeServiceTest";
    public static final String TEST_PREFIX = "test";
    public static final QName  TYPE_QNAME_TEST = QName.createQName(NAMESPACE, "multiprop");
    public static final QName  PROP_QNAME_NAME = QName.createQName(NAMESPACE, "name");
    public static final QName  ASSOC_QNAME_CHILDREN = QName.createQName(NAMESPACE, "child");
    
    // Rule to initialise the default Alfresco spring configuration
    public static ApplicationContextInit APP_CONTEXT_INIT = ApplicationContextInit.createStandardContextWithOverrides(CannedQueryDAOTest.IBATIS_TEST_CONTEXT);

    // Tie them together in a static Rule Chain
    @ClassRule public static RuleChain staticRuleChain = RuleChain.outerRule(APP_CONTEXT_INIT);
 
    private static Log logger = LogFactory.getLog(NodeServiceTest.class);
    
    private static ServiceRegistry serviceRegistry;
    private static NodeService nodeService;
    private static PersonService personService;
    private static ContentService contentService;
    private static PermissionService permissionService;
    private static NodeDAO nodeDAO;
    private static VersionService versionService;
    private static TransactionService txnService;
    private static PolicyComponent policyComponent;
    private static CannedQueryDAO cannedQueryDAOForTesting;
    private static SimpleCache<Serializable, ValueHolder<Serializable>> nodesCache;
    private static SimpleCache<Serializable, ValueHolder<Serializable>> propsCache;
    private static SimpleCache<Serializable, ValueHolder<Serializable>> aspectsCache;
    private static SimpleCache<Long, ContentUrlEntity> contentDataCache;
    private static SimpleCache<Long, ContentUrlEntity> contentUrlCache;
    
    private static Long deletedTypeQNameId;
    
    /** populated during setup */
    private static NodeRef rootNodeRef;

    @SuppressWarnings("unchecked")
    @BeforeClass public static void setup() throws Exception
    {
        I18NUtil.setLocale(null);

        serviceRegistry = (ServiceRegistry) APP_CONTEXT_INIT.getApplicationContext().getBean(ServiceRegistry.SERVICE_REGISTRY);
        nodeService = serviceRegistry.getNodeService();
        personService = serviceRegistry.getPersonService();
        contentService = serviceRegistry.getContentService();
        permissionService = serviceRegistry.getPermissionService();
        nodeDAO = (NodeDAO) APP_CONTEXT_INIT.getApplicationContext().getBean("nodeDAO");
        versionService = serviceRegistry.getVersionService();
        txnService = serviceRegistry.getTransactionService();
        policyComponent = (PolicyComponent) APP_CONTEXT_INIT.getApplicationContext().getBean("policyComponent");
        cannedQueryDAOForTesting = (CannedQueryDAO) APP_CONTEXT_INIT.getApplicationContext().getBean("cannedQueryDAOForTesting");
        
        // Get the caches for later testing
        nodesCache = (SimpleCache<Serializable, ValueHolder<Serializable>>) APP_CONTEXT_INIT.getApplicationContext().getBean("node.nodesSharedCache");
        propsCache = (SimpleCache<Serializable, ValueHolder<Serializable>>) APP_CONTEXT_INIT.getApplicationContext().getBean("node.propertiesSharedCache");
        aspectsCache = (SimpleCache<Serializable, ValueHolder<Serializable>>) APP_CONTEXT_INIT.getApplicationContext().getBean("node.aspectsSharedCache");
        contentDataCache = (SimpleCache<Long, ContentUrlEntity>) APP_CONTEXT_INIT.getApplicationContext().getBean("contentDataCache");
        contentUrlCache = (SimpleCache<Long, ContentUrlEntity>) APP_CONTEXT_INIT.getApplicationContext().getBean("contentUrlCache");
        
        // Clear the caches to remove fluff
        nodesCache.clear();
        propsCache.clear();
        aspectsCache.clear();
        contentDataCache.clear();
        contentUrlCache.clear();
        
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
        
        final QNameDAO qnameDAO = (QNameDAO) APP_CONTEXT_INIT.getApplicationContext().getBean("qnameDAO");
        deletedTypeQNameId = txnService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Long>()
        {
            @Override
            public Long execute() throws Throwable
            {
                return qnameDAO.getOrCreateQName(ContentModel.TYPE_DELETED).getFirst();
            }
        });

    }
    
    /**
     * Clean up the test thread
     */
    @AfterClass public static void tearDown()
    {
        AuthenticationUtil.clearCurrentSecurityContext();
        I18NUtil.setLocale(null);
    }
    
    @Test public void testSetUp() throws Exception
    {
        assertNotNull(rootNodeRef);
    }
    
    @Test public void testLocaleSupport() throws Exception
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
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, this.getClass().getName()),
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
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, this.getClass().getName()),
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
                Map<QName, Serializable> props = new HashMap<QName, Serializable>(3);
                props.put(ContentModel.PROP_NAME, "depth-" + 0 + "-" + GUID.generate());
                liveNodeRefs[0] = nodeService.createNode(
                        workspaceRootNodeRef,
                        ContentModel.ASSOC_CHILDREN,
                        QName.createQName(NAMESPACE, "depth-" + 0),
                        ContentModel.TYPE_FOLDER,
                        props).getChildRef();
                for (int i = 1; i < liveNodeRefs.length; i++)
                {
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

    @Test public void testRootAspect() throws Exception
    {
        final NodeRef workspaceRootNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        final NodeRef[] nodes = new NodeRef[6];
        buildNodeHierarchy(workspaceRootNodeRef, nodes);

        Set<NodeRef> allRootNodes = nodeService.getAllRootNodes(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        int initialNumRootNodes = allRootNodes.size();

        nodeService.addAspect(nodes[1], ContentModel.ASPECT_ROOT, null);
        nodeService.addAspect(nodes[3], ContentModel.ASPECT_ROOT, null);
        nodeService.addAspect(nodes[4], ContentModel.ASPECT_ROOT, null);

        allRootNodes = nodeService.getAllRootNodes(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        assertEquals("", 3, allRootNodes.size() - initialNumRootNodes);
        List<Path> paths = nodeService.getPaths(nodes[5], false);
        assertEquals("", 4, paths.size());

        nodeService.removeAspect(nodes[3], ContentModel.ASPECT_ROOT);
        allRootNodes = nodeService.getAllRootNodes(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        assertEquals("", 2, allRootNodes.size() - initialNumRootNodes);
        paths = nodeService.getPaths(nodes[5], false);
        for(Path path : paths)
        {
            System.out.println("Path = " + path.toString());
        }
        assertEquals("", 3, paths.size());
    }

    /**
     * Test class to detect inner transaction failure
     */
    private static class InnerCallbackException extends RuntimeException
    {
        private static final long serialVersionUID = 4993673371982008186L;
        
        private final Throwable hiddenCause;

        public InnerCallbackException(Throwable hiddenCause)
        {
            super(hiddenCause.getMessage());
            this.hiddenCause = hiddenCause;
        }

        public Throwable getHiddenCause()
        {
            return hiddenCause;
        }
    }

    /**
     * Tests that two separate node trees can be deleted concurrently at the database level.
     * This is not a concurrent thread issue; instead we delete a hierarchy and hold the txn
     * open while we delete another in a new txn, thereby testing that DB locks don't prevent
     * concurrent deletes.
     * <p/>
     * See: <a href="https://issues.alfresco.com/jira/browse/ALF-5714">ALF-5714</a><br/>
     * See: <a href="https://issues.alfresco.com/jira/browse/ALF-16888">ALF-16888</a>
     * See: <a href="https://issues.alfresco.com/jira/browse/REPO-2783">REPO-2783</a>
     * <p/>
     * Note: if this test hangs for MySQL then check if 'innodb_locks_unsafe_for_binlog = true' (and restart MySQL + test)
     */
    public void testConcurrentArchive() throws Exception
    {
        final NodeRef workspaceRootNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        final NodeRef[] nodesPrimer = new NodeRef[2];
        buildNodeHierarchy(workspaceRootNodeRef, nodesPrimer);
        final NodeRef[] nodesOne = new NodeRef[15];
        buildNodeHierarchy(workspaceRootNodeRef, nodesOne);
        final NodeRef[] nodesTwo = new NodeRef[10];
        buildNodeHierarchy(workspaceRootNodeRef, nodesTwo);

        // Prime the root of the archive store (first child adds inherited ACL)
        nodeService.deleteNode(nodesPrimer[0]);

        RetryingTransactionCallback<Void> outerCallback = new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // Delete the first hierarchy
                nodeService.deleteNode(nodesOne[0]);

                // Keep the txn hanging around to maintain DB locks
                // and start a second transaction to delete another hierarchy
                class InnerThread extends Thread
                {

                    private Throwable error;

                    public InnerThread()
                    {
                        setDaemon(true);
                    }

                    public Throwable getError()
                    {
                        return error;
                    }

                    /*
                     * (non-Javadoc)
                     * @see java.lang.Thread#run()
                     */
                    @Override
                    public void run()
                    {
                        AuthenticationUtil.setRunAsUserSystem();
                        RetryingTransactionCallback<Void> innerCallback = new RetryingTransactionCallback<Void>()
                        {
                            @Override
                            public Void execute() throws Throwable
                            {
                                try
                                {
                                    nodeService.deleteNode(nodesTwo[0]);
                                    return null;
                                }
                                catch (Throwable t)
                                {
                                    // Wrap throwables so they pass straight through the retry mechanism
                                    throw new InnerCallbackException(t);
                                }
                            }
                        };
                        try
                        {
                            txnService.getRetryingTransactionHelper().doInTransaction(innerCallback, false, true);
                        }
                        catch (InnerCallbackException e)
                        {
                            error = e.getHiddenCause();
                        }
                    }
                }
                InnerThread innerThread = new InnerThread();
                innerThread.start();
                innerThread.join(30000);
                if (innerThread.isAlive())
                {
                    innerThread.interrupt();
                    fail("Transaction hung for 30 seconds. Test failed.");
                }
                // Rethrow potentially retryable exception
                Throwable t = innerThread.getError();
                if (t != null)
                {
                    throw t;
                }
                return null;
            }
        };
        txnService.getRetryingTransactionHelper().doInTransaction(outerCallback, false, true);
    }
    
    /**
     * Tests archive and restore of simple hierarchy, checking that references and IDs are
     * used correctly.
     */
    @Test public void testArchiveAndRestore()
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
    
    @Test public void testGetAssocById()
    {
        // Get a node association that doesn't exist
        AssociationRef assocRef = nodeService.getAssoc(Long.MAX_VALUE);
        assertNull("Should get null for missing ID of association. ", assocRef);
    }
    
    @Test public void testDuplicateChildNodeName()
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
    
    @Test public void testGetChildren_Limited()
    {
        // Create a node and loads of children
        final NodeRef[] liveNodeRefs = new NodeRef[10];
        final NodeRef workspaceRootNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);

        buildNodeHierarchy(workspaceRootNodeRef, liveNodeRefs);
        
        // Hook 3rd and subsequent children into 1st child
        for (int i = 2; i < liveNodeRefs.length; i++)
        {
            nodeService.addChild(
                    liveNodeRefs[0],
                    liveNodeRefs[i],
                    ContentModel.ASSOC_CONTAINS,
                    QName.createQName(NAMESPACE, "secondary"));
        }
        
        // Do limited queries each time
        for (int i = 1; i < liveNodeRefs.length; i++)
        {
            List<ChildAssociationRef> childAssocRefs = nodeService.getChildAssocs(liveNodeRefs[0], null, null, i, true);
            assertEquals("Expected exact number of child assocs", i, childAssocRefs.size());
        }
        
        // Repeat, but don't preload
        for (int i = 1; i < liveNodeRefs.length; i++)
        {
            List<ChildAssociationRef> childAssocRefs = nodeService.getChildAssocs(liveNodeRefs[0], null, null, i, false);
            assertEquals("Expected exact number of child assocs", i, childAssocRefs.size());
        }
    }

    @Test public void testGetChildren()
    {
        NodeRef workspaceRootNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        int numberOfReferences = 3;
        
        NodeRef childNodeRef = setupTestGetChildren(workspaceRootNodeRef, numberOfReferences);

        List<ChildAssociationRef> childAssocRefs = nodeService.getChildAssocs(childNodeRef, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL, false);
        assertEquals("Expected exact number of reference assocs", numberOfReferences, childAssocRefs.size());

        childAssocRefs = nodeService.getChildAssocs(childNodeRef, ContentModel.ASSOC_CONTAINS, new RegexQNamePattern(NAMESPACE, "reference*"), false);
        assertEquals("Expected exact number of reference assocs", numberOfReferences, childAssocRefs.size());
        
        // Use preloading
        childAssocRefs = nodeService.getChildAssocs(childNodeRef, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL, true);
        assertEquals("Expected exact number of reference assocs", numberOfReferences, childAssocRefs.size());

        childAssocRefs = nodeService.getChildAssocs(childNodeRef, ContentModel.ASSOC_CONTAINS, new RegexQNamePattern(NAMESPACE, "reference*"), true);
        assertEquals("Expected exact number of reference assocs", numberOfReferences, childAssocRefs.size());
        
        // Limit the output to 1 result
        childAssocRefs = nodeService.getChildAssocs(childNodeRef, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL, 1, true);
        assertEquals("Expected exact number of reference assocs", 1, childAssocRefs.size());

        childAssocRefs = nodeService.getChildAssocs(childNodeRef, ContentModel.ASSOC_CONTAINS, new RegexQNamePattern(NAMESPACE, "reference*"), 1, true);
        assertEquals("Expected exact number of reference assocs", 1, childAssocRefs.size());
    }

    private NodeRef setupTestGetChildren(final NodeRef workspaceRootNodeRef, final int numberOfReferences)
    {
        RetryingTransactionCallback<NodeRef> setupCallback = new RetryingTransactionCallback<NodeRef>()
        {
            @Override
            public NodeRef execute() throws Throwable
            {
                NodeRef[] referenceNodeRefs = new NodeRef[numberOfReferences];
                // Create one folder
                Map<QName, Serializable> folderProps = new HashMap<QName, Serializable>(3);
                folderProps.put(ContentModel.PROP_NAME, "folder-" + GUID.generate());
                NodeRef folderNodeRef = nodeService.createNode(
                        workspaceRootNodeRef,
                        ContentModel.ASSOC_CHILDREN,
                        QName.createQName(NAMESPACE, "folder"),
                        ContentModel.TYPE_FOLDER,
                        folderProps).getChildRef();

                // Create some content
                for (int i = 0; i < numberOfReferences; i++)
                {
                    Map<QName, Serializable> props = new HashMap<QName, Serializable>(3);
                    props.put(ContentModel.PROP_NAME, "reference-" + GUID.generate());
                    referenceNodeRefs[i] = nodeService.createNode(
                            folderNodeRef,
                            ContentModel.ASSOC_CONTAINS,
                            QName.createQName(NAMESPACE, "reference"),
                            ContentModel.TYPE_RATING,
                            props).getChildRef();
                }
                return folderNodeRef;
            }
        };
        return txnService.getRetryingTransactionHelper().doInTransaction(setupCallback);
    }

    /**
     * Checks that the node caches react correctly when a node is deleted
     */
    @Test public void testCaches_DeleteNode()
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
    
    /**
     * Checks that file renames are handled when getting children
     */
    @Test public void testCaches_RenameNode()
    {
        final NodeRef[] nodeRefs = new NodeRef[2];
        final NodeRef workspaceRootNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        buildNodeHierarchy(workspaceRootNodeRef, nodeRefs);
        
        // What is the name of the first child?
        String name = (String) nodeService.getProperty(nodeRefs[1], ContentModel.PROP_NAME);
        // Now query for it
        NodeRef nodeRefCheck = nodeService.getChildByName(nodeRefs[0], ContentModel.ASSOC_CONTAINS, name);
        assertNotNull("Did not find node by name", nodeRefCheck);
        assertEquals("Node found was not correct", nodeRefs[1], nodeRefCheck);
        
        // Rename the node
        nodeService.setProperty(nodeRefs[1], ContentModel.PROP_NAME, "New Name");
        // Should find nothing
        nodeRefCheck = nodeService.getChildByName(nodeRefs[0], ContentModel.ASSOC_CONTAINS, name);
        assertNull("Should not have found anything", nodeRefCheck);
        
        // Add another child with the same original name
        NodeRef newChildNodeRef = nodeService.createNode(
                nodeRefs[0],
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NAMESPACE, name),
                ContentModel.TYPE_FOLDER,
                Collections.singletonMap(ContentModel.PROP_NAME, (Serializable)name)).getChildRef();
        // We should find this new node when looking for the name
        nodeRefCheck = nodeService.getChildByName(nodeRefs[0], ContentModel.ASSOC_CONTAINS, name);
        assertNotNull("Did not find node by name", nodeRefCheck);
        assertEquals("Node found was not correct", newChildNodeRef, nodeRefCheck);
    }
    
    /**
     * Looks for a key that contains the toString() of the value
     */
    private Object findCacheValue(SimpleCache<Serializable, ValueHolder<Serializable>> cache, Serializable key)
    {
        Collection<Serializable> keys = cache.getKeys();
        for (Serializable keyInCache : keys)
        {
            String keyInCacheStr = keyInCache.toString();
            String keyStr = key.toString();
            if (keyInCacheStr.endsWith(keyStr))
            {
                Object value = TransactionalCache.getSharedCacheValue(cache, keyInCache);
                return value;
            }
        }
        return null;
    }
    
    private static final QName PROP_RESIDUAL = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, GUID.generate());
    /**
     * Check that simple node property modifications advance the node caches correctly
     */
    @SuppressWarnings("unchecked")
    @Test public void testCaches_ImmutableNodeCaches() throws Exception
    {
        final NodeRef[] nodeRefs = new NodeRef[2];
        final NodeRef workspaceRootNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        buildNodeHierarchy(workspaceRootNodeRef, nodeRefs);
        final NodeRef nodeRef = nodeRefs[1];

        // Get the current node cache key
        Long nodeId = (Long) findCacheValue(nodesCache, nodeRef);
        assertNotNull("Node not found in cache", nodeId);
        Node nodeOne = (Node) findCacheValue(nodesCache, nodeId);
        assertNotNull("Node not found in cache", nodeOne);
        NodeVersionKey nodeKeyOne = nodeOne.getNodeVersionKey();
        
        // Get the node cached values
        Map<QName, Serializable> nodePropsOne = (Map<QName, Serializable>) findCacheValue(propsCache, nodeKeyOne);
        Set<QName> nodeAspectsOne = (Set<QName>) findCacheValue(aspectsCache, nodeKeyOne);
        
        // Check the values
        assertEquals("The node version is incorrect", Long.valueOf(1L), nodeKeyOne.getVersion());
        assertNotNull("No cache entry for properties", nodePropsOne);
        assertNotNull("No cache entry for aspects", nodeAspectsOne);
        assertEquals("Property count incorrect", 1, nodePropsOne.size());
        assertNotNull("Expected a cm:name property", nodePropsOne.get(ContentModel.PROP_NAME));
        assertEquals("Aspect count incorrect", 1, nodeAspectsOne.size());
        assertTrue("Expected a cm:auditable aspect", nodeAspectsOne.contains(ContentModel.ASPECT_AUDITABLE));
        
        // Add a property
        nodeService.setProperty(nodeRef, PROP_RESIDUAL, GUID.generate());
        
        // Get the values for the previous version
        Map<QName, Serializable> nodePropsOneCheck = (Map<QName, Serializable>) findCacheValue(propsCache, nodeKeyOne);
        Set<QName> nodeAspectsOneCheck = (Set<QName>) findCacheValue(aspectsCache, nodeKeyOne);
        assertTrue("Previous cache entries must be left alone", nodePropsOneCheck.equals(nodePropsOne));
        assertTrue("Previous cache entries must be left alone", nodeAspectsOneCheck.equals(nodeAspectsOne));

        // Get the current node cache key
        Node nodeTwo = (Node) findCacheValue(nodesCache, nodeId);
        assertNotNull("Node not found in cache", nodeTwo);
        NodeVersionKey nodeKeyTwo = nodeTwo.getNodeVersionKey();
        
        // Get the node cached values
        Map<QName, Serializable> nodePropsTwo = (Map<QName, Serializable>) findCacheValue(propsCache, nodeKeyTwo);
        Set<QName> nodeAspectsTwo = (Set<QName>) findCacheValue(aspectsCache, nodeKeyTwo);

        // Check the values
        assertEquals("The node version is incorrect", Long.valueOf(2L), nodeKeyTwo.getVersion());
        assertNotNull("No cache entry for properties", nodePropsTwo);
        assertNotNull("No cache entry for aspects", nodeAspectsTwo);
        assertFalse("Properties must have moved on", nodePropsTwo.equals(nodePropsOne));
        assertEquals("Property count incorrect", 2, nodePropsTwo.size());
        assertNotNull("Expected a cm:name property", nodePropsTwo.get(ContentModel.PROP_NAME));
        assertNotNull("Expected a residual property", nodePropsTwo.get(PROP_RESIDUAL));
        assertTrue("Aspects must be carried", nodeAspectsTwo.equals(nodeAspectsOne));
        
        // Remove a property
        nodeService.removeProperty(nodeRef, PROP_RESIDUAL);
        
        // Get the values for the previous version
        Map<QName, Serializable> nodePropsTwoCheck = (Map<QName, Serializable>) findCacheValue(propsCache, nodeKeyTwo);
        Set<QName> nodeAspectsTwoCheck = (Set<QName>) findCacheValue(aspectsCache, nodeKeyTwo);
        assertTrue("Previous cache entries must be left alone", nodePropsTwoCheck.equals(nodePropsTwo));
        assertTrue("Previous cache entries must be left alone", nodeAspectsTwoCheck.equals(nodeAspectsTwo));

        // Get the current node cache key
        Node nodeThree = (Node) findCacheValue(nodesCache, nodeId);
        assertNotNull("Node not found in cache", nodeThree);
        NodeVersionKey nodeKeyThree = nodeThree.getNodeVersionKey();
        
        // Get the node cached values
        Map<QName, Serializable> nodePropsThree = (Map<QName, Serializable>) findCacheValue(propsCache, nodeKeyThree);
        Set<QName> nodeAspectsThree = (Set<QName>) findCacheValue(aspectsCache, nodeKeyThree);

        // Check the values
        assertEquals("The node version is incorrect", Long.valueOf(3L), nodeKeyThree.getVersion());
        assertNotNull("No cache entry for properties", nodePropsThree);
        assertNotNull("No cache entry for aspects", nodeAspectsThree);
        assertFalse("Properties must have moved on", nodePropsThree.equals(nodePropsTwo));
        assertEquals("Property count incorrect", 1, nodePropsThree.size());
        assertNotNull("Expected a cm:name property", nodePropsThree.get(ContentModel.PROP_NAME));
        assertNull("Expected no residual property", nodePropsThree.get(PROP_RESIDUAL));
        assertTrue("Aspects must be carried", nodeAspectsThree.equals(nodeAspectsTwo));
        
        // Add an aspect
        nodeService.addAspect(nodeRef, ContentModel.ASPECT_TITLED, null);
        
        // Get the values for the previous version
        Map<QName, Serializable> nodePropsThreeCheck = (Map<QName, Serializable>) findCacheValue(propsCache, nodeKeyThree);
        Set<QName> nodeAspectsThreeCheck = (Set<QName>) findCacheValue(aspectsCache, nodeKeyThree);
        assertTrue("Previous cache entries must be left alone", nodePropsThreeCheck.equals(nodePropsThree));
        assertTrue("Previous cache entries must be left alone", nodeAspectsThreeCheck.equals(nodeAspectsThree));

        // Get the current node cache key
        Node nodeFour = (Node) findCacheValue(nodesCache, nodeId);
        assertNotNull("Node not found in cache", nodeFour);
        NodeVersionKey nodeKeyFour = nodeFour.getNodeVersionKey();
        
        // Get the node cached values
        Map<QName, Serializable> nodePropsFour = (Map<QName, Serializable>) findCacheValue(propsCache, nodeKeyFour);
        Set<QName> nodeAspectsFour = (Set<QName>) findCacheValue(aspectsCache, nodeKeyFour);

        // Check the values
        assertEquals("The node version is incorrect", Long.valueOf(4L), nodeKeyFour.getVersion());
        assertNotNull("No cache entry for properties", nodePropsFour);
        assertNotNull("No cache entry for aspects", nodeAspectsFour);
        assertTrue("Properties must be carried", nodePropsFour.equals(nodePropsThree));
        assertFalse("Aspects must have moved on", nodeAspectsFour.equals(nodeAspectsThree));
        assertTrue("Expected cm:titled aspect", nodeAspectsFour.contains(ContentModel.ASPECT_TITLED));
        
        // Remove an aspect
        nodeService.removeAspect(nodeRef, ContentModel.ASPECT_TITLED);
        
        // Get the values for the previous version
        Map<QName, Serializable> nodePropsFourCheck = (Map<QName, Serializable>) findCacheValue(propsCache, nodeKeyFour);
        Set<QName> nodeAspectsFourCheck = (Set<QName>) findCacheValue(aspectsCache, nodeKeyFour);
        assertTrue("Previous cache entries must be left alone", nodePropsFourCheck.equals(nodePropsFour));
        assertTrue("Previous cache entries must be left alone", nodeAspectsFourCheck.equals(nodeAspectsFour));

        // Get the current node cache key
        Node nodeFive = (Node) findCacheValue(nodesCache, nodeId);
        assertNotNull("Node not found in cache", nodeFive);
        NodeVersionKey nodeKeyFive = nodeFive.getNodeVersionKey();
        
        // Get the node cached values
        Map<QName, Serializable> nodePropsFive = (Map<QName, Serializable>) findCacheValue(propsCache, nodeKeyFive);
        Set<QName> nodeAspectsFive = (Set<QName>) findCacheValue(aspectsCache, nodeKeyFive);

        // Check the values
        assertEquals("The node version is incorrect", Long.valueOf(5L), nodeKeyFive.getVersion());
        assertNotNull("No cache entry for properties", nodePropsFive);
        assertNotNull("No cache entry for aspects", nodeAspectsFive);
        assertTrue("Properties must be carried", nodePropsFive.equals(nodePropsFour));
        assertFalse("Aspects must have moved on", nodeAspectsFive.equals(nodeAspectsFour));
        assertFalse("Expected no cm:titled aspect ", nodeAspectsFive.contains(ContentModel.ASPECT_TITLED));
        
        // Add an aspect, some properties and secondary association
        RetryingTransactionCallback<Void> nodeSixWork = new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                Map<QName, Serializable> props = new HashMap<QName, Serializable>();
                props.put(ContentModel.PROP_TITLE, "some title");
                nodeService.addAspect(nodeRef, ContentModel.ASPECT_TITLED, props);
                nodeService.setProperty(nodeRef, ContentModel.PROP_DESCRIPTION, "Some description");
                // Adding a child node now triggers behaviour to update a CRC property
//                nodeService.addChild(
//                        Collections.singletonList(workspaceRootNodeRef),
//                        nodeRef,
//                        ContentModel.ASSOC_CHILDREN,
//                        QName.createQName(TEST_PREFIX, "secondary"));
                return null;
            }
        };
        txnService.getRetryingTransactionHelper().doInTransaction(nodeSixWork);
        
        // Get the values for the previous version
        Map<QName, Serializable> nodePropsFiveCheck = (Map<QName, Serializable>) findCacheValue(propsCache, nodeKeyFive);
        Set<QName> nodeAspectsFiveCheck = (Set<QName>) findCacheValue(aspectsCache, nodeKeyFive);
        assertTrue("Previous cache entries must be left alone", nodePropsFiveCheck.equals(nodePropsFive));
        assertTrue("Previous cache entries must be left alone", nodeAspectsFiveCheck.equals(nodeAspectsFive));

        // Get the current node cache key
        Node nodeSix = (Node) findCacheValue(nodesCache, nodeId);
        assertNotNull("Node not found in cache", nodeSix);
        NodeVersionKey nodeKeySix = nodeSix.getNodeVersionKey();
        
        // Get the node cached values
        Map<QName, Serializable> nodePropsSix = (Map<QName, Serializable>) findCacheValue(propsCache, nodeKeySix);
        Set<QName> nodeAspectsSix = (Set<QName>) findCacheValue(aspectsCache, nodeKeySix);

        // Check the values
        assertEquals("The node version is incorrect", Long.valueOf(6L), nodeKeySix.getVersion());
        assertNotNull("No cache entry for properties", nodePropsSix);
        assertNotNull("No cache entry for aspects", nodeAspectsSix);
        assertFalse("Properties must have moved on", nodePropsSix.equals(nodePropsFive));
        assertEquals("Property count incorrect", 3, nodePropsSix.size());
        assertNotNull("Expected a cm:name property", nodePropsSix.get(ContentModel.PROP_NAME));
        assertNotNull("Expected a cm:title property", nodePropsSix.get(ContentModel.PROP_TITLE));
        assertNotNull("Expected a cm:description property", nodePropsSix.get(ContentModel.PROP_DESCRIPTION));
        assertFalse("Aspects must have moved on", nodeAspectsSix.equals(nodeAspectsFive));
        assertTrue("Expected cm:titled aspect ", nodeAspectsSix.contains(ContentModel.ASPECT_TITLED));
        
        // Remove an aspect, some properties and a secondary association
        RetryingTransactionCallback<Void> nodeSevenWork = new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                nodeService.removeAspect(nodeRef, ContentModel.ASPECT_TITLED);
                nodeService.removeChild(workspaceRootNodeRef, nodeRef);
                return null;
            }
        };
        txnService.getRetryingTransactionHelper().doInTransaction(nodeSevenWork);
        
        // Get the values for the previous version
        Map<QName, Serializable> nodePropsSixCheck = (Map<QName, Serializable>) findCacheValue(propsCache, nodeKeySix);
        Set<QName> nodeAspectsSixCheck = (Set<QName>) findCacheValue(aspectsCache, nodeKeySix);
        assertTrue("Previous cache entries must be left alone", nodePropsSixCheck.equals(nodePropsSix));
        assertTrue("Previous cache entries must be left alone", nodeAspectsSixCheck.equals(nodeAspectsSix));

        // Get the current node cache key
        Node nodeSeven = (Node) findCacheValue(nodesCache, nodeId);
        assertNotNull("Node not found in cache", nodeSeven);
        NodeVersionKey nodeKeySeven = nodeSeven.getNodeVersionKey();
        
        // Get the node cached values
        Map<QName, Serializable> nodePropsSeven = (Map<QName, Serializable>) findCacheValue(propsCache, nodeKeySeven);
        Set<QName> nodeAspectsSeven = (Set<QName>) findCacheValue(aspectsCache, nodeKeySeven);

        // Check the values
        assertEquals("The node version is incorrect", Long.valueOf(7L), nodeKeySeven.getVersion());
        assertNotNull("No cache entry for properties", nodePropsSeven);
        assertNotNull("No cache entry for aspects", nodeAspectsSeven);
        assertFalse("Properties must have moved on", nodePropsSeven.equals(nodePropsSix));
        assertEquals("Property count incorrect", 1, nodePropsSeven.size());
        assertNotNull("Expected a cm:name property", nodePropsSeven.get(ContentModel.PROP_NAME));
        assertFalse("Aspects must have moved on", nodeAspectsSeven.equals(nodeAspectsSix));
        assertFalse("Expected no cm:titled aspect ", nodeAspectsSeven.contains(ContentModel.ASPECT_TITLED));
        
        // Modify cm:auditable
        RetryingTransactionCallback<Void> nodeEightWork = new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                BehaviourFilter behaviourFilter = (BehaviourFilter) APP_CONTEXT_INIT.getApplicationContext().getBean("policyBehaviourFilter");
                // Disable behaviour for txn
                behaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
                nodeService.setProperty(nodeRef, ContentModel.PROP_MODIFIER, "Fred");
                return null;
            }
        };
        txnService.getRetryingTransactionHelper().doInTransaction(nodeEightWork);
        
        // Get the values for the previous version
        Map<QName, Serializable> nodePropsSevenCheck = (Map<QName, Serializable>) findCacheValue(propsCache, nodeKeySeven);
        Set<QName> nodeAspectsSevenCheck = (Set<QName>) findCacheValue(aspectsCache, nodeKeySeven);
        assertTrue("Previous cache entries must be left alone", nodePropsSevenCheck.equals(nodePropsSeven));
        assertTrue("Previous cache entries must be left alone", nodeAspectsSevenCheck.equals(nodeAspectsSeven));

        // Get the current node cache key
        Node nodeEight = (Node) findCacheValue(nodesCache, nodeId);
        assertNotNull("Node not found in cache", nodeEight);
        NodeVersionKey nodeKeyEight = nodeEight.getNodeVersionKey();
        
        // Get the node cached values
        Map<QName, Serializable> nodePropsEight = (Map<QName, Serializable>) findCacheValue(propsCache, nodeKeyEight);
        Set<QName> nodeAspectsEight = (Set<QName>) findCacheValue(aspectsCache, nodeKeyEight);

        // Check the values
        assertEquals("The node version is incorrect", Long.valueOf(8L), nodeKeyEight.getVersion());
        assertNotNull("No cache entry for properties", nodePropsEight);
        assertNotNull("No cache entry for aspects", nodeAspectsEight);
        assertEquals("Expected change to cm:modifier", "Fred", nodeEight.getAuditableProperties().getAuditModifier());
        assertTrue("Properties must be carried", nodePropsEight.equals(nodePropsSeven));
        assertTrue("Aspects be carried", nodeAspectsEight.equals(nodeAspectsSeven));
    }
    
    @Test public void testCreateNodePolicies()
    {
        // Create and bind the mock behaviours...
        OnCreateNodePolicy onCreateNodePolicy = createClassPolicy(
                    OnCreateNodePolicy.class,
                    OnCreateNodePolicy.QNAME,
                    ContentModel.TYPE_CONTENT);
        
        BeforeCreateNodePolicy beforeCreateNodePolicy = createClassPolicy(
                    BeforeCreateNodePolicy.class,
                    BeforeCreateNodePolicy.QNAME,
                    ContentModel.TYPE_CONTENT); 
        
        OnCreateChildAssociationPolicy onCreateChildAssociationPolicy = createAssocPolicy(
                        OnCreateChildAssociationPolicy.class,
                        OnCreateChildAssociationPolicy.QNAME,
                        ContentModel.TYPE_STOREROOT);
        
        OnUpdatePropertiesPolicy onUpdatePropertiesPolicy = createClassPolicy(
                        OnUpdatePropertiesPolicy.class,
                        OnUpdatePropertiesPolicy.QNAME,
                        ContentModel.TYPE_CONTENT);
        
        // Create a node - this should result in the behaviours firing.
        NodeRef newNodeRef = nodeService.createNode(
                    rootNodeRef, 
                    ContentModel.ASSOC_CHILDREN, 
                    ContentModel.ASSOC_CHILDREN, 
                    ContentModel.TYPE_CONTENT, 
                    PropertyMap.EMPTY_MAP).getChildRef();
        
        Map<QName, Serializable> propsAfter = nodeService.getProperties(newNodeRef);
        ChildAssociationRef childAssocRef = nodeService.getPrimaryParent(newNodeRef);
        
        // Check the behaviours fired as expected...
        verify(beforeCreateNodePolicy).beforeCreateNode(
                    rootNodeRef,
                    ContentModel.ASSOC_CHILDREN,
                    ContentModel.ASSOC_CHILDREN,
                    ContentModel.TYPE_CONTENT);
        verify(onCreateNodePolicy).onCreateNode(childAssocRef);
        verify(onCreateChildAssociationPolicy).onCreateChildAssociation(childAssocRef, true);
        verify(onUpdatePropertiesPolicy).onUpdateProperties(newNodeRef, PropertyMap.EMPTY_MAP, propsAfter);
    }
    
    @Test public void testSetNodeTypePolicies()
    {   
        // Create a node (before behaviours are attached)
        NodeRef nodeRef = nodeService.createNode(
                    rootNodeRef, 
                    ContentModel.ASSOC_CHILDREN, 
                    ContentModel.ASSOC_CHILDREN, 
                    ContentModel.TYPE_CONTENT, 
                    new HashMap<QName, Serializable>(0)).getChildRef();
        
        // Create and bind the mock behaviours...
        BeforeUpdateNodePolicy beforeUpdatePolicy = createClassPolicy(
                    BeforeUpdateNodePolicy.class,
                    BeforeUpdateNodePolicy.QNAME,
                    ContentModel.TYPE_CONTENT);
        
        OnUpdateNodePolicy onUpdatePolicy = createClassPolicy(
                    OnUpdateNodePolicy.class,
                    OnUpdateNodePolicy.QNAME,
                    ContentModel.TYPE_FOLDER);
        
        BeforeSetNodeTypePolicy beforeSetNodeTypePolicy = createClassPolicy(
                    BeforeSetNodeTypePolicy.class,
                    BeforeSetNodeTypePolicy.QNAME,
                    ContentModel.TYPE_CONTENT);
        
        OnSetNodeTypePolicy onSetNodeTypePolicy = createClassPolicy(
                    OnSetNodeTypePolicy.class,
                    OnSetNodeTypePolicy.QNAME,
                    ContentModel.TYPE_FOLDER);
             
        // Set the type of the new node - this should trigger the correct behaviours.
        nodeService.setType(nodeRef, ContentModel.TYPE_FOLDER);
        
        // Check the behaviours fired as expected...
        verify(beforeUpdatePolicy).beforeUpdateNode(nodeRef);
        verify(onUpdatePolicy).onUpdateNode(nodeRef);
        verify(beforeSetNodeTypePolicy).beforeSetNodeType(nodeRef, ContentModel.TYPE_CONTENT, ContentModel.TYPE_FOLDER);
        verify(onSetNodeTypePolicy).onSetNodeType(nodeRef, ContentModel.TYPE_CONTENT, ContentModel.TYPE_FOLDER);
    }
    
    private <T extends Policy> T createClassPolicy(Class<T> policyInterface, QName policyQName, QName triggerOnClass)
    {
        T policy = mock(policyInterface);
        policyComponent.bindClassBehaviour(
                    policyQName, 
                    triggerOnClass, 
                    new JavaBehaviour(policy, policyQName.getLocalName()));
        return policy;
    }
    

    private <T extends Policy> T createAssocPolicy(Class<T> policyInterface, QName policyQName, QName triggerOnClass)
    {
        T policy = mock(policyInterface);
        policyComponent.bindAssociationBehaviour(
                    policyQName, 
                    triggerOnClass, 
                    new JavaBehaviour(policy, policyQName.getLocalName()));
        return policy;
    }
    
    /**
     * Ensure that nodes cannot be linked to deleted nodes.
     * <p/>
     * Conditions that <i>might</i> cause this are:<br/>
     * <ul>
     *   <li>Node created within a parent node that is being deleted</li>
     *   <li>The node cache is temporarily incorrect when the association is made</li>
     * </ul>
     * <p/>
     * <a href="https://issues.alfresco.com/jira/browse/ALF-12358">Concurrency: Possible to create association references to deleted nodes</a>
     */
    @Test public void testConcurrentLinkToDeletedNode() throws Throwable
    {
        // First find any broken links to start with
        final NodeEntity params = new NodeEntity();
        params.setId(0L);
        params.setTypeQNameId(deletedTypeQNameId);
        
        // Find all 'at risk' nodes before the test
        final List<Long> attachedToDeletedIdsBefore = getChildNodesWithDeletedParentNode(params, 0);
        logger.debug("Found child nodes with deleted parent node (before): " + attachedToDeletedIdsBefore);
        final List<Long> orphanedNodeIdsBefore = getChildNodesWithNoParentNode(params, 0);
        logger.debug("Found child nodes without parent (before): " + orphanedNodeIdsBefore);
        
        final NodeRef[] nodeRefs = new NodeRef[10];
        final NodeRef workspaceRootNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        buildNodeHierarchy(workspaceRootNodeRef, nodeRefs);
        
        // Fire off a bunch of threads that create random nodes within the hierarchy created above
        final RetryingTransactionCallback<NodeRef> createChildCallback = new RetryingTransactionCallback<NodeRef>()
        {
            @Override
            public NodeRef execute() throws Throwable
            {
                String randomName = this.getClass().getName() + "-" + GUID.generate();
                QName randomQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, randomName);
                Map<QName, Serializable> props = new HashMap<QName, Serializable>();
                props.put(ContentModel.PROP_NAME, randomName);
                // Choose a random parent node from the hierarchy
                int random = new Random().nextInt(10);
                return nodeService.createNode(
                        nodeRefs[random],
                        ContentModel.ASSOC_CONTAINS,
                        randomQName,
                        ContentModel.TYPE_CONTAINER,
                        props).getChildRef();
            }
        };
        final Runnable[] runnables = new Runnable[20];
        final List<NodeRef> nodesAtRisk = Collections.synchronizedList(new ArrayList<NodeRef>(100));
        
        final List<Thread> threads = new ArrayList<Thread>();
        for (int i = 0; i < runnables.length; i++)
        {
            runnables[i] = new Runnable()
            {
                @Override
                public synchronized void run()
                {
                    AuthenticationUtil.setRunAsUserSystem();
                    try
                    {
                        wait(1000L);     // A short wait before we kick off (should be notified)
                        for (int i = 0; i < 100; i++)
                        {
                            NodeRef nodeRef = txnService.getRetryingTransactionHelper().doInTransaction(createChildCallback);
                            // Store the node for later checks
                            nodesAtRisk.add(nodeRef);
                            // Wait to give other threads a chance
                            wait(1L);
                        }
                    }
                    catch (Throwable e)
                    {
                        // This is expected i.e. we'll just keep doing it until failure
                        logger.debug("Got exception adding child node: ", e);
                    }
                }
            };
            Thread thread = new Thread(runnables[i]);
            threads.add(thread);
            thread.start();
        }
        
        final RetryingTransactionCallback<NodeRef> deleteWithNestedCallback = new RetryingTransactionCallback<NodeRef>()
        {
            @Override
            public NodeRef execute() throws Throwable
            {
                // Notify the threads to kick off
                for (int i = 0; i < runnables.length; i++)
                {
                    // Notify the threads to stop waiting
                    synchronized(runnables[i])
                    {
                        runnables[i].notify();
                    }
                    // Short wait to give thread a chance to run
                    synchronized(this) { try { wait(10L); } catch (Throwable e) {} };
                }
                //add the Temporary aspect to make the deletion faster (it will not be moved to the archival store)
                nodeService.addAspect(nodeRefs[0], ContentModel.ASPECT_TEMPORARY, null);

                // Delete the parent node
                nodeService.deleteNode(nodeRefs[0]);
                return null;
            }
        };
        txnService.getRetryingTransactionHelper().doInTransaction(deleteWithNestedCallback);
        
        // Wait for the threads to finish
        for (Thread t : threads)
        {
            t.join();
        }
        
        logger.info("All threads should have finished");
        
        // Find all 'at risk' nodes after the test
        final List<Long> attachedToDeletedIdsAfter = getChildNodesWithDeletedParentNode(params, attachedToDeletedIdsBefore.size());
        logger.debug("Found child nodes with deleted parent node (after): " + attachedToDeletedIdsAfter);
        final List<Long> orphanedNodeIdsAfter = getChildNodesWithNoParentNode(params, orphanedNodeIdsBefore.size());
        logger.debug("Found child nodes without parent (after): " + attachedToDeletedIdsAfter);
        // Now need to identify the problem nodes

        if (attachedToDeletedIdsAfter.isEmpty() && orphanedNodeIdsAfter.isEmpty())
        {
            // nothing more to test
            return;
        }
        
        // We are already in a failed state, but check if the orphan cleanup works
        
        // workaround recovery: force collection of any orphan nodes (ALF-12358 + ALF-13066)
        for (final NodeRef nodeRef : nodesAtRisk)
        {
            txnService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    if (nodeService.exists(nodeRef))
                    {
                        nodeService.getPath(nodeRef); // ignore return
                    }
                    return null;
                }
            });
        }
        
        // Find all 'at risk' nodes after the test
        final List<Long> attachedToDeletedIdsCleaned = getChildNodesWithDeletedParentNode(params, attachedToDeletedIdsBefore.size());
        logger.debug("Found child nodes with deleted parent node (cleaned): " + attachedToDeletedIdsAfter);
        final List<Long> orphanedNodeIdsCleaned = getChildNodesWithNoParentNode(params, orphanedNodeIdsBefore.size());
        logger.debug("Found child nodes without parent (cleaned): " + attachedToDeletedIdsAfter);
        
        // Check
        assertTrue(
                "Expected full cleanup of nodes referencing deleted nodes: " + attachedToDeletedIdsCleaned,
                attachedToDeletedIdsCleaned.isEmpty());
        assertTrue(
                "Expected full cleanup of nodes referencing without parents: " + orphanedNodeIdsCleaned,
                orphanedNodeIdsCleaned.isEmpty());
        
        // check lost_found ...
        List<NodeRef> lostAndFoundNodeRefs = getLostAndFoundNodes();
        assertFalse(lostAndFoundNodeRefs.isEmpty());
        
        Set<Long> lostAndFoundNodeIds = new HashSet<Long>(lostAndFoundNodeRefs.size());
        for (NodeRef nodeRef : lostAndFoundNodeRefs)
        {
            lostAndFoundNodeIds.add((Long)nodeService.getProperty(nodeRef, ContentModel.PROP_NODE_DBID));
        }
        
        assertTrue("Nodes linked to deleted parent nodes not handled.", lostAndFoundNodeIds.containsAll(attachedToDeletedIdsAfter));
        assertTrue("Orphaned nodes not all handled.", lostAndFoundNodeIds.containsAll(orphanedNodeIdsAfter));
        
        // Now fail because we allowed the situation in the first place
        fail("We allowed orphaned nodes or nodes with deleted parents.");
    }
    
    /**
     * Test for MNT-8494 - we should be able to recover when indexing encounters a node with deleted ancestors
     */
    @Test public void testLinkToDeletedNodeRecovery() throws Throwable
    {
        // First find any broken links to start with
        final NodeEntity params = new NodeEntity();
        params.setId(0L);
        params.setTypeQNameId(deletedTypeQNameId);

        List<Long> nodesWithDeletedParents = getChildNodesWithDeletedParentNode(params, 0);
        List<Long> deletedChildren = getDeletedChildren(params, 0);
        List<Long> nodesWithNoParents = getChildNodesWithNoParentNode(params, 0);

        logger.debug("Found child nodes with deleted parent node (before): " + nodesWithDeletedParents);

        final NodeRef[] nodeRefs = new NodeRef[10];
        final NodeRef workspaceRootNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        buildNodeHierarchy(workspaceRootNodeRef, nodeRefs);

        int cnt = 5;
        final List<NodeRef> childNodeRefs = new ArrayList<NodeRef>(cnt);

        final NodeDAO nodeDAO = (NodeDAO) APP_CONTEXT_INIT.getApplicationContext().getBean("nodeDAO");

        for (int i = 0; i < cnt; i++)
        {
            // create some pseudo- thumnails
            String randomName = this.getClass().getName() + "-" + System.nanoTime();
            QName randomQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, randomName);
            Map<QName, Serializable> props = new HashMap<QName, Serializable>();
            props.put(ContentModel.PROP_NAME, randomName);

            // Choose a random parent node from the hierarchy
            int random = new Random().nextInt(10);
            NodeRef parentNodeRef = nodeRefs[random];

            NodeRef childNodeRef = nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS, randomQName,
                    ContentModel.TYPE_THUMBNAIL, props).getChildRef();

            childNodeRefs.add(childNodeRef);
        }

        // forcefully delete the root, a random connecting one, and a random leaf
        txnService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                Long nodeId = (Long) nodeService.getProperty(nodeRefs[0], ContentModel.PROP_NODE_DBID);
                nodeDAO.updateNode(nodeId, ContentModel.TYPE_DELETED, null);
                nodeDAO.removeNodeAspects(nodeId);
                nodeDAO.removeNodeProperties(nodeId, nodeDAO.getNodeProperties(nodeId).keySet());
                nodeId = (Long) nodeService.getProperty(nodeRefs[2], ContentModel.PROP_NODE_DBID);
                nodeDAO.updateNode(nodeId, ContentModel.TYPE_DELETED, null);
                nodeDAO.removeNodeAspects(nodeId);
                nodeDAO.removeNodeProperties(nodeId, nodeDAO.getNodeProperties(nodeId).keySet());
                nodeId = (Long) nodeService.getProperty(childNodeRefs.get(childNodeRefs.size() - 1),
                        ContentModel.PROP_NODE_DBID);
                nodeDAO.updateNode(nodeId, ContentModel.TYPE_DELETED, null);
                nodeDAO.removeNodeAspects(nodeId);
                nodeDAO.removeNodeProperties(nodeId, nodeDAO.getNodeProperties(nodeId).keySet());
                return null;
            }
        });

        // Now need to identify the problem nodes
        final List<Long> childNodeIds = getChildNodesWithDeletedParentNode(params, nodesWithDeletedParents.size());
        assertFalse(childNodeIds.isEmpty());
        logger.debug("Found child nodes with deleted parent node (after): " + childNodeIds);

        // Now visit the nodes in reverse order and do indexing-like things
        List<NodeRef> allNodeRefs = new ArrayList<NodeRef>(nodeRefs.length + childNodeRefs.size());
        allNodeRefs.addAll(Arrays.asList(nodeRefs));
        allNodeRefs.addAll(childNodeRefs);
        Collections.reverse(allNodeRefs);
        for (final NodeRef nodeRef : allNodeRefs)
        {
            txnService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    if (nodeService.exists(nodeRef))
                    {
                        try
                        {
                            for (ChildAssociationRef parentRef : nodeService.getParentAssocs(nodeRef))
                            {
                                nodeService.getPath(parentRef.getParentRef());
                            }
                            nodeService.getPath(nodeRef); // ignore return
                        }
                        catch (InvalidNodeRefException e)
                        {
                            throw new ConcurrencyFailureException("Deleted node - should be healed on retry", e);
                        }
                    }
                    return null;
                }
            });
        }
        
        // Let's fix up the deleted child nodes indexing might not spot, but hierarchy traversal (e.g. getChildAssocs)
        // might
        for (final NodeRef nodeRef : allNodeRefs)
        {
            txnService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    nodeDAO.getNodePair(nodeRef);
                    return null;
                }
            });
        }

        // Check again
        List<Long> nodeIds = getDeletedChildren(params, deletedChildren.size());
        assertTrue("The following deleted nodes still have parents: " + nodeIds, nodeIds.isEmpty());
        nodeIds = getChildNodesWithDeletedParentNode(params, nodesWithDeletedParents.size());
        assertTrue("The following child nodes have deleted parent nodes: " + nodeIds, nodeIds.isEmpty());
        nodeIds = getChildNodesWithNoParentNode(params, nodesWithNoParents.size());
        assertTrue("The following child nodes have no parent node: " + nodeIds, nodeIds.isEmpty());

        // check lost_found ...
        final List<NodeRef> lostAndFoundNodeRefs = getLostAndFoundNodes();
        assertFalse(lostAndFoundNodeRefs.isEmpty());

        final List<Long> lostAndFoundNodeIds = new ArrayList<Long>(lostAndFoundNodeRefs.size());
        txnService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                for (NodeRef nodeRef : lostAndFoundNodeRefs)
                {
                    Long nodeId = nodeDAO.getNodePair(nodeRef).getFirst();
                    lostAndFoundNodeIds.add(nodeId);
                }
                return null;
            }
        });

        for (final Long childNodeId : childNodeIds)
        {
            Boolean exists = txnService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Boolean>()
            {
                @Override
                public Boolean execute() throws Throwable
                {
                    return nodeDAO.exists(childNodeId);
                }
            });
            assertTrue("Not found: "+childNodeId, lostAndFoundNodeIds.contains(childNodeId) || !exists);
        }
    }

    /**
     * Pending repeatable test - force issue ALF-ALF-13066 (non-root node with no parent)
     */
    @Test public void testForceNonRootNodeWithNoParentNode() throws Throwable
    {
        // First find any broken links to start with
        final NodeEntity params = new NodeEntity();
        params.setId(0L);
        params.setTypeQNameId(deletedTypeQNameId);
        
        List<Long> ids = getChildNodesWithNoParentNode(params, 0);
        logger.debug("Found child nodes with deleted parent node (before): " + ids);
        
        final int idsToSkip = ids.size();
        
        final NodeRef[] nodeRefs = new NodeRef[10];
        final NodeRef workspaceRootNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        buildNodeHierarchy(workspaceRootNodeRef, nodeRefs);
        
        int cnt = 5;
        List<NodeRef> childNodeRefs = new ArrayList<NodeRef>(cnt);
        
        final NodeDAO nodeDAO = (NodeDAO)APP_CONTEXT_INIT.getApplicationContext().getBean("nodeDAO");
        
        for (int i = 0; i < cnt; i++)
        {
            // create some pseudo- thumnails
            String randomName = this.getClass().getName() + "-" + System.nanoTime();
            QName randomQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, randomName);
            Map<QName, Serializable> props = new HashMap<QName, Serializable>();
            props.put(ContentModel.PROP_NAME, randomName);
            
            // Choose a random parent node from the hierarchy
            int random = new Random().nextInt(10);
            NodeRef parentNodeRef = nodeRefs[random];
            
            NodeRef childNodeRef = nodeService.createNode(
                    parentNodeRef,
                    ContentModel.ASSOC_CONTAINS,
                    randomQName,
                    ContentModel.TYPE_THUMBNAIL,
                    props).getChildRef();
            
            childNodeRefs.add(childNodeRef);
            
            // forcefully remove the primary parent assoc
            final Long childNodeId = (Long)nodeService.getProperty(childNodeRef, ContentModel.PROP_NODE_DBID);
            txnService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    Pair<Long, ChildAssociationRef> assocPair = nodeDAO.getPrimaryParentAssoc(childNodeId);
                    nodeDAO.deleteChildAssoc(assocPair.getFirst());
                    return null;
                }
            });
        }
        
        // Now need to identify the problem nodes
        final List<Long> childNodeIds = getChildNodesWithNoParentNode(params, idsToSkip);
        assertFalse(childNodeIds.isEmpty());
        logger.debug("Found child nodes with deleted parent node (after): " + childNodeIds);
        
        // workaround recovery: force collection of any orphan nodes (ALF-12358 + ALF-13066)
        for (final NodeRef nodeRef : childNodeRefs)
        {
            txnService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    if (nodeService.exists(nodeRef))
                    {
                        nodeService.getPath(nodeRef); // ignore return
                    }
                    return null;
                }
            });
        }
        
        // check again ...
        ids = getChildNodesWithNoParentNode(params, idsToSkip);
        assertTrue("The following child nodes have no parent node: " + ids, ids.isEmpty());
        
        // check lost_found ...
        List<NodeRef> lostAndFoundNodeRefs = getLostAndFoundNodes();
        assertFalse(lostAndFoundNodeRefs.isEmpty());
        
        List<Long> lostAndFoundNodeIds = new ArrayList<Long>(lostAndFoundNodeRefs.size());
        for (NodeRef nodeRef : lostAndFoundNodeRefs)
        {
            lostAndFoundNodeIds.add((Long)nodeService.getProperty(nodeRef, ContentModel.PROP_NODE_DBID));
        }
        
        for (Long childNodeId : childNodeIds)
        {
            assertTrue("Not found: "+childNodeId, lostAndFoundNodeIds.contains(childNodeId) || !nodeDAO.exists(childNodeId));
        }
    }
    
    private List<Long> getChildNodesWithDeletedParentNode(NodeEntity params, int idsToSkip)
    {
        return cannedQueryDAOForTesting.executeQuery(
                "alfresco.query.test",
                "select_NodeServiceTest_testConcurrentLinkToDeletedNode_GetChildNodesWithDeletedParentNodeCannedQuery",
                params,
                idsToSkip,
                Integer.MAX_VALUE);
    }
    
    private List<Long> getChildNodesWithNoParentNode(NodeEntity params, int idsToSkip)
    {
        return cannedQueryDAOForTesting.executeQuery(
                "alfresco.query.test",
                "select_NodeServiceTest_testForceNonRootNodeWithNoParentNode_GetChildNodesWithNoParentNodeCannedQuery",
                params,
                idsToSkip,
                Integer.MAX_VALUE);
    }
    
    private List<Long> getDeletedChildren(NodeEntity params, int idsToSkip)
    {
        return cannedQueryDAOForTesting.executeQuery(
                "alfresco.query.test",
                "select_NodeServiceTest_testLinkToDeletedNodeRecovery_GetDeletedChildrenCannedQuery",
                params,
                idsToSkip,
                Integer.MAX_VALUE);
    }

    private List<NodeRef> getLostAndFoundNodes()
    {
        Set<QName> childNodeTypeQNames = new HashSet<QName>(1);
        childNodeTypeQNames.add(ContentModel.TYPE_LOST_AND_FOUND);
        
        List<ChildAssociationRef> childAssocRefs = nodeService.getChildAssocs(nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE), childNodeTypeQNames);
        
        List<NodeRef> lostNodeRefs = null;
        
        if (childAssocRefs.size() > 0)
        {
            List<ChildAssociationRef> lostNodeChildAssocRefs = nodeService.getChildAssocs(childAssocRefs.get(0).getChildRef());
            lostNodeRefs = new ArrayList<NodeRef>(lostNodeChildAssocRefs.size());
            for(ChildAssociationRef lostNodeChildAssocRef : lostNodeChildAssocRefs)
            {
                lostNodeRefs.add(lostNodeChildAssocRef.getChildRef());
            }
        }
        else
        {
            lostNodeRefs = Collections.emptyList();
        }
        
        return lostNodeRefs;
    }
    
    /**
     * @see NodeHierarchyWalker
     */
    @Test public void testNodeHierarchyWalker() throws Exception
    {
        final NodeRef workspaceRootNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        final NodeRef[] nodes = new NodeRef[6];
        buildNodeHierarchy(workspaceRootNodeRef, nodes);
        // Hook up some associations
        nodeService.addAspect(nodes[1], ContentModel.ASPECT_COPIEDFROM, null);
        nodeService.createAssociation(nodes[1], nodes[0], ContentModel.ASSOC_ORIGINAL);             // Peer n1-n0
        nodeService.addChild(                                                                       // Secondary child n0-n2
                nodes[0],
                nodes[2],
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.ALFRESCO_URI, "testNodeHierarchyWalker"));
        
        // Walk the hierarchy
        NodeHierarchyWalker walker = txnService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeHierarchyWalker>()
        {
            @Override
            public NodeHierarchyWalker execute() throws Throwable
            {
                Pair<Long, NodeRef> parentNodePair = nodeDAO.getNodePair(nodes[0]);
                Pair<Long, ChildAssociationRef> parentAssocPair = nodeDAO.getPrimaryParentAssoc(parentNodePair.getFirst());
                
                NodeHierarchyWalker walker = new NodeHierarchyWalker(nodeDAO);
                walker.walkHierarchy(parentNodePair, parentAssocPair);
                return walker;
            }
        }, true);
        
        List<VisitedNode> nodesLeafFirst = walker.getNodes(true);
        assertEquals("Unexpected number of nodes visited", 6, nodesLeafFirst.size());
        assertEquals("Incorrect order ", nodesLeafFirst.get(0).nodeRef, nodes[5]);
        assertEquals("Incorrect order ", nodesLeafFirst.get(5).nodeRef, nodes[0]);
        List<VisitedNode> nodesParentFirst = walker.getNodes(false);
        assertEquals("Unexpected number of nodes visited", 6, nodesParentFirst.size());
        assertEquals("Incorrect order ", nodesParentFirst.get(0).nodeRef, nodes[0]);
        assertEquals("Incorrect order ", nodesParentFirst.get(5).nodeRef, nodes[5]);
        
        // Check primary parent links
        assertEquals(workspaceRootNodeRef, nodesParentFirst.get(0).primaryParentAssocPair.getSecond().getParentRef());
        assertEquals(nodes[0], nodesParentFirst.get(1).primaryParentAssocPair.getSecond().getParentRef());
        assertEquals(nodes[4], nodesParentFirst.get(5).primaryParentAssocPair.getSecond().getParentRef());
        
        // Check secondary parent links
        assertEquals(0, nodesParentFirst.get(0).secondaryParentAssocs.size());
        assertEquals(nodes[0], nodesParentFirst.get(2).secondaryParentAssocs.get(0).getSecond().getParentRef());
        assertEquals(0, nodesParentFirst.get(1).secondaryParentAssocs.size());
        assertEquals(1, nodesParentFirst.get(2).secondaryParentAssocs.size());
        assertEquals(0, nodesParentFirst.get(3).secondaryParentAssocs.size());
        
        // Check secondary child links
        assertEquals(1, nodesParentFirst.get(0).secondaryChildAssocs.size());
        assertEquals(nodes[2], nodesParentFirst.get(0).secondaryChildAssocs.get(0).getSecond().getChildRef());
        assertEquals(0, nodesParentFirst.get(1).secondaryChildAssocs.size());
        
        // Check target assocs
        assertEquals(0, nodesParentFirst.get(0).targetAssocs.size());
        assertEquals(1, nodesParentFirst.get(1).targetAssocs.size());
        assertEquals(nodes[0], nodesParentFirst.get(1).targetAssocs.get(0).getSecond().getTargetRef());
        
        // Check source assocs
        assertEquals(1, nodesParentFirst.get(0).sourceAssocs.size());
        assertEquals(nodes[1], nodesParentFirst.get(0).sourceAssocs.get(0).getSecond().getSourceRef());
        assertEquals(0, nodesParentFirst.get(1).sourceAssocs.size());
    }
    
    @Test
    public void testCascadeUpdate()
    {
        NodeRef nodeRef1 = nodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, this.getClass().getName()),
                ContentModel.TYPE_CONTAINER).getChildRef();
        
        assertFalse(nodeService.getAspects(nodeRef1).contains(ContentModel.ASPECT_CASCADE_UPDATE));

        Map<QName, Serializable> aspectProps = new HashMap<QName, Serializable>();
        ArrayList<NodeRef> cats = new   ArrayList<NodeRef>();
        cats.add(nodeRef1);
        aspectProps.put(ContentModel.PROP_CATEGORIES, cats);
        nodeService.addAspect(nodeRef1, ContentModel.ASPECT_GEN_CLASSIFIABLE, aspectProps);
        assertTrue(nodeService.getAspects(nodeRef1).contains(ContentModel.ASPECT_GEN_CLASSIFIABLE));
        assertFalse(nodeService.getAspects(nodeRef1).contains(ContentModel.ASPECT_CASCADE_UPDATE));
        
        NodeRef nodeRef2 = nodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, this.getClass().getName()),
                ContentModel.TYPE_CONTAINER).getChildRef();
        
        NodeRef nodeRef3 = nodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, this.getClass().getName()),
                ContentModel.TYPE_CONTAINER).getChildRef();
        
        NodeRef nodeRef4 = nodeService.createNode(
                nodeRef2,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, this.getClass().getName()),
                ContentModel.TYPE_CONTAINER).getChildRef();
        
        assertFalse(nodeService.getAspects(nodeRef2).contains(ContentModel.ASPECT_CASCADE_UPDATE));
        assertFalse(nodeService.getAspects(nodeRef3).contains(ContentModel.ASPECT_CASCADE_UPDATE));
        assertFalse(nodeService.getAspects(nodeRef4).contains(ContentModel.ASPECT_CASCADE_UPDATE));
        
        nodeService.moveNode(nodeRef4, nodeRef3, ContentModel.ASSOC_CHILDREN, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, this.getClass().getName()));
        
        assertFalse(nodeService.getAspects(nodeRef2).contains(ContentModel.ASPECT_CASCADE_UPDATE));
        assertFalse(nodeService.getAspects(nodeRef3).contains(ContentModel.ASPECT_CASCADE_UPDATE));
        assertTrue(nodeService.getAspects(nodeRef4).contains(ContentModel.ASPECT_CASCADE_UPDATE));
        Status status = nodeService.getNodeStatus(nodeRef4);
        Long lastCascadeTx = (Long)nodeService.getProperty(nodeRef4, ContentModel.PROP_CASCADE_TX);
        assertTrue(status.getDbTxnId().equals(lastCascadeTx));
        assertTrue(nodeService.getProperty(nodeRef4, ContentModel.PROP_CASCADE_CRC) != null);
        Long crcIn3 = (Long)nodeService.getProperty(nodeRef4, ContentModel.PROP_CASCADE_CRC);
        
        nodeService.moveNode(nodeRef4, nodeRef2, ContentModel.ASSOC_CHILDREN, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, this.getClass().getName()));
        Long crcIn2 = (Long)nodeService.getProperty(nodeRef4, ContentModel.PROP_CASCADE_CRC);
           
        assertFalse(crcIn2.equals(crcIn3));
        
        
        NodeRef nodeRef5 = nodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "5"),
                ContentModel.TYPE_CONTAINER).getChildRef();
        
        NodeRef nodeRef6 = nodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "6"),
                ContentModel.TYPE_CONTAINER).getChildRef();
        
        NodeRef nodeRef7 = nodeService.createNode(
                nodeRef5,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "7"),
                ContentModel.TYPE_CONTAINER).getChildRef();
        
        NodeRef nodeRef8 = nodeService.createNode(
                nodeRef5,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "8"),
                ContentModel.TYPE_CONTAINER).getChildRef();
        
        assertFalse(nodeService.getAspects(nodeRef5).contains(ContentModel.ASPECT_CASCADE_UPDATE));
        assertFalse(nodeService.getAspects(nodeRef6).contains(ContentModel.ASPECT_CASCADE_UPDATE));
        assertFalse(nodeService.getAspects(nodeRef7).contains(ContentModel.ASPECT_CASCADE_UPDATE));
        assertFalse(nodeService.getAspects(nodeRef8).contains(ContentModel.ASPECT_CASCADE_UPDATE));
        
        nodeService.addChild(nodeRef6, nodeRef7, ContentModel.ASSOC_CHILDREN, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, this.getClass().getName()));
        assertFalse(nodeService.getAspects(nodeRef5).contains(ContentModel.ASPECT_CASCADE_UPDATE));
        assertFalse(nodeService.getAspects(nodeRef6).contains(ContentModel.ASPECT_CASCADE_UPDATE));
        assertTrue(nodeService.getAspects(nodeRef7).contains(ContentModel.ASPECT_CASCADE_UPDATE));
        assertFalse(nodeService.getAspects(nodeRef8).contains(ContentModel.ASPECT_CASCADE_UPDATE));
        
        Long doubleLinkCRC = (Long)nodeService.getProperty(nodeRef7, ContentModel.PROP_CASCADE_CRC);
        assertNotNull(doubleLinkCRC);
        
        nodeService.removeChild(nodeRef6, nodeRef7);
        Long singleLinkCRC = (Long)nodeService.getProperty(nodeRef7, ContentModel.PROP_CASCADE_CRC);
        assertFalse(doubleLinkCRC.equals(singleLinkCRC));
        
        nodeService.addChild(nodeRef6, nodeRef7, ContentModel.ASSOC_CHILDREN, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, this.getClass().getName()));
        Long doubleLinkCRC2 = (Long)nodeService.getProperty(nodeRef7, ContentModel.PROP_CASCADE_CRC);
        assertFalse(singleLinkCRC.equals(doubleLinkCRC2));
        
        nodeService.removeChild(nodeRef6, nodeRef7);
        Long singleLinkCRC2 = (Long)nodeService.getProperty(nodeRef7, ContentModel.PROP_CASCADE_CRC);
        assertFalse(doubleLinkCRC2.equals(singleLinkCRC2));
        
    }

    /**
     * See MNT-20850
     */
    @Test
    public void testUpdateContentPermissionWithoutRestrictions()
    {
        ContentPropertyRestrictionInterceptor contentPropertyRestrictionInterceptor =
                (ContentPropertyRestrictionInterceptor) APP_CONTEXT_INIT.getApplicationContext().getBean("contentPropertyRestrictionInterceptor");

        contentPropertyRestrictionInterceptor.setGlobalContentPropertyRestrictions(false);
        try
        {
            updateContentPermissionCommonWork();
        }
        finally
        {
            contentPropertyRestrictionInterceptor.setGlobalContentPropertyRestrictions(true);
        }
    }

    /**
     * See MNT-20850
     */
    @Test
    public void testUpdateContentPermissionWithRestrictions()
    {
        NodeRef workspaceRootNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        String content = "Some content";
        String userName1 = GUID.generate();
        String userName2 = GUID.generate();
        HashMap<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_USERNAME, userName1);
        personService.createPerson(properties);
        properties.put(ContentModel.PROP_USERNAME, userName2);
        personService.createPerson(properties);

        Map<QName, Serializable> props = new HashMap<>(3);
        props.put(ContentModel.PROP_NAME, GUID.generate());
        NodeRef folder1 = nodeService.createNode(
                workspaceRootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(NAMESPACE, GUID.generate()),
                ContentModel.TYPE_FOLDER,
                props).getChildRef();

        props.put(ContentModel.PROP_NAME, GUID.generate());
        NodeRef folder2 = nodeService.createNode(
                workspaceRootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(NAMESPACE, GUID.generate()),
                ContentModel.TYPE_FOLDER,
                props).getChildRef();

        permissionService.setPermission(folder1, userName1, PermissionService.ALL_PERMISSIONS, true);
        permissionService.setInheritParentPermissions(folder1, false);
        permissionService.setPermission(folder2, userName2, PermissionService.ALL_PERMISSIONS, true);
        permissionService.setInheritParentPermissions(folder2, false);

        ContentData contentProp1 = AuthenticationUtil.runAs(() -> {
            NodeRef nodeRef = createContentNode(folder1);

            // Should be possible to add content via contentService
            addContentToNode(nodeRef);

            try
            {
                AuthenticationUtil.runAs(() -> {
                    contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
                    fail("The content of node1 should not be readable by user 2");
                    return null;
                }, userName2);
            }
            catch (Exception e)
            {
                // expected
                assertTrue("The AccessDeniedException should be thrown.", e instanceof AccessDeniedException);
            }

            return DefaultTypeConverter.INSTANCE.convert(ContentData.class, nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT));
        }, userName1);

        AuthenticationUtil.runAs(() ->
        {
            NodeRef nodeRef = nodeService.createNode(
                    folder2,
                    ContentModel.ASSOC_CONTAINS,
                    QName.createQName(GUID.generate()),
                    ContentModel.TYPE_CONTENT).getChildRef();

            try
            {
                nodeService.setProperty(nodeRef, ContentModel.PROP_CONTENT, contentProp1);
                fail("Should not be possible to call setProperty directly to set content");
            }
            catch (InvalidTypeException ite)
            {
                // expected
            }

            try
            {
                Map<QName, Serializable> testProps = new HashMap<>();
                testProps.put(ContentModel.PROP_CONTENT, contentProp1);
                nodeService.setProperties(nodeRef, testProps);
                fail("Should not be possible to call setProperties directly to set content");
            }
            catch (InvalidTypeException ite)
            {
                // expected
            }

            try
            {
                Map<QName, Serializable> testProps = new HashMap<>();
                testProps.put(ContentModel.PROP_CONTENT, contentProp1);
                nodeService.addProperties(nodeRef, testProps);
                fail("Should not be possible to call addProperties directly to set content");
            }
            catch (InvalidTypeException ite)
            {
                // expected
            }

            try
            {
                Map<QName, Serializable> testProps = new HashMap<>();
                testProps.put(ContentModel.PROP_CONTENT, contentProp1);
                nodeService.addAspect(nodeRef, ContentModel.ASPECT_OWNABLE, testProps);
                fail("Should not be possible to call addAspect directly to set content");
            }
            catch (InvalidTypeException ite)
            {
                // expected
            }

            try
            {
                Map<QName, Serializable> testProps = new HashMap<>();
                testProps.put(ContentModel.PROP_CONTENT, contentProp1);
                createContentNode(folder2, testProps);
                fail("Should not be possible to call createNode directly to set content");
            }
            catch (InvalidTypeException ite)
            {
                // expected
            }

            ContentReader contentReader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
            assertNull("The second node should not have any content (all attempts should fail)", contentReader);
            return null;
        }, userName2);
    }

    /**
     * See MNT-20850
     */
    @Test
    public void testUpdateContentPermissionWithRestrictionsAndWhiteList()
    {
        ContentPropertyRestrictionInterceptor contentPropertyRestrictionInterceptor =
                (ContentPropertyRestrictionInterceptor) APP_CONTEXT_INIT.getApplicationContext().getBean("contentPropertyRestrictionInterceptor");

        contentPropertyRestrictionInterceptor.setGlobalContentPropertyRestrictionWhiteList(this.getClass().getName());
        try
        {
            updateContentPermissionCommonWork();
        }
        finally
        {
            contentPropertyRestrictionInterceptor.setGlobalContentPropertyRestrictionWhiteList("");
        }
    }

    private void updateContentPermissionCommonWork()
    {
        NodeRef workspaceRootNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        String content = "Some content";
        String userName1 = GUID.generate();
        String userName2 = GUID.generate();
        HashMap<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_USERNAME, userName1);
        personService.createPerson(properties);
        properties.put(ContentModel.PROP_USERNAME, userName2);
        personService.createPerson(properties);

        Map<QName, Serializable> props = new HashMap<>(3);
        props.put(ContentModel.PROP_NAME, GUID.generate());
        NodeRef folder1 = nodeService.createNode(
                workspaceRootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(NAMESPACE, GUID.generate()),
                ContentModel.TYPE_FOLDER,
                props).getChildRef();

        props.put(ContentModel.PROP_NAME, GUID.generate());
        NodeRef folder2 = nodeService.createNode(
                workspaceRootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(NAMESPACE, GUID.generate()),
                ContentModel.TYPE_FOLDER,
                props).getChildRef();

        permissionService.setPermission(folder1, userName1, PermissionService.ALL_PERMISSIONS, true);
        permissionService.setInheritParentPermissions(folder1, false);
        permissionService.setPermission(folder2, userName2, PermissionService.ALL_PERMISSIONS, true);
        permissionService.setInheritParentPermissions(folder2, false);


        ContentData contentProp1 = AuthenticationUtil.runAs(() ->
        {
            NodeRef nodeRef = createContentNode(folder1);

            // Should be possible to add content via contentService
            addContentToNode(nodeRef);

            try
            {
                AuthenticationUtil.runAs(() ->
                {
                    contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
                    fail("The content of node1 should not be readable by user 2");
                    return null;
                }, userName2);
            }
            catch (Exception e)
            {
                // expected
                assertTrue("The AccessDeniedException should be thrown.", e instanceof AccessDeniedException);
            }

            return DefaultTypeConverter.INSTANCE.convert(ContentData.class, nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT));
        }, userName1);

        AuthenticationUtil.runAs(() ->
        {
            NodeRef nodeRef = createContentNode(folder2);

            nodeService.setProperty(nodeRef, ContentModel.PROP_CONTENT, contentProp1);
            ContentReader contentReader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
            assertNotNull("The node should have the content set", contentReader);
            assertEquals("The property should be set successfully", content, contentReader.getContentString());

            // reset the property
            nodeService.setProperty(nodeRef, ContentModel.PROP_CONTENT, null);

            Map<QName, Serializable> testProps = new HashMap<>();
            testProps.put(ContentModel.PROP_CONTENT, contentProp1);
            nodeService.setProperties(nodeRef, testProps);
            contentReader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
            assertNotNull("The node should have the content set", contentReader);
            assertEquals("The property should be set successfully", content, contentReader.getContentString());

            // reset the property
            nodeService.setProperty(nodeRef, ContentModel.PROP_CONTENT, null);

            testProps = new HashMap<>();
            testProps.put(ContentModel.PROP_CONTENT, contentProp1);
            nodeService.addAspect(nodeRef, ContentModel.ASPECT_OWNABLE, testProps);
            contentReader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
            assertNotNull("The node should have the content set", contentReader);
            assertEquals("The property should be set successfully", content, contentReader.getContentString());

            // reset the property
            nodeService.setProperty(nodeRef, ContentModel.PROP_CONTENT, null);

            testProps.put(ContentModel.PROP_CONTENT, contentProp1);
            nodeService.addProperties(nodeRef, testProps);
            contentReader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
            assertNotNull("The node should have the content set", contentReader);
            assertEquals("The property should be set successfully", content, contentReader.getContentString());

            testProps.put(ContentModel.PROP_CONTENT, contentProp1);
            NodeRef newNode = createContentNode(folder2, testProps);
            contentReader = contentService.getReader(newNode, ContentModel.PROP_CONTENT);
            assertNotNull("The node should have the content set", contentReader);
            assertEquals("The property should be set successfully", content, contentReader.getContentString());

            contentReader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
            assertNotNull("The second node should not have any content (all attempts should fail)", contentReader);
            return null;
        }, userName2);
    }

    /**
     * See MNT-20850
     */
    @Test
    public void testSetContentPropertiesWithoutModification()
    {
        NodeRef workspaceRootNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        String userName1 = GUID.generate();
        HashMap<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_USERNAME, userName1);
        personService.createPerson(properties);

        Map<QName, Serializable> props = new HashMap<>(3);
        props.put(ContentModel.PROP_NAME, GUID.generate());
        NodeRef folder1 = nodeService.createNode(
                workspaceRootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(NAMESPACE, GUID.generate()),
                ContentModel.TYPE_FOLDER,
                props).getChildRef();

        permissionService.setPermission(folder1, userName1, PermissionService.ALL_PERMISSIONS, true);
        permissionService.setInheritParentPermissions(folder1, false);

        AuthenticationUtil.runAs(() ->
        {
            NodeRef nodeRef = createContentNode(folder1);

            // Should be possible to add content via contentService
            addContentToNode(nodeRef);

            Serializable contentProp = nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);

            Map<QName, Serializable> existingProps = nodeService.getProperties(nodeRef);
            String description = "Some description";
            existingProps.put(ContentModel.PROP_DESCRIPTION, description);

            nodeService.setProperties(nodeRef, existingProps);
            assertEquals("Additional property should be set", description, nodeService.getProperty(nodeRef, ContentModel.PROP_DESCRIPTION));

            existingProps = new HashMap<>(2);
            existingProps.put(ContentModel.PROP_CONTENT, contentProp); // same prop
            String title = "Some title";
            existingProps.put(ContentModel.PROP_TITLE, title);

            nodeService.addProperties(nodeRef, existingProps);
            assertEquals("Additional property should be set", title, nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE));

            nodeService.setProperty(nodeRef, ContentModel.PROP_CONTENT, contentProp);

            Map<QName, Serializable> aspectProps = new HashMap<>();
            aspectProps.put(ContentModel.PROP_CONTENT, contentProp);
            nodeService.addAspect(nodeRef, ContentModel.ASPECT_OWNABLE, aspectProps);
            assertTrue("Aspect should be added", nodeService.hasAspect(nodeRef, ContentModel.ASPECT_OWNABLE));

            return null;
        }, userName1);
    }

    /**
     * See MNT-20850
     */
    @Test
    public void testSetContentPropertyToNull()
    {
        NodeRef workspaceRootNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        ContentData nullContentData = new ContentData(null, "text/plain", 0L, "UTF-8", Locale.ENGLISH);
        ContentData emptyContentData = new ContentData("", "text/plain", 0L, "UTF-8", Locale.ENGLISH);
        String userName1 = GUID.generate();
        HashMap<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_USERNAME, userName1);
        personService.createPerson(properties);

        Map<QName, Serializable> props = new HashMap<>(3);
        props.put(ContentModel.PROP_NAME, GUID.generate());
        NodeRef folder1 = nodeService.createNode(
                workspaceRootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(NAMESPACE, GUID.generate()),
                ContentModel.TYPE_FOLDER,
                props).getChildRef();

        permissionService.setPermission(folder1, userName1, PermissionService.ALL_PERMISSIONS, true);
        permissionService.setInheritParentPermissions(folder1, false);

        AuthenticationUtil.runAs(() ->
        {
            NodeRef nodeRef = createContentNode(folder1);

            nodeService.setProperty(nodeRef, ContentModel.PROP_CONTENT, emptyContentData);
            assertEquals("The content property was not correct.", emptyContentData, nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT));
            nodeService.setProperty(nodeRef, ContentModel.PROP_CONTENT, nullContentData);
            assertEquals("The content property was not correct.", nullContentData, nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT));
            nodeService.setProperty(nodeRef, ContentModel.PROP_CONTENT, null);
            assertNull("The content property was not correct.", nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT));

            nodeRef = createContentNode(folder1);

            Map<QName, Serializable> existingProps = nodeService.getProperties(nodeRef);
            String description = "Some description 1";
            existingProps.put(ContentModel.PROP_DESCRIPTION, description);
            existingProps.put(ContentModel.PROP_CONTENT, emptyContentData);
            nodeService.setProperties(nodeRef, existingProps);
            assertEquals("Additional property should be set", description, nodeService.getProperty(nodeRef, ContentModel.PROP_DESCRIPTION));
            assertEquals("The content property was not correct.", emptyContentData, nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT));

            nodeRef = createContentNode(folder1);

            existingProps = nodeService.getProperties(nodeRef);
            description = "Some description 2";
            existingProps.put(ContentModel.PROP_DESCRIPTION, description);
            existingProps.put(ContentModel.PROP_CONTENT, nullContentData);
            nodeService.setProperties(nodeRef, existingProps);
            assertEquals("Additional property should be set", description, nodeService.getProperty(nodeRef, ContentModel.PROP_DESCRIPTION));
            assertEquals("The content property was not correct.", nullContentData, nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT));

            nodeRef = createContentNode(folder1);

            existingProps = nodeService.getProperties(nodeRef);
            description = "Some description 3";
            existingProps.put(ContentModel.PROP_DESCRIPTION, description);
            existingProps.put(ContentModel.PROP_CONTENT, null);
            nodeService.setProperties(nodeRef, existingProps);
            assertEquals("Additional property should be set", description, nodeService.getProperty(nodeRef, ContentModel.PROP_DESCRIPTION));
            assertNull("The content property was not correct.", nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT));

            nodeRef = createContentNode(folder1);

            existingProps = new HashMap<>(2);
            existingProps.put(ContentModel.PROP_CONTENT, emptyContentData);
            String title = "Some title 1";
            existingProps.put(ContentModel.PROP_TITLE, title);

            nodeService.addProperties(nodeRef, existingProps);
            assertEquals("Additional property should be set", title, nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE));
            assertEquals("The content property was not correct.", emptyContentData, nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT));

            nodeRef = createContentNode(folder1);

            existingProps = new HashMap<>(2);
            existingProps.put(ContentModel.PROP_CONTENT, nullContentData);
            title = "Some title 2";
            existingProps.put(ContentModel.PROP_TITLE, title);

            nodeService.addProperties(nodeRef, existingProps);
            assertEquals("Additional property should be set", title, nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE));
            assertEquals("The content property was not correct.", nullContentData, nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT));

            nodeRef = createContentNode(folder1);

            existingProps = new HashMap<>(2);
            existingProps.put(ContentModel.PROP_CONTENT, null);
            title = "Some title 3";
            existingProps.put(ContentModel.PROP_TITLE, title);

            nodeService.addProperties(nodeRef, existingProps);
            assertEquals("Additional property should be set", title, nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE));
            assertNull("The content property was not correct.", nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT));

            nodeRef = createContentNode(folder1);

            Map<QName, Serializable> aspectProps = new HashMap<>();
            aspectProps.put(ContentModel.PROP_CONTENT, emptyContentData);
            nodeService.addAspect(nodeRef, ContentModel.ASPECT_OWNABLE, aspectProps);
            assertTrue("Aspect should be added", nodeService.hasAspect(nodeRef, ContentModel.ASPECT_OWNABLE));
            assertEquals("The content property was not correct.", emptyContentData, nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT));

            nodeRef = createContentNode(folder1);

            aspectProps = new HashMap<>();
            aspectProps.put(ContentModel.PROP_CONTENT, nullContentData);
            nodeService.addAspect(nodeRef, ContentModel.ASPECT_OWNABLE, aspectProps);
            assertTrue("Aspect should be added", nodeService.hasAspect(nodeRef, ContentModel.ASPECT_OWNABLE));
            assertEquals("The content property was not correct.", nullContentData, nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT));

            nodeRef = createContentNode(folder1);

            aspectProps = new HashMap<>();
            aspectProps.put(ContentModel.PROP_CONTENT, null);
            nodeService.addAspect(nodeRef, ContentModel.ASPECT_OWNABLE, aspectProps);
            assertTrue("Aspect should be added", nodeService.hasAspect(nodeRef, ContentModel.ASPECT_OWNABLE));
            assertNull("The content property was not correct.", nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT));

            return null;
        }, userName1);
    }

    /**
     * See MNT-20850
     */
    @Test
    public void testChangeContentPropertyParameters()
    {
        NodeRef workspaceRootNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        String userName1 = GUID.generate();
        HashMap<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_USERNAME, userName1);
        personService.createPerson(properties);

        Map<QName, Serializable> props = new HashMap<>(3);
        props.put(ContentModel.PROP_NAME, GUID.generate());
        NodeRef folder1 = nodeService.createNode(
                workspaceRootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(NAMESPACE, GUID.generate()),
                ContentModel.TYPE_FOLDER,
                props).getChildRef();

        permissionService.setPermission(folder1, userName1, PermissionService.ALL_PERMISSIONS, true);
        permissionService.setInheritParentPermissions(folder1, false);

        AuthenticationUtil.runAs(() ->
        {
            NodeRef nodeRef = createContentNode(folder1);
            ContentData oldContentData = (ContentData) nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
            ContentData newContentData = new ContentData(oldContentData.getContentUrl(), MimetypeMap.MIMETYPE_PDF,
                    oldContentData.getSize(), oldContentData.getEncoding(), oldContentData.getLocale());
            nodeService.setProperty(nodeRef, ContentModel.PROP_CONTENT, newContentData);
            assertEquals("The content property was not correct.", newContentData, nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT));

            nodeRef = createContentNode(folder1);
            oldContentData = (ContentData) nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
            newContentData = new ContentData(oldContentData.getContentUrl(), oldContentData.getMimetype(),
                    oldContentData.getSize() + 1, oldContentData.getEncoding(), oldContentData.getLocale());
            nodeService.setProperty(nodeRef, ContentModel.PROP_CONTENT, newContentData);
            assertEquals("The content property was not correct.", newContentData, nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT));

            nodeRef = createContentNode(folder1);
            oldContentData = (ContentData) nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
            newContentData = new ContentData(oldContentData.getContentUrl(), oldContentData.getMimetype(),
                    oldContentData.getSize(), "UTF-16", oldContentData.getLocale());
            nodeService.setProperty(nodeRef, ContentModel.PROP_CONTENT, newContentData);
            assertEquals("The content property was not correct.", newContentData, nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT));

            nodeRef = createContentNode(folder1);
            oldContentData = (ContentData) nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
            newContentData = new ContentData(oldContentData.getContentUrl(), oldContentData.getMimetype(),
                    oldContentData.getSize(), oldContentData.getEncoding(), Locale.GERMAN);
            nodeService.setProperty(nodeRef, ContentModel.PROP_CONTENT, newContentData);
            assertEquals("The content property was not correct.", newContentData, nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT));

            try
            {
                nodeRef = createContentNode(folder1);
                oldContentData = (ContentData) nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
                newContentData = new ContentData("fake://url/123", oldContentData.getMimetype(),
                        oldContentData.getSize(), oldContentData.getEncoding(), oldContentData.getLocale());
                nodeService.setProperty(nodeRef, ContentModel.PROP_CONTENT, newContentData);
                fail("Should not be possible to change content URL");
            }
            catch (InvalidTypeException ite)
            {
                // expected
            }

            return null;
        }, userName1);
    }
    
    /**
     * See MNT-22710
     */
    @Test
    public void testChangeContentURLSameCRC()
    {
        ContentPropertyRestrictionInterceptor contentPropertyRestrictionInterceptor =
                (ContentPropertyRestrictionInterceptor) APP_CONTEXT_INIT.getApplicationContext().getBean("contentPropertyRestrictionInterceptor");

        contentPropertyRestrictionInterceptor.setGlobalContentPropertyRestrictionWhiteList(this.getClass().getName());
        
        try
        {
            NodeRef workspaceRootNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
            Map<QName, Serializable> props = new HashMap<>(3);
            props.put(ContentModel.PROP_NAME, GUID.generate());
            NodeRef testFolder = nodeService.createNode(
                    workspaceRootNodeRef,
                    ContentModel.ASSOC_CHILDREN,
                    QName.createQName(NAMESPACE, GUID.generate()),
                    ContentModel.TYPE_FOLDER,
                    props).getChildRef();
             
            NodeRef nodeRef1 = nodeService.createNode(
                    testFolder,
                    ContentModel.ASSOC_CONTAINS,
                    QName.createQName("180225704974-DSA Correspondence 1"),
                    ContentModel.TYPE_CONTENT).getChildRef();
            
            NodeRef nodeRef2 = nodeService.createNode(
                    testFolder,
                    ContentModel.ASSOC_CONTAINS,
                    QName.createQName("090502800600-General Correspondence 1"),
                    ContentModel.TYPE_CONTENT).getChildRef();
            
            // CRC: 2997538036
            String contentURL1 = "s3v2://contentstore/1597325412593_1610985343942_3708/SLC/IC/DSA/2018/02/16/20/180225704974-DSA Correspondence 1";
            // CRC: 2921900407
            String contentURL2 = "s3v2://contentstore/1639588316822_1642775862393_49/SLC/IC/GENERAL/2009/05/27/16/090502800600-General Correspondence 1";
            // CRC: 2997538036 (same CRC as contentURL1)
            String contentURL3 = "s3v2://contentstore/1639588316821_1642775862393_49/SLC/IC/GENERAL/2009/05/27/16/090502800600-General Correspondence 1";
            
            ContentData contentData1 = new ContentData(
                    contentURL1, 
                    "application/pdf",
                    100L, 
                    "UTF-8"
                    );
            
            ContentData contentData2 = new ContentData(
                    contentURL2, 
                    "application/pdf",
                    100L, 
                    "UTF-8"
                    );
            
            ContentData contentData3 = new ContentData(
                    contentURL3, 
                    "application/pdf",
                    100L, 
                    "UTF-8"
                    );
            
            // Setting same contentURLs
            nodeService.setProperty(nodeRef1, ContentModel.PROP_CONTENT, contentData1);
            nodeService.setProperty(nodeRef2, ContentModel.PROP_CONTENT, contentData1);
            
            //Validate the data
            ContentData cdNode1Val0= (ContentData) nodeService.getProperty(nodeRef1, ContentModel.PROP_CONTENT);
            ContentData cdNode2Val0= (ContentData) nodeService.getProperty(nodeRef2, ContentModel.PROP_CONTENT);
            assertEquals("ContentURL for node1 should be contentURL1",contentURL1,cdNode1Val0.getContentUrl());
            assertEquals("ContentURL for node2 should also be the same as node1",contentURL1,cdNode2Val0.getContentUrl());
            
            // Setting non colliding URLS
            nodeService.setProperty(nodeRef2, ContentModel.PROP_CONTENT, contentData2);
            
            //Validate the data
            ContentData cdNode1Val1= (ContentData) nodeService.getProperty(nodeRef1, ContentModel.PROP_CONTENT);
            ContentData cdNode2Val1= (ContentData) nodeService.getProperty(nodeRef2, ContentModel.PROP_CONTENT);
            assertEquals("ContentURL for node1 should be contentURL1",contentURL1,cdNode1Val1.getContentUrl());
            assertEquals("ContentURL for node2 should be contentURL2",contentURL2,cdNode2Val1.getContentUrl());
            
            //Set a colliding URL
            try
            {
                nodeService.setProperty(nodeRef2, ContentModel.PROP_CONTENT, contentData3);
                fail("Should not be possible to set a contentUrl with the same CRC and different value");
            }
            catch (IllegalArgumentException e)
            {
                //Expected
            }
            
            //Validate the contentURL values
            ContentData cdNode1Val2= (ContentData) nodeService.getProperty(nodeRef1, ContentModel.PROP_CONTENT);
            ContentData cdNode2Val2= (ContentData) nodeService.getProperty(nodeRef2, ContentModel.PROP_CONTENT);
            assertFalse("Collision detected on node 1",contentURL3.equals(cdNode1Val2.getContentUrl()));
            assertFalse("Collision detected on node 2",contentURL1.equals(cdNode2Val2.getContentUrl()));
            
            //Clear caches and validate again
            propsCache.clear();
            contentDataCache.clear();
            contentUrlCache.clear();
            
            ContentData cdNode1Val3= (ContentData) nodeService.getProperty(nodeRef1, ContentModel.PROP_CONTENT);
            ContentData cdNode2Val3= (ContentData) nodeService.getProperty(nodeRef2, ContentModel.PROP_CONTENT);
            assertFalse("Collision detected on node 1 after clear caches",contentURL3.equals(cdNode1Val3.getContentUrl()));
            assertFalse("Collision detected on node 2 after clear caches",contentURL1.equals(cdNode2Val3.getContentUrl()));
            
            assertEquals("ContentURL for node1 should be contentURL1",contentURL1,cdNode1Val3.getContentUrl());
            assertEquals("ContentURL for node2 should be contentURL2",contentURL2,cdNode2Val3.getContentUrl());
        }
        finally
        {
            contentPropertyRestrictionInterceptor.setGlobalContentPropertyRestrictionWhiteList("");
        }
        
    }

    private void addContentToNode(NodeRef nodeRef)
    {
        String content = "Some content";
        ContentWriter contentWriter = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
        contentWriter.setMimetype("text/plain");
        contentWriter.setEncoding("UTF-8");
        contentWriter.putContent(content);
    }

    private NodeRef createContentNode(NodeRef parentRef)
    {
        NodeRef nodeRef = nodeService.createNode(
                parentRef,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(GUID.generate()),
                ContentModel.TYPE_CONTENT).getChildRef();
        addContentToNode(nodeRef);
        return nodeRef;
    }
    private NodeRef createContentNode(NodeRef parentRef, Map<QName, Serializable> properties)
    {
        return nodeService.createNode(
                parentRef,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(GUID.generate()),
                ContentModel.TYPE_CONTENT,
                properties).getChildRef();
    }
}
