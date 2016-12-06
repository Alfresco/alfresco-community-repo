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
package org.alfresco.repo.quickshare;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import org.alfresco.model.ContentModel;
import org.alfresco.model.QuickShareModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.node.archive.RestoreNodeReport;
import org.alfresco.repo.node.archive.RestoreNodeReport.RestoreStatus;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.action.scheduled.ScheduledPersistedAction;
import org.alfresco.service.cmr.action.scheduled.ScheduledPersistedActionService;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.alfresco.service.cmr.quickshare.InvalidSharedIdException;
import org.alfresco.service.cmr.quickshare.QuickShareDTO;
import org.alfresco.service.cmr.quickshare.QuickShareLinkExpiryAction;
import org.alfresco.service.cmr.quickshare.QuickShareLinkExpiryActionPersister;
import org.alfresco.service.cmr.quickshare.QuickShareService;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.test.junitrules.AlfrescoPerson;
import org.alfresco.util.test.junitrules.ApplicationContextInit;
import org.alfresco.util.test.junitrules.TemporaryModels;
import org.alfresco.util.test.junitrules.TemporaryNodes;
import org.apache.commons.codec.binary.Base64;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;
import org.springframework.context.ApplicationContext;

/**
 * Quick share service tests.
 *
 * @author Alex Miller
 * @since Cloud/4.2
 */
public class QuickShareServiceIntegrationTest
{
    private static final ApplicationContextInit testContext = new ApplicationContextInit();
    
    private static final String MODEL = 
    	"<?xml version='1.0' encoding='UTF-8'?>" +
		"<model name='lx:lxmodel' xmlns='http://www.alfresco.org/model/dictionary/1.0'>" +
            "<description>LX model</description>" +
            "<author>Peter Löfgren</author>" +
            "<version>1.0</version>" +
            "<imports>" +
	            "<import uri='http://www.alfresco.org/model/dictionary/1.0' prefix='d' />" +
                "<import uri='http://www.alfresco.org/model/content/1.0' prefix='cm' />" +
	        "</imports>" +
            "<namespaces>" +
	            "<namespace uri='http://bugtestmodel' prefix='lx' />" +
            "</namespaces>" +
	        "<constraints>" +
            "</constraints>" +
	        "<types>" +
                "<type name='lx:doc'>" +
		            "<title>LX dokument</title>" +
                    "<parent>cm:content</parent>" +
		            "<mandatory-aspects>" +
                        "<aspect>cm:generalclassifiable</aspect>" +
		            "</mandatory-aspects>" +
                "</type>" +
                "<type name='lx:doc2'>" +
		            "<title>LX dokument 2</title>" +
                    "<parent>cm:cmobject</parent>" +
                "</type>" +
		    "</types>" +
        "</model>";
    
    private static CopyService copyService;
    private static NodeService nodeService;
    private static QuickShareService quickShareService;
    private static QuickShareService directQuickShareService;
    private static Repository repository;
    private static AttributeService attributeService;
    private static PermissionService permissionService;
    private static NodeArchiveService nodeArchiveService;
    private static ScheduledPersistedActionService scheduledPersistedActionService;
    private static QuickShareLinkExpiryActionPersister quickShareLinkExpiryActionPersister;
    private static RetryingTransactionHelper transactionHelper;
    private static Properties globalProperties;
    
    private static AlfrescoPerson user1 = new AlfrescoPerson(testContext, "UserOne");
    private static AlfrescoPerson user2 = new AlfrescoPerson(testContext, "UserTwo");
    
    // A rule to manage test nodes reused across all the test methods
    @Rule public TemporaryNodes testNodes = new TemporaryNodes(testContext);
    
    @Rule public TemporaryModels temporaryModels = new TemporaryModels(testContext);

    @ClassRule public static RuleChain classChain = RuleChain.outerRule(testContext)
                                                         .around(user1)
                                                         .around(user2);

    private NodeRef testNode;

    private NodeRef userHome;

    @BeforeClass public static void beforeClass() throws Exception
    {
        findServices();
    }    
    

    private static void findServices()
    {
        ApplicationContext ctx = testContext.getApplicationContext();
        
        copyService = ctx.getBean("CopyService", CopyService.class);
        nodeService = ctx.getBean("NodeService", NodeService.class);
        directQuickShareService = ctx.getBean("quickShareService", QuickShareService.class);
        quickShareService = ctx.getBean("QuickShareService", QuickShareService.class);
        repository = ctx.getBean("repositoryHelper", Repository.class);
        attributeService = ctx.getBean("AttributeService", AttributeService.class);
        permissionService = ctx.getBean("PermissionService", PermissionService.class);
        nodeArchiveService = ctx.getBean("nodeArchiveService", NodeArchiveService.class);
        scheduledPersistedActionService = ctx.getBean("scheduledPersistedActionService", ScheduledPersistedActionService.class);
        quickShareLinkExpiryActionPersister = ctx.getBean("quickShareLinkExpiryActionPersister", QuickShareLinkExpiryActionPersister.class);
        transactionHelper = ctx.getBean("retryingTransactionHelper", RetryingTransactionHelper.class);
        globalProperties = ctx.getBean("global-properties", Properties.class);
    }
    
    @Before public void createTestData()
    {
        userHome = repository.getUserHome(user1.getPersonNode());
        
        testNode = testNodes.createNodeWithTextContent(userHome,
                        "Quick Share Test Node",
                        ContentModel.TYPE_CONTENT, 
                        user1.getUsername(),
                        "Quick Share Test Node Content");
    }

    @Test public void getMetaDataFromNodeRefByOwner() 
    {
        Map<String, Object> metadata = AuthenticationUtil.runAs(new RunAsWork<Map<String,Object>>(){

            @Override
            public Map<String, Object> doWork() throws Exception
            {
                return quickShareService.getMetaData(testNode);    
            }
        }, user1.getUsername());
        
        assertNotNull(metadata);
        assertTrue(metadata.size() > 0);
    }
    
    @Test(expected=AccessDeniedException.class) 
    public void getMetaDataFromNodeRefByNonOwner() 
    {
        Map<String, Object> metadata = AuthenticationUtil.runAs(new RunAsWork<Map<String,Object>>(){

            @Override
            public Map<String, Object> doWork() throws Exception
            {
                return quickShareService.getMetaData(testNode);    
            }
        }, user2.getUsername());
        
    }

    @Test public void share() 
    {
        share(testNode, user1.getUsername());
        
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>(){

            @Override
            public Void doWork() throws Exception
            {
                assertTrue( nodeService.getAspects(testNode).contains(QuickShareModel.ASPECT_QSHARE));
                assertNotNull(nodeService.getProperty(testNode, QuickShareModel.PROP_QSHARE_SHAREDID));
                assertEquals(user1.getUsername(), nodeService.getProperty(testNode, QuickShareModel.PROP_QSHARE_SHAREDBY));
                return null;
            }
            
        });
    }
    
    @Test public void unshare() {
        final QuickShareDTO dto = share(testNode, user1.getUsername());
        unshare(dto.getId(), user1.getUsername());
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>(){

            @Override
            public Void doWork() throws Exception
            {
                assertFalse( nodeService.getAspects(testNode).contains(QuickShareModel.ASPECT_QSHARE));
                assertNull(nodeService.getProperty(testNode, QuickShareModel.PROP_QSHARE_SHAREDID));
                assertNull(nodeService.getProperty(testNode, QuickShareModel.PROP_QSHARE_SHAREDBY));
                return null;
            }
            
        });
    }

    // MNT-16224, RA-1093
    @Test public void testDeleteAndRestoreSharedNode()
    {
        // Share the test node
        share(testNode, user1.getUsername());
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                assertTrue(nodeService.hasAspect(testNode, QuickShareModel.ASPECT_QSHARE));
                return null;
            }
        });

        // Delete and restore the shared node.
        testNode = AuthenticationUtil.runAs(new RunAsWork<NodeRef>()
        {
            @Override
            public NodeRef doWork() throws Exception
            {
                // Delete the shared node
                nodeService.deleteNode(testNode);

                // Check if the node has been archived
                final NodeRef archivedNode = nodeArchiveService.getArchivedNode(testNode);
                assertNotNull(archivedNode);

                // Restore the deleted shared node from trashcan
                RestoreNodeReport restoreNodeReport = nodeArchiveService.restoreArchivedNode(archivedNode);
                assertNotNull(restoreNodeReport);
                assertTrue(restoreNodeReport.getStatus() == RestoreStatus.SUCCESS);
                NodeRef restoredNodeRef = restoreNodeReport.getRestoredNodeRef();
                assertNotNull(restoredNodeRef);
                return restoredNodeRef;
            }

        }, user1.getUsername());

        // Check the restored node doesn't have the 'shared' aspect.
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                assertFalse(nodeService.hasAspect(testNode, QuickShareModel.ASPECT_QSHARE));
                assertNull(nodeService.getProperty(testNode, QuickShareModel.PROP_QSHARE_SHAREDID));
                assertNull(nodeService.getProperty(testNode, QuickShareModel.PROP_QSHARE_SHAREDBY));
                return null;
            }
        });


        /**
         * Tests the scenario where the shared node has been deleted and restored before the fix (MNT-16224).
         * In this scenario the user should be able to un-share the restored node.
         */
        {
            // Share the test node again
            final QuickShareDTO dto = share(testNode, user1.getUsername());

            // Delete only the sharedId without removing the 'shared' aspect!(The cause of MNT-16224 and RA-1093)
            TenantUtil.runAsDefaultTenant(new TenantRunAsWork<Void>()
            {
                public Void doWork() throws Exception
                {
                    attributeService.removeAttribute(".sharedIds", dto.getId());
                    return null;
                }
            });

            // Check the 'shared' aspect does exist
            AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
            {
                @Override
                public Void doWork() throws Exception
                {
                    assertTrue(nodeService.hasAspect(testNode, QuickShareModel.ASPECT_QSHARE));
                    return null;
                }
            });

            try
            {
                // Try to un-share the node even though the sharedId was deleted.
                unshare(dto.getId(), user2.getUsername());
                fail("user2 shouldn't be able to un-share the node.");
            }
            catch (InvalidSharedIdException ex)
            {
                // Expected
            }

            // Un-share the node even though the sharedId was deleted.
            // This should succeed as the lookup will use TMDQ.
            unshare(dto.getId(), user1.getUsername());

            // Check the 'shared' aspect does not exist
            AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
            {
                @Override
                public Void doWork() throws Exception
                {
                    assertFalse(nodeService.hasAspect(testNode, QuickShareModel.ASPECT_QSHARE));
                    return null;
                }
            });
        }
    }

    private void unshare(final String sharedId, final String userName) {
        
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {

            @Override
            public Void doWork() throws Exception
            {
                quickShareService.unshareContent(sharedId);
                return null;
            }
        }, userName);
    }

    private QuickShareDTO share(final NodeRef nodeRef, final String username)
    {
        return share(nodeRef, username, null);
    }

    private QuickShareDTO share(final NodeRef nodeRef, final String username, final Date expiryDate)
    {
        return AuthenticationUtil.runAs(() -> quickShareService.shareContent(nodeRef, expiryDate), username);
    }

    @Test public void getMetadataFromShareId()
    {
        QuickShareDTO dto = share(testNode, user1.getUsername());
        
        Map<String, Object> metadata = quickShareService.getMetaData(dto.getId());
        
        assertNotNull(metadata);
        assertTrue(metadata.size() > 0);
    }
    
    @Test(expected=InvalidSharedIdException.class) public void getMetadataFromShareIdWithInvalidId()
    {
        UUID uuid = UUIDGenerator.getInstance().generateRandomBasedUUID();
        String sharedId = Base64.encodeBase64URLSafeString(uuid.toByteArray()); // => 22 chars (eg. q3bEKPeDQvmJYgt4hJxOjw)

        Map<String, Object> metadata = quickShareService.getMetaData(sharedId);
    }

    @Test public void copyNode()
    {
        share(testNode, user1.getUsername());
        
        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {

            @Override
            public Object doWork() throws Exception
            {
                
                Assert.assertTrue(nodeService.hasAspect(testNode, QuickShareModel.ASPECT_QSHARE));
                Assert.assertNotNull(nodeService.getProperty(testNode, QuickShareModel.PROP_QSHARE_SHAREDBY));
                Assert.assertNotNull(nodeService.getProperty(testNode, QuickShareModel.PROP_QSHARE_SHAREDID));

                Map<QName, Serializable> originalProps = nodeService.getProperties(testNode);
                
                NodeRef copyNodeRef = copyService.copyAndRename(testNode, userHome, ContentModel.ASSOC_CONTAINS, 
                            QName.createQName(NamespaceService.APP_MODEL_1_0_URI, "copy"), true);
                
                Map<QName, Serializable> copyProps = nodeService.getProperties(copyNodeRef);
                
                Assert.assertFalse(nodeService.hasAspect(copyNodeRef, QuickShareModel.ASPECT_QSHARE));
                Assert.assertNull(nodeService.getProperty(copyNodeRef, QuickShareModel.PROP_QSHARE_SHAREDBY));
                Assert.assertNull(nodeService.getProperty(copyNodeRef, QuickShareModel.PROP_QSHARE_SHAREDID));
                
                for (QName property : originalProps.keySet())
                {
                    if (property.equals(QuickShareModel.PROP_QSHARE_SHAREDBY) ||
                        property.equals(QuickShareModel.PROP_QSHARE_SHAREDID))
                    {
                        continue;
                    }
                    Assert.assertTrue("Mising property " + property, copyProps.containsKey(property));
                }
                return null;
            }
        }, user1.getUsername());
    }
    
    /**
     * Content types that extend cm:content should be shareable.
     * 
     * See https://issues.alfresco.com/jira/browse/ALF-16274.
     */
    @Test public void testWithCustomContentType() 
    {
    	ByteArrayInputStream modelStream = new ByteArrayInputStream(MODEL.getBytes());
    	temporaryModels.loadModel(modelStream);
        
        QName sharableType = QName.createQName("{http://bugtestmodel}doc");
        QName unsharableType = QName.createQName("{http://bugtestmodel}doc2");

    	final NodeRef sharableNode = testNodes.createNodeWithTextContent(userHome,
                           "Quick Share Custom Type Sharable Test Node",
                           sharableType, 
                           user1.getUsername(),
                           "Quick Share Test Node Content");
    	
        Map<String, Object> metadata = getMetadata(sharableNode, user1);
        
        assertTrue((Boolean)metadata.get("sharable"));
        
        QuickShareDTO dto = share(sharableNode, user1.getUsername());
        unshare(dto.getId(), user1.getUsername());        
        
    	final NodeRef unsharableNode = testNodes.createNodeWithTextContent(userHome,
                "Quick Share Custom Type Unsharable Test Node",
                unsharableType, 
                user1.getUsername(),
                "Quick Share Test Node Content");

		metadata = getMetadata(unsharableNode, user1);
        assertFalse((Boolean)metadata.get("sharable"));

		boolean exceptionThrown = false;
		try {
			// Prior to fixing ALF-16274, this would throw an InvalidNodeRefException. 
			share(unsharableNode, user1.getUsername());
		}
		catch(InvalidNodeRefException ex)
		{
			exceptionThrown = true;
		}
		assertTrue("InvalidNodeRefException not thrown on trying to share an unsharable content type", exceptionThrown);
    }


	@SuppressWarnings("unchecked")
	private Map<String, Object> getMetadata(final NodeRef nodeRef, AlfrescoPerson user) {
		Map<String, Object> container = AuthenticationUtil.runAs(new RunAsWork<Map<String, Object>>()
        {
            @Override
            public Map<String, Object> doWork() throws Exception
            {
            	return quickShareService.getMetaData(nodeRef);
            }
        }, user.getUsername());
		return (Map<String, Object>)container.get("item");
	}
	
    @Test public void cloud928()
    {
        final NodeRef node = testNodes.createNodeWithTextContent(userHome,
                "CLOUD-928 Test Node",
                ContentModel.TYPE_CONTENT, 
                user1.getUsername(),
                "Quick Share Test Node Content");
        
        QuickShareDTO dto = share(node, user1.getUsername());

        attributeService.removeAttribute(QuickShareServiceImpl.ATTR_KEY_SHAREDIDS_ROOT, dto.getId());
        
        AuthenticationUtil.runAs(new RunAsWork<Object>(){

            @Override
            public Object doWork() throws Exception {
                nodeService.deleteNode(node);
                return null;
            }
        }, user1.getUsername());
 
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        Assert.assertFalse(nodeService.exists(node));
    }
    
    /**
     * Test for MNT-11960
     * <p> The node is created by user1 and shared by user2.
     * <p> The modifier should not change to user2 after sharing.
     */
    @Test
    public void testModifierAfterSharing()
    {
        AuthenticationUtil.runAs(new RunAsWork<Void>(){
            @Override
            public Void doWork() throws Exception
            {
                permissionService.setPermission(testNode, user2.getUsername(), PermissionService.CONSUMER, true);
                return null;
            }
        }, user1.getUsername());
        
        final Serializable modifiedDate = AuthenticationUtil.runAsSystem(new RunAsWork<Serializable>(){
            @Override
            public Serializable doWork() throws Exception
            {
                return nodeService.getProperty(testNode, ContentModel.PROP_MODIFIED);
            }
        });
        
        share(testNode, user2.getUsername());
        
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>(){
            @Override
            public Void doWork() throws Exception
            {
                assertTrue(nodeService.getAspects(testNode).contains(ContentModel.ASPECT_AUDITABLE));
                assertNotNull(nodeService.getProperty(testNode, ContentModel.PROP_MODIFIER));
                assertEquals("The modifier has changed after sharing.", user1.getUsername(), nodeService.getProperty(testNode, ContentModel.PROP_MODIFIER));
                assertNotNull(nodeService.getProperty(testNode, ContentModel.PROP_MODIFIED));
                assertEquals("The modified date has changed after sharing.", modifiedDate, nodeService.getProperty(testNode, ContentModel.PROP_MODIFIED));
                return null;
            }
        });
    }

    /**
     * Test for MNT-15654
     * <p> The node is created and shared by user1. Then unshared by user2
     * <p> The modifier should not change to user2 after unsharing.
     */
    @Test
    public void testModifierAfterUnSharing()
    {
        AuthenticationUtil.runAs(new RunAsWork<Void>(){
            @Override
            public Void doWork() throws Exception
            {
                permissionService.setPermission(testNode, user2.getUsername(), PermissionService.CONSUMER, true);
                return null;
            }
        }, user1.getUsername());

        QuickShareDTO dto = share(testNode, user1.getUsername());
        unshare(dto.getId(), user2.getUsername());

        String modifier = AuthenticationUtil.runAsSystem(new RunAsWork<String>(){
            @Override
            public String doWork() throws Exception
            {
                return (String )nodeService.getProperty(testNode, ContentModel.PROP_MODIFIER);
            }
        });

        assertEquals("The modifier has changed after sharing.", user1.getUsername(), modifier);
    }

    /**
     * Test the quick share link expiry date action.
     */
    @Test
    public void testSharedLinkExpiryScheduling() throws  Exception
    {
        // First record the number of available schedules
        final int numOfSchedules = listSchedules();

        // 1 day from now
        Date expiryDate = DateTime.now().plusDays(1).toDate();
        QuickShareDTO quickShareDTO = share(testNode, user1.getUsername(), expiryDate);
        assertTrue(hasQuickShareAspect(testNode));
        assertEquals(quickShareDTO.getId(), getProperty(testNode, QuickShareModel.PROP_QSHARE_SHAREDID));
        assertNotNull(quickShareDTO.getExpiresAt());
        assertEquals(expiryDate, quickShareDTO.getExpiresAt());
        // Check that the expiry action is persisted
        QuickShareLinkExpiryAction expiryAction = getExpiryActionAndAttachSchedule(quickShareDTO.getId());
        assertEquals(quickShareDTO.getId(), expiryAction.getSharedId());
        assertEquals(quickShareDTO.getExpiresAt(), expiryAction.getScheduleStart());
        assertNull("We haven't set interval count.", expiryAction.getScheduleIntervalCount());
        assertNull("We haven't set interval period.", expiryAction.getScheduleIntervalPeriod());

        // Try to share the already shared node with a different expiry date.
        // This basically will update the expiry action start time
        expiryDate = DateTime.now().plusDays(7).toDate();
        quickShareDTO = share(testNode, user1.getUsername(), expiryDate);
        assertEquals(expiryDate, quickShareDTO.getExpiresAt());
        assertEquals(expiryDate, getProperty(testNode, QuickShareModel.PROP_QSHARE_EXPIRY_DATE));
        assertTrue(hasQuickShareAspect(testNode));
        assertEquals(quickShareDTO.getId(), getProperty(testNode, QuickShareModel.PROP_QSHARE_SHAREDID));
        // Check that the expiry action is persisted
        expiryAction = getExpiryActionAndAttachSchedule(quickShareDTO.getId());
        assertEquals(quickShareDTO.getId(), expiryAction.getSharedId());
        assertEquals(quickShareDTO.getExpiresAt(), expiryAction.getScheduleStart());
        assertNull("We haven't set interval count.", expiryAction.getScheduleIntervalCount());
        assertNull("We haven't set interval period.", expiryAction.getScheduleIntervalPeriod());

        // Delete the expiry action
        deleteExpiryAction(expiryAction);

        // Check that the expiry action has been deleted
        QuickShareLinkExpiryAction deletedExpiryAction = getExpiryAction(quickShareDTO.getId());
        assertNull(deletedExpiryAction);
        assertNull(getProperty(testNode, QuickShareModel.PROP_QSHARE_EXPIRY_DATE));
        // Unshare
        unshare(quickShareDTO.getId(), user1.getUsername());

        // Share the testNode, with expiry date of 1 day from now
        expiryDate = DateTime.now().plusDays(1).toDate();
        quickShareDTO = share(testNode, user1.getUsername(), expiryDate);
        assertTrue(hasQuickShareAspect(testNode));
        expiryAction = getExpiryActionAndAttachSchedule(quickShareDTO.getId());
        assertEquals(expiryDate, expiryAction.getScheduleStart());
        assertEquals(numOfSchedules + 1, listSchedules());
        // Now update the schedule to be executed in 5 seconds.
        expiryAction.setScheduleStart(DateTime.now().plusSeconds(5).toDate());
        // Here we'll bypass the QuickShareService in order to force the new time.
        // As the QuickShareService by default will enforce the expiry date to not be less than 24 hours.
        forceSaveNewExpiryTime(expiryAction);

        // wait 10 seconds
        Thread.sleep(10000L);
        // Check that the expiry action was successful and it removed the shared link
        assertFalse(hasQuickShareAspect(testNode));
        // Also check the expiry date property is removed
        assertNull(getProperty(testNode, QuickShareModel.PROP_QSHARE_EXPIRY_DATE));
        // Check that the persisted expiry action is removed
        assertNull(getExpiryAction(quickShareDTO.getId()));
        // Check that the persisted schedule is removed as well
        assertEquals(numOfSchedules, listSchedules());

        // Share the testNode, with expiry date of 1 day from now
        expiryDate = DateTime.now().plusDays(1).toDate();
        quickShareDTO = share(testNode, user1.getUsername(), expiryDate);
        assertTrue(hasQuickShareAspect(testNode));
        expiryAction = getExpiryActionAndAttachSchedule(quickShareDTO.getId());
        assertEquals(expiryDate, expiryAction.getScheduleStart());

        // Delete the shared testNode as user1
        AuthenticationUtil.runAs(() -> {
            nodeService.deleteNode(testNode);

            return null;
        }, user1.getUsername());

        // Check that the persisted expiry action is removed, as we have deleted the source node
        assertNull(getExpiryAction(quickShareDTO.getId()));
        // Check that the persisted schedule is removed as well
        assertEquals(numOfSchedules, listSchedules());

        // Restore the testNode as user1
        AuthenticationUtil.runAs(() -> {
            final NodeRef archivedNode = nodeArchiveService.getArchivedNode(testNode);
            RestoreNodeReport restoreNodeReport = nodeArchiveService.restoreArchivedNode(archivedNode);
            assertNotNull(restoreNodeReport);
            assertTrue(restoreNodeReport.getStatus() == RestoreStatus.SUCCESS);
            testNode = restoreNodeReport.getRestoredNodeRef();

            return null;
        }, user1.getUsername());

        // Check that restoring the node hasn't brought back the shared aspect or the persisted expiry action
        assertFalse(hasQuickShareAspect(testNode));
        assertNull(getExpiryAction(quickShareDTO.getId()));
        assertEquals(numOfSchedules, listSchedules());
    }

    /**
     * Test date validator for the quick share link expiry date action.
     */
    @Test
    public void testSharedLinkExpiryDateValidator() throws  Exception
    {
        // Try to share with invalid time - passed time
        try
        {
            share(testNode, user1.getUsername(), DateTime.now().minusDays(1).toDate());
            fail("Should have failed as the expiry date is invalid (passed time).");
        }
        catch (QuickShareLinkExpiryActionException.InvalidExpiryDateException ex)
        {
            // Expected
        }

        final String defaultExpiryDatePeriod = globalProperties.getProperty("system.quickshare.expiry_date.enforce.minimum.period");

        // Test expiry date period enforcement
        try
        {
            /*
             * Set the expiry date period enforcement to Days
             */
            {
                ((QuickShareServiceImpl) directQuickShareService).setExpiryDatePeriod("DAYS");

                try
                {
                    // Try to share with invalid time - less than 1 day
                    share(testNode, user1.getUsername(), DateTime.now().plusHours(1).toDate());
                    fail("Should have failed as the expiry date is invalid (less than 1 day).");
                }
                catch (QuickShareLinkExpiryActionException.InvalidExpiryDateException ex)
                {
                    // Expected
                }
                try
                {
                    // Try to share with invalid time - less than 1 day
                    share(testNode, user1.getUsername(), DateTime.now().plusMinutes(30).toDate());
                    fail("Should have failed as the expiry date is invalid (less than 1 day).");
                }
                catch (QuickShareLinkExpiryActionException.InvalidExpiryDateException ex)
                {
                    // Expected
                }
                // Set the expiry date to be in 24 hours
                Date expiryDate = DateTime.now().plusHours(24).toDate();
                QuickShareDTO quickShareDTO = share(testNode, user1.getUsername(), expiryDate);
                assertTrue(hasQuickShareAspect(testNode));
                QuickShareLinkExpiryAction expiryAction = getExpiryActionAndAttachSchedule(quickShareDTO.getId());
                assertEquals(expiryDate, expiryAction.getScheduleStart());
                // Unshare
                unshare(quickShareDTO.getId(), user1.getUsername());

                // Set the expiry date to be next year
                expiryDate = DateTime.now().plusYears(1).toDate();
                quickShareDTO = share(testNode, user1.getUsername(), expiryDate);
                assertTrue(hasQuickShareAspect(testNode));
                expiryAction = getExpiryActionAndAttachSchedule(quickShareDTO.getId());
                assertEquals(expiryDate, expiryAction.getScheduleStart());
                // Unshare
                unshare(quickShareDTO.getId(), user1.getUsername());
            }
            /*
             * Set the expiry date period enforcement to Hours
             */
            {
                ((QuickShareServiceImpl) directQuickShareService).setExpiryDatePeriod("HOURS");

                try
                {
                    // Try to share with invalid time - less than 1 hour
                    share(testNode, user1.getUsername(), DateTime.now().plusMinutes(30).toDate());
                    fail("Should have failed as the expiry date is invalid (less than 1 hour).");
                }
                catch (QuickShareLinkExpiryActionException.InvalidExpiryDateException ex)
                {
                    // Expected
                }
                // Set the expiry date to be in the next hour
                Date expiryDate = DateTime.now().plusHours(1).toDate();
                QuickShareDTO quickShareDTO = share(testNode, user1.getUsername(), expiryDate);
                assertTrue(hasQuickShareAspect(testNode));
                QuickShareLinkExpiryAction expiryAction = getExpiryActionAndAttachSchedule(quickShareDTO.getId());
                assertEquals(expiryDate, expiryAction.getScheduleStart());
                // Unshare
                unshare(quickShareDTO.getId(), user1.getUsername());

                // Set the expiry date to be in the next 2 days, even though we did set the date period to HOURS.
                expiryDate = DateTime.now().plusDays(2).toDate();
                quickShareDTO = share(testNode, user1.getUsername(), expiryDate);
                assertTrue(hasQuickShareAspect(testNode));
                expiryAction = getExpiryActionAndAttachSchedule(quickShareDTO.getId());
                assertEquals(expiryDate, expiryAction.getScheduleStart());
                // Unshare
                unshare(quickShareDTO.getId(), user1.getUsername());
            }
            /*
             * Set the expiry date period enforcement to Minutes
             */
            {
                ((QuickShareServiceImpl) directQuickShareService).setExpiryDatePeriod("MINUTES");

                try
                {
                    // Try to share with invalid time - less than 1 minute
                    share(testNode, user1.getUsername(), DateTime.now().plusSeconds(10).toDate());
                    fail("Should have failed as the expiry date is invalid (less than 1 minute).");
                }
                catch (QuickShareLinkExpiryActionException.InvalidExpiryDateException ex)
                {
                    // Expected
                }
                // Set the expiry date to be in 5 minutes time
                Date expiryDate = DateTime.now().plusMinutes(5).toDate();
                QuickShareDTO quickShareDTO = share(testNode, user1.getUsername(), expiryDate);
                assertTrue(hasQuickShareAspect(testNode));
                QuickShareLinkExpiryAction expiryAction = getExpiryActionAndAttachSchedule(quickShareDTO.getId());
                assertEquals(expiryDate, expiryAction.getScheduleStart());
                // Unshare
                unshare(quickShareDTO.getId(), user1.getUsername());

                // Set the expiry date to be in 60 days
                expiryDate = DateTime.now().plusDays(60).toDate();
                quickShareDTO = share(testNode, user1.getUsername(), expiryDate);
                assertTrue(hasQuickShareAspect(testNode));
                expiryAction = getExpiryActionAndAttachSchedule(quickShareDTO.getId());
                assertEquals(expiryDate, expiryAction.getScheduleStart());
                // Unshare
                unshare(quickShareDTO.getId(), user1.getUsername());
            }
        }
        finally
        {
            ((QuickShareServiceImpl) directQuickShareService).setExpiryDatePeriod(defaultExpiryDatePeriod);
        }
    }

    private QuickShareLinkExpiryAction getExpiryActionAndAttachSchedule(String sharedId)
    {

            // Check that the expiry action is persisted
            QuickShareLinkExpiryAction expiryAction = getExpiryAction(sharedId);
            assertNotNull(expiryAction);
            assertNotNull("Expiry action should have been persisted.", expiryAction.getNodeRef());
            assertNull("The schedule hasn't been attached yet.", expiryAction.getSchedule());
            ScheduledPersistedAction scheduledPersistedAction = getSchedule(expiryAction);
            assertNotNull("Scheduled action should have been persisted.", scheduledPersistedAction);
            //Attach the schedule
            expiryAction.setSchedule(scheduledPersistedAction);

            return expiryAction;

    }

    private QuickShareLinkExpiryAction getExpiryAction(final String sharedId)
    {
        return AuthenticationUtil.runAsSystem(
                    () -> quickShareLinkExpiryActionPersister.loadQuickShareLinkExpiryAction(QuickShareLinkExpiryActionImpl.createQName(sharedId)));
    }

    private ScheduledPersistedAction getSchedule(final QuickShareLinkExpiryAction linkExpiryAction)
    {
        return AuthenticationUtil.runAsSystem(
                    () -> scheduledPersistedActionService.getSchedule(linkExpiryAction));
    }

    private int listSchedules()
    {
        return AuthenticationUtil.runAsSystem(() -> scheduledPersistedActionService.listSchedules().size());

    }

    private void deleteExpiryAction(final QuickShareLinkExpiryAction linkExpiryAction)
    {
        transactionHelper.doInTransaction(() -> {
            quickShareService.deleteQuickShareLinkExpiryAction(linkExpiryAction);
            return null;
        });
    }

    private boolean hasQuickShareAspect(NodeRef nodeRef)
    {
        return AuthenticationUtil.runAsSystem(() -> nodeService.hasAspect(nodeRef, QuickShareModel.ASPECT_QSHARE));
    }

    private Serializable getProperty(NodeRef nodeRef, QName property)
    {
        return AuthenticationUtil.runAsSystem(() -> nodeService.getProperty(nodeRef, property));
    }

    private void forceSaveNewExpiryTime(final QuickShareLinkExpiryAction linkExpiryAction)
    {
        transactionHelper.doInTransaction(() -> {
            AuthenticationUtil.runAsSystem(() -> {
                quickShareLinkExpiryActionPersister.saveQuickShareLinkExpiryAction(linkExpiryAction);
                scheduledPersistedActionService.saveSchedule(linkExpiryAction.getSchedule());
                return null;
            });
            return null;
        });
    }
}
