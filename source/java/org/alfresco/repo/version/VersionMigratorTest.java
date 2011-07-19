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
package org.alfresco.repo.version;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.integrity.IntegrityChecker;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.EqualsHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Test simple version store migration
 */
public class VersionMigratorTest extends BaseVersionStoreTest
{
    private static Log logger = LogFactory.getLog(VersionMigratorTest.class);

    protected VersionServiceImpl version1Service = new VersionServiceImpl();
    
    protected Version2ServiceImpl version2Service;
    protected NodeService versionNodeService;
    
    protected VersionMigrator versionMigrator;
    protected PolicyComponent policyComponent;
    protected DictionaryService dictionaryService;
    protected CheckOutCheckInService cociService;
    protected IntegrityChecker integrityChecker;
    
    public VersionMigratorTest()
    {
        //super.setDefaultRollback(false); // default is true
    }
    
    protected void onSetUpInTransaction() throws Exception
    {
        super.onSetUpInTransaction();
        
        this.versionMigrator = (VersionMigrator)applicationContext.getBean("versionMigrator");
        this.policyComponent = (PolicyComponent)applicationContext.getBean("policyComponent");
        this.dictionaryService = (DictionaryService)applicationContext.getBean("dictionaryService");
        this.version2Service = (Version2ServiceImpl)applicationContext.getBean("versionService");
        this.versionNodeService = (NodeService)applicationContext.getBean("versionNodeService"); // note: auto-switches between V1 and V2
        
        this.cociService = (CheckOutCheckInService)applicationContext.getBean("CheckoutCheckinService");
        this.integrityChecker = (IntegrityChecker)applicationContext.getBean("integrityChecker");
        
        // Version1Service is used to create the version nodes in Version1Store (workspace://lightWeightVersionStore) 
        version1Service.setDbNodeService(dbNodeService);
        version1Service.setNodeService(dbNodeService);
        version1Service.setPolicyComponent(policyComponent);
        version1Service.setDictionaryService(dictionaryService);
        version1Service.initialiseWithoutBind(); // TODO - temp - if use intialise, get: "More than one CalculateVersionLabelPolicy behaviour has been registered for the type {http://www.alfresco.org/model/content/1.0}content"
    
        super.setVersionService(version1Service);
    }
    
	/**
     * Test migration of a simple versioned node (one version, no children)
     */
    public void testMigrateOneVersion() throws Exception
    {
        if (version2Service.useDeprecatedV1 == true)
        {
            logger.info("testMigrateOneVersion: skip");
            return;
        }
        
        NodeRef versionableNode = createNewVersionableNode();
        
        logger.info("testMigrateOneVersion: versionedNodeRef = " + versionableNode);
        
        // Get the next version label
        String nextVersionLabel = peekNextVersionLabel(versionableNode, versionProperties);
        
        // Snap-shot the node created date-time
        Date beforeVersionDate = (Date)nodeService.getProperty(versionableNode, ContentModel.PROP_CREATED);
        long beforeVersionTime = beforeVersionDate.getTime();
        logger.info("beforeVersion Date/Time: " + beforeVersionDate + " [" + beforeVersionTime + "]");
        
        Version oldVersion = createVersion(versionableNode);
        
        // get and store old version details for later comparison - versionNodeService will retrieve these from the old version store
        
        QName oldVersionType = versionNodeService.getType(oldVersion.getFrozenStateNodeRef());
        Set<QName> oldVersionAspects = versionNodeService.getAspects(oldVersion.getFrozenStateNodeRef());
        Map<QName, Serializable> oldVersionProps = versionNodeService.getProperties(oldVersion.getFrozenStateNodeRef());
        
        logger.info("oldVersion props: " + oldVersion);
        logger.info("oldVersion created: " + oldVersion.getFrozenModifiedDate() + " [" + oldVersion.getFrozenModifiedDate().getTime()+"]");
        
        logger.info("oldVersion props via versionNodeService: " + oldVersionProps);
        
        VersionHistory vh = version1Service.getVersionHistory(versionableNode);
        assertEquals(1, vh.getAllVersions().size());
         
        NodeRef oldVHNodeRef = version1Service.getVersionHistoryNodeRef(versionableNode);
        
        Thread.sleep(70000);

        // Migrate and delete old version history !
        NodeRef versionedNodeRef = versionMigrator.v1GetVersionedNodeRef(oldVHNodeRef);
        NodeRef newVHNodeRef = versionMigrator.migrateVersionHistory(oldVHNodeRef, versionedNodeRef);
        versionMigrator.v1DeleteVersionHistory(oldVHNodeRef);
        
        VersionHistory vh2 = version2Service.getVersionHistory(versionableNode);
        assertEquals(1, vh2.getAllVersions().size());
        
        Version newVersion = vh2.getRootVersion();
        
        logger.info("newVersion props: " + newVersion);
        logger.info("newVersion created: " + newVersion.getFrozenModifiedDate() + " [" + newVersion.getFrozenModifiedDate().getTime()+"]");
        
        // check new version - switch to new version service to do the check 
        super.setVersionService(version2Service);
        checkNewVersion(beforeVersionTime, nextVersionLabel, newVersion, versionableNode);
        
        // get and compare new version details - - versionNodeService will retrieve these from the new version store
        
        QName newVersionType = versionNodeService.getType(newVersion.getFrozenStateNodeRef());
        Set<QName> newVersionAspects = versionNodeService.getAspects(newVersion.getFrozenStateNodeRef());
        Map<QName, Serializable> newVersionProps = versionNodeService.getProperties(newVersion.getFrozenStateNodeRef());
        
        logger.info("newVersion props via versionNodeService: " + newVersionProps);
        
        assertEquals(oldVersionType, newVersionType);
        
        assertEquals(oldVersionAspects.size(), newVersionAspects.size());
        for (QName key : oldVersionAspects)
        {
            assertTrue(""+key, newVersionAspects.contains(key));
        }
        
        // note: since 3.4, "cm:accessed" is not returned/migrated if null
        int expectedPropCount = oldVersionProps.size();
        
        if (oldVersionProps.get(ContentModel.PROP_ACCESSED) == null)
        {
            expectedPropCount--;
        }
        
        assertEquals(expectedPropCount, newVersionProps.size());
        for (QName key : oldVersionProps.keySet())
        {
            if (! (key.equals(ContentModel.PROP_ACCESSED) && (oldVersionProps.get(key) == null)))
            {
                assertEquals(""+key, oldVersionProps.get(key), newVersionProps.get(key));
            }
        }
        
        // ALFCOM-2658
        assertEquals(oldVersion.getFrozenStateNodeRef().getId(), newVersion.getFrozenStateNodeRef().getId());
        
        logger.info("testMigrateOneVersion: Migrated from oldVHNodeRef = " + oldVHNodeRef + " to newVHNodeRef = " + newVHNodeRef);
    }    
    
    /**
     * Test migration of a multiple versioned nodes
     */
    public void testMigrateMultipleVersions() throws Exception
    {
        if (version2Service.useDeprecatedV1 == true)
        {
            logger.info("testMigrateOneVersion: skip");
            return;
        }
        
        NodeRef versionableNode = createNewVersionableNode();
        
        // Get the next version label and snapshot the date-time
        String nextVersionLabel1 = peekNextVersionLabel(versionableNode, versionProperties);
        
        // Snap-shot the node created date-time
        long beforeVersionTime1 = ((Date)nodeService.getProperty(versionableNode, ContentModel.PROP_CREATED)).getTime();
        
        Version version1 = createVersion(versionableNode);
        logger.info(version1);
        
        // Get the next version label and snapshot the date-time
        String nextVersionLabel2 = peekNextVersionLabel(versionableNode, versionProperties);
        
        // Snap-shot the node created date-time
        long beforeVersionTime2 = ((Date)nodeService.getProperty(versionableNode, ContentModel.PROP_CREATED)).getTime();
        
        Version version2 = createVersion(versionableNode);
        logger.info(version2);
        
        // Get the next version label and snapshot the date-time
        String nextVersionLabel3 = peekNextVersionLabel(versionableNode, versionProperties);
        
        // Snap-shot the node created date-time
        long beforeVersionTime3 = ((Date)nodeService.getProperty(versionableNode, ContentModel.PROP_CREATED)).getTime();
        
        Version version3 = createVersion(versionableNode);
        logger.info(version3);
        
        VersionHistory vh1 = version1Service.getVersionHistory(versionableNode);
        assertEquals(3, vh1.getAllVersions().size());
        
        logger.info("testMigrateMultipleVersions: versionedNodeRef = " + versionableNode);
        
        NodeRef oldVHNodeRef = version1Service.getVersionHistoryNodeRef(versionableNode);

        // Migrate and delete old version history !
        NodeRef versionedNodeRef = versionMigrator.v1GetVersionedNodeRef(oldVHNodeRef);
        NodeRef newVHNodeRef = versionMigrator.migrateVersionHistory(oldVHNodeRef, versionedNodeRef);
        versionMigrator.v1DeleteVersionHistory(oldVHNodeRef);
        
        VersionHistory vh2 = version2Service.getVersionHistory(versionableNode);
        assertEquals(3, vh2.getAllVersions().size());
        
        // TODO move check version history into BaseVersionStoreTest
        // check new versions - switch to new version service to do the check
        super.setVersionService(version2Service);
        
        Version[] newVersions = vh2.getAllVersions().toArray(new Version[]{});
        
        checkVersion(beforeVersionTime1, nextVersionLabel1, newVersions[2], versionableNode);
        checkVersion(beforeVersionTime2, nextVersionLabel2, newVersions[1], versionableNode);
        checkNewVersion(beforeVersionTime3, nextVersionLabel3, newVersions[0], versionableNode);
        
        // ALFCOM-2658
        assertEquals(version1.getFrozenStateNodeRef().getId(), newVersions[2].getFrozenStateNodeRef().getId());
        assertEquals(version2.getFrozenStateNodeRef().getId(), newVersions[1].getFrozenStateNodeRef().getId());
        assertEquals(version3.getFrozenStateNodeRef().getId(), newVersions[0].getFrozenStateNodeRef().getId());
        
        logger.info("testMigrateMultipleVersions: Migrated from oldVHNodeRef = " + oldVHNodeRef + " to newVHNodeRef = " + newVHNodeRef);
    }
    
    public void testMigrateMultipleNodesSuccessful() throws Exception
    {
        testMigrateMultipleNodes(false);
    }
    
    public void test_ETHREEOH_2091() throws Exception
    {
        // test partial migration (with skipped nodes)
        testMigrateMultipleNodes(true);
    }
    
    /**
     * Test migration of a multiple nodes (each with one version)
     */
    private void testMigrateMultipleNodes(final boolean withSkip)
    {
        if (version2Service.useDeprecatedV1 == true)
        {
            logger.info("testMigrateOneVersion: skip");
            return;
        }
        
        final int nodeCount = 5;
        assert(nodeCount > 3);
        
        final NodeRef[] versionableNodes = new NodeRef[nodeCount];
        
        setComplete();
        endTransaction();
        
        RetryingTransactionHelper txHelper = transactionService.getRetryingTransactionHelper();
        
        for (int i = 0; i < nodeCount; i++)
        {
            final int idx = i;
            
            txHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>()
            {
                public NodeRef execute() throws Throwable
                {
                    NodeRef versionableNode = null;
                    if ((idx % 2) == 0)
                    {
                        versionableNode = createNewVersionableNode();
                    }
                    else
                    {
                        versionableNode = createNewVersionableContentNode(true);
                    }
                    createVersion(versionableNode);
                    versionableNodes[idx] = versionableNode;
                    
                    return null;
                }
            });
        }
        
        setComplete();
        endTransaction();
        
        txHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                // check old version histories
                for (int i = 0; i< nodeCount; i++)
                {
                    VersionHistory vh1 = version1Service.getVersionHistory(versionableNodes[i]);
                    assertNotNull(vh1);
                    assertEquals(1, vh1.getAllVersions().size());
                }
                
                return null;
            }
        });
        
        setComplete();
        endTransaction();
        
        if (withSkip)
        {
            // remove test model - those nodes should fail - currently all - add separate create ...
            
            // TODO ...
            dictionaryDAO.removeModel(QName.createQName("http://www.alfresco.org/test/versionstorebasetest/1.0", "versionstorebasetestmodel"));
        }
        
        txHelper = transactionService.getRetryingTransactionHelper();
        
        txHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                // Migrate (and don't delete old version history) !
                versionMigrator.migrateVersions(1, 1, -1, false, null, false);
                
                return null;
            }
        });
        
        setComplete();
        endTransaction();
        
        txHelper.doInTransaction(new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Throwable
            {
                // check new version histories
                for (int i = 0; i < nodeCount; i++)
                {
                    VersionHistory vh2 = version2Service.getVersionHistory(versionableNodes[i]);
                    
                    if (withSkip && ((i % 2) == 0))
                    {
                        assertNull(vh2);
                    }
                    else
                    {
                        assertNotNull(vh2);
                        assertEquals(1, vh2.getAllVersions().size());
                    }
                }
                
                return null;
            }
        });
    }
    
    private NodeRef createNewVersionableContentNode(boolean versionable)
    {
        // Use this map to retrieve the versionable nodes in later tests
        this.versionableNodes = new HashMap<String, NodeRef>();
        
        // Create node (this node has some content)
        NodeRef nodeRef = this.dbNodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "myNode"),
                ContentModel.TYPE_CONTENT,
                this.nodeProperties).getChildRef();
        
        if (versionable)
        {
            this.dbNodeService.addAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE, new HashMap<QName, Serializable>());
        }
        
        assertNotNull(nodeRef);
        this.versionableNodes.put(nodeRef.getId(), nodeRef);
        
        // Add the content to the node
        ContentWriter contentWriter = this.contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
        contentWriter.putContent(TEST_CONTENT);
        
        // Set author
        Map<QName, Serializable> authorProps = new HashMap<QName, Serializable>(1, 1.0f);
        authorProps.put(ContentModel.PROP_AUTHOR, "Charles Dickens");
        this.dbNodeService.addAspect(nodeRef, ContentModel.ASPECT_AUTHOR, authorProps);
        
        return nodeRef;
    }
    
    public void test_ETHREEOH_1540() throws Exception
    {
        // Create the node used for tests
        NodeRef nodeRef = nodeService.createNode(
                rootNodeRef, 
                ContentModel.ASSOC_CHILDREN, 
                QName.createQName(TEST_NAMESPACE, "MyVersionableNode"),
                TEST_TYPE_QNAME,
                this.nodeProperties).getChildRef();
        
        nodeService.addAspect(nodeRef, ContentModel.ASPECT_TITLED, null);
        nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, "name");
        
        // Add the initial content to the node
        ContentWriter contentWriter = this.contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
        contentWriter.setMimetype("text/plain");
        contentWriter.setEncoding("UTF-8");
        contentWriter.putContent("my content");
        
        VersionHistory vh1 = version1Service.getVersionHistory(nodeRef);
        assertNull(vh1);
        
        version2Service.useDeprecatedV1 = true;
        
        // Add the version aspect to the created node
        nodeService.addAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE, null);
        
        vh1 = version1Service.getVersionHistory(nodeRef);
        assertNull(vh1);
        
        NodeRef workingCopyNodeRef = cociService.checkout(nodeRef);
        
        vh1 = version1Service.getVersionHistory(nodeRef);
        assertNull(vh1);

        int v1count = 3;
        
        for (int i = 1; i <= v1count; i++)
        {
            Map<String, Serializable> versionProperties = new HashMap<String, Serializable>();
            versionProperties.put(Version.PROP_DESCRIPTION, "This is a test checkin - " + i);
            
            cociService.checkin(workingCopyNodeRef, versionProperties);
            
            vh1 = version1Service.getVersionHistory(nodeRef);
            assertEquals(i, vh1.getAllVersions().size());
            
            workingCopyNodeRef = cociService.checkout(nodeRef);
            
            vh1 = version1Service.getVersionHistory(nodeRef);
            assertEquals(i, vh1.getAllVersions().size());
        }

        NodeRef oldVHNodeRef = version1Service.getVersionHistoryNodeRef(nodeRef);
        
        version2Service.useDeprecatedV1 = false;

        // Migrate and delete old version history !
        NodeRef versionedNodeRef = versionMigrator.v1GetVersionedNodeRef(oldVHNodeRef);
        
        //int nextVersionNumber = versionCounterService.nextVersionNumber(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, VersionModel.STORE_ID));
        //versionCounterService.setVersionNumber(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, Version2Model.STORE_ID), nextVersionNumber);
        
        // to force the error: https://issues.alfresco.com/jira/browse/ETHREEOH-1540
        //versionCounterService.setVersionNumber(new StoreRef(StoreRef.PROTOCOL_WORKSPACE, Version2Model.STORE_ID), 0);
        
        NodeRef newVHNodeRef = versionMigrator.migrateVersionHistory(oldVHNodeRef, versionedNodeRef);
        versionMigrator.v1DeleteVersionHistory(oldVHNodeRef);
        
        VersionHistory vh2 = version2Service.getVersionHistory(nodeRef);
        assertEquals(v1count, vh2.getAllVersions().size());
        
        int v2count = 3;
        
        for (int i = 1; i <= v2count; i++)
        {
            versionProperties = new HashMap<String, Serializable>();
            versionProperties.put(Version.PROP_DESCRIPTION, "This is a test checkin - " + (v1count + i));
            
            cociService.checkin(workingCopyNodeRef, versionProperties);
            
            vh2 = version2Service.getVersionHistory(nodeRef);
            assertEquals((v1count + i), vh2.getAllVersions().size());
            
            workingCopyNodeRef = cociService.checkout(nodeRef);
            
            vh2 = version2Service.getVersionHistory(nodeRef);
            assertEquals((v1count + i), vh2.getAllVersions().size());
        }
        
        logger.info("testMigrateOneCheckoutVersion: Migrated from oldVHNodeRef = " + oldVHNodeRef + " to newVHNodeRef = " + newVHNodeRef);
    }
    
    /**
     * Test migration of a single versioned node with versioned child assocs & peer assocs
     * 
     * @since 3.3 Ent - applies only to direct upgrade from 2.x to 3.3 Ent or higher
     */
    public void testMigrateVersionWithAssocs() throws Exception
    {
        if (version2Service.useDeprecatedV1 == true)
        {
            logger.info("testMigrateVersionWithAssocs: skip");
            return;
        }
        
        NodeRef versionableNode = createNewVersionableNode();
        NodeRef targetNode = createNewNode();
        
        nodeService.createAssociation(versionableNode, targetNode, TEST_ASSOC);
        
        // Get the next version label and snapshot the date-time
        String nextVersionLabel1 = peekNextVersionLabel(versionableNode, versionProperties);
        
        // Snap-shot the node created date-time
        long beforeVersionTime1 = ((Date)nodeService.getProperty(versionableNode, ContentModel.PROP_CREATED)).getTime();
        
        Version version1 = createVersion(versionableNode);
        logger.info(version1);
        
        VersionHistory vh1 = version1Service.getVersionHistory(versionableNode);
        assertEquals(1, vh1.getAllVersions().size());
        
        List<ChildAssociationRef> oldChildAssocs = nodeService.getChildAssocs(version1.getFrozenStateNodeRef());
        List<AssociationRef> oldAssocs = nodeService.getTargetAssocs(version1.getFrozenStateNodeRef(), RegexQNamePattern.MATCH_ALL);
        
        logger.info("testMigrateVersionWithAssocs: versionedNodeRef = " + versionableNode);
        
        NodeRef oldVHNodeRef = version1Service.getVersionHistoryNodeRef(versionableNode);
        
        // Migrate and delete old version history !
        NodeRef versionedNodeRef = versionMigrator.v1GetVersionedNodeRef(oldVHNodeRef);
        NodeRef newVHNodeRef = versionMigrator.migrateVersionHistory(oldVHNodeRef, versionedNodeRef);
        versionMigrator.v1DeleteVersionHistory(oldVHNodeRef);
        
        VersionHistory vh2 = version2Service.getVersionHistory(versionableNode);
        assertEquals(1, vh2.getAllVersions().size());
        
        // check new version - switch to new version service to do the check
        super.setVersionService(version2Service);
        
        Version[] newVersions = vh2.getAllVersions().toArray(new Version[]{});
        
        Version newVersion1 = newVersions[0];
        
        checkVersion(beforeVersionTime1, nextVersionLabel1, newVersion1, versionableNode);
        
        List<ChildAssociationRef> newChildAssocs = nodeService.getChildAssocs(newVersion1.getFrozenStateNodeRef());
        assertEquals(oldChildAssocs.size(), newChildAssocs.size());
        for (ChildAssociationRef oldChildAssoc : oldChildAssocs)
        {
            boolean found = false;
            for (ChildAssociationRef newChildAssoc : newChildAssocs)
            {
                if (newChildAssoc.getParentRef().getId().equals(oldChildAssoc.getParentRef().getId()) &&
                    newChildAssoc.getChildRef().equals(oldChildAssoc.getChildRef()) &&
                    newChildAssoc.getTypeQName().equals(oldChildAssoc.getTypeQName()) &&
                    newChildAssoc.getQName().equals(oldChildAssoc.getQName()) &&
                    (newChildAssoc.isPrimary() == oldChildAssoc.isPrimary()) &&
                    (newChildAssoc.getNthSibling() == oldChildAssoc.getNthSibling()))
                {
                    found = true;
                    break;
                }
            }
            
            if (! found)
            {
                fail(oldChildAssoc.toString()+ " not found");
            }
        }
        
        List<AssociationRef> newAssocs = nodeService.getTargetAssocs(newVersion1.getFrozenStateNodeRef(), RegexQNamePattern.MATCH_ALL);
        assertEquals(oldAssocs.size(), newAssocs.size());
        for (AssociationRef oldAssoc : oldAssocs)
        {
            boolean found = false;
            for (AssociationRef newAssoc : newAssocs)
            {
                if (newAssoc.getSourceRef().getId().equals(oldAssoc.getSourceRef().getId()) &&
                    newAssoc.getTargetRef().equals(oldAssoc.getTargetRef()) &&
                    newAssoc.getTypeQName().equals(oldAssoc.getTypeQName()) &&
                    EqualsHelper.nullSafeEquals(newAssoc.getId(), oldAssoc.getId()))
                {
                    found = true;
                    break;
                }
            }
            
            if (! found)
            {
                fail(oldAssoc.toString()+ " not found");
            }
        }
        
        logger.info("testMigrateVersionWithAssocs: Migrated from oldVHNodeRef = " + oldVHNodeRef + " to newVHNodeRef = " + newVHNodeRef);
    }
}
