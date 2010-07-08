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

package org.alfresco.repo.replication;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.replication.ReplicationDefinition;
import org.alfresco.service.cmr.replication.ReplicationService;
import org.alfresco.service.cmr.replication.ReplicationServiceException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.transfer.TransferDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseAlfrescoSpringTest;
import org.alfresco.util.GUID;

/**
 * @author Nick Burch
 */
public class ReplicationServiceIntegrationTest extends BaseAlfrescoSpringTest
{
    private ReplicationActionExecutor replicationActionExecutor;
    private ReplicationService replicationService;
    private JobLockService jobLockService;
    private NodeService nodeService;
    private Repository repositoryHelper;
    
    private NodeRef replicationRoot;
    
    private NodeRef folder1;
    private NodeRef folder2;
    private NodeRef folder2a;
    private NodeRef folder2b;
    private NodeRef content1_1;
    private NodeRef content1_2;
    private NodeRef thumbnail1_3;  // Thumbnail extends content
    private NodeRef authority1_4;  // Authority doesn't
    private NodeRef content2a_1;
    private NodeRef thumbnail2a_2; // Thumbnail extends content
    private NodeRef zone2a_3;      // Zone doesn't
    
    private final QName ACTION_NAME  = QName.createQName(NamespaceService.ALFRESCO_URI, "testName");
    private final QName ACTION_NAME2 = QName.createQName(NamespaceService.ALFRESCO_URI, "testName2");
    
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        super.onSetUpInTransaction();
        replicationActionExecutor = (ReplicationActionExecutor) this.applicationContext.getBean("replicationActionExecutor");
        replicationService = (ReplicationService) this.applicationContext.getBean("replicationService");
        jobLockService = (JobLockService) this.applicationContext.getBean("jobLockService");
        nodeService = (NodeService) this.applicationContext.getBean("nodeService");
        repositoryHelper = (Repository) this.applicationContext.getBean("repositoryHelper");
        
        // Set the current security context as admin
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        // Zap any existing replication entries
        replicationRoot = ReplicationDefinitionPersisterImpl.REPLICATION_ACTION_ROOT_NODE_REF;
        for(ChildAssociationRef child : nodeService.getChildAssocs(replicationRoot)) {
           QName type = nodeService.getType( child.getChildRef() );
           if(ReplicationDefinitionPersisterImpl.ACTION_TYPES.contains(type)) {
              nodeService.deleteNode(child.getChildRef());
           }
        }
        
        // Create the test folder structure
        folder1 = makeNode(repositoryHelper.getCompanyHome(), ContentModel.TYPE_FOLDER);
        folder2 = makeNode(repositoryHelper.getCompanyHome(), ContentModel.TYPE_FOLDER);
        folder2a = makeNode(folder2, ContentModel.TYPE_FOLDER);
        folder2b = makeNode(folder2, ContentModel.TYPE_FOLDER);
        
        content1_1 = makeNode(folder1, ContentModel.TYPE_CONTENT);
        content1_2 = makeNode(folder1, ContentModel.TYPE_CONTENT);
        thumbnail1_3 = makeNode(folder1, ContentModel.TYPE_THUMBNAIL);
        authority1_4 = makeNode(folder1, ContentModel.TYPE_AUTHORITY);
        content2a_1 = makeNode(folder2a, ContentModel.TYPE_CONTENT);
        thumbnail2a_2 = makeNode(folder2a, ContentModel.TYPE_THUMBNAIL);
        zone2a_3 = makeNode(folder2a, ContentModel.TYPE_ZONE);
    }
    
    @Override
   protected void onTearDownInTransaction() throws Exception {
      super.onTearDownInTransaction();
      if(folder1 != null) {
         nodeService.deleteNode(folder1);
      }
      if(folder2 != null) {
         nodeService.deleteNode(folder2);
      }
   }



   public void testCreation() throws Exception
    {
       ReplicationDefinition replicationAction =
          replicationService.createReplicationDefinition(ACTION_NAME, "Test Definition");
       assertNotNull(replicationAction);
       assertEquals("Test Definition", replicationAction.getDescription());
       assertEquals(ACTION_NAME, replicationAction.getReplicationName());
       
       String id = replicationAction.getId();
       assertNotNull(id);
       assertTrue(id.length() > 0);
       
       assertNotNull(replicationAction.getPayload());
       assertEquals(0, replicationAction.getPayload().size());
       
       assertNull(replicationAction.getLocalTransferReport());
    }
    
    public void testCreateSaveLoad() throws Exception
    {
       ReplicationDefinition replicationAction =
          replicationService.createReplicationDefinition(ACTION_NAME, "Test Definition");
       replicationAction.getPayload().add(
             new NodeRef("workspace://SpacesStore/Testing")
       );
       replicationAction.getPayload().add(
             new NodeRef("workspace://SpacesStore/Testing2")
       );
       assertEquals(2, replicationAction.getPayload().size());
       
       replicationService.saveReplicationDefinition(replicationAction);
       
       ReplicationDefinition retrieved =
          replicationService.loadReplicationDefinition(ACTION_NAME);
       assertNotNull(retrieved);
       assertEquals(ACTION_NAME, retrieved.getReplicationName());
       assertEquals("Test Definition", retrieved.getDescription());
       assertEquals(2, retrieved.getPayload().size());
    }
    
    public void testLoadList() throws Exception
    {
       assertEquals(0, replicationService.loadReplicationDefinitions().size());

       // Create and store
       ReplicationDefinition rd1 = replicationService.createReplicationDefinition(ACTION_NAME, "Test 1");
       ReplicationDefinition rd2 = replicationService.createReplicationDefinition(ACTION_NAME2, "Test 2");
       
       assertEquals(0, replicationService.loadReplicationDefinitions().size());

       replicationService.saveReplicationDefinition(rd1);
       
       assertEquals(1, replicationService.loadReplicationDefinitions().size());
       assertEquals(ACTION_NAME, replicationService.loadReplicationDefinitions().get(0).getReplicationName());
       
       replicationService.saveReplicationDefinition(rd2);
       assertEquals(2, replicationService.loadReplicationDefinitions().size());
    }
    
    public void testLoadByTarget() throws Exception
    {
       assertEquals(0, replicationService.loadReplicationDefinitions().size());
       assertEquals(0, replicationService.loadReplicationDefinitions("TestTarget").size());
       assertEquals(0, replicationService.loadReplicationDefinitions("TestTarget2").size());
       
       // Store some
       ReplicationDefinition rdTT = replicationService.createReplicationDefinition(ACTION_NAME, "Test");
       rdTT.setTargetName("TestTarget");
       replicationService.saveReplicationDefinition(rdTT);
       
       // Check it shows up correctly
       assertEquals(1, replicationService.loadReplicationDefinitions().size());
       assertEquals(1, replicationService.loadReplicationDefinitions("TestTarget").size());
       assertEquals(0, replicationService.loadReplicationDefinitions("TestTarget2").size());
    }

    /**
     * Test that the action service can find the executor
     *  for us, and that it has everything it needs
     */
    public void testBasicExecution() throws Exception
    {
       // First one with no target, which isn't allowed
       ReplicationDefinition rd = replicationService.createReplicationDefinition(ACTION_NAME, "Test");
       try {
          actionService.executeAction(rd, replicationRoot);
          fail("Shouldn't be permitted with no Target defined");
       } catch(ReplicationServiceException e) {}
       
       
       // Now no payload, also not allowed
       rd.setTargetName("TestTarget");
       try {
          actionService.executeAction(rd, replicationRoot);
          fail("Shouldn't be permitted with no payload defined");
       } catch(ReplicationServiceException e) {}
       
       
       // Next a proper one with a transient definition
       rd = replicationService.createReplicationDefinition(ACTION_NAME, "Test");
       rd.setTargetName("TestTarget");
       rd.getPayload().add( folder1 );
       // Will execute without error
       actionService.executeAction(rd, replicationRoot);
       
       
       // Now with one that's in the repo
       ReplicationDefinition rd2 = replicationService.createReplicationDefinition(ACTION_NAME2, "Test");
       rd2.setTargetName("TestTarget");
       rd2.getPayload().add(
             folder2
       );
       replicationService.saveReplicationDefinition(rd2);
       rd2 = replicationService.loadReplicationDefinition(ACTION_NAME2);
       // Again no errors
       actionService.executeAction(rd2, replicationRoot);
    }
    
    /**
     * Check that the locking works.
     * Take a 5 second lock on the job, then execute.
     * Ensure that we really wait a little over 5 seconds.
     */
    public void testReplicationExectionLocking() throws Exception
    {
       ReplicationDefinition rd = replicationService.createReplicationDefinition(ACTION_NAME, "Test");
       rd.setTargetName("TestTarget");
       rd.getPayload().add(folder1);
       rd.getPayload().add(folder2a);
       
       // Get the lock, and run
       long start = System.currentTimeMillis();
       String token = jobLockService.getLock(
             rd.getReplicationName(),
             5 * 1000,
             1,
             1
       );
       actionService.executeAction(rd, replicationRoot);
       long end = System.currentTimeMillis();
       
       assertTrue(
            "Should wait for the lock, but didn't (waited " + 
               ((end-start)/1000.0) + " seconds, not 5)",
            end-start > 5000
       );
    }
    
    /**
     * Test that we turn a list of payload node starting points
     *  into the correct set of nodes to pass to the 
     *  transfer service.
     */
    public void testReplicationPayloadExpansion() throws Exception
    {
       ReplicationDefinition rd = replicationService.createReplicationDefinition(ACTION_NAME, "Test");
       Set<NodeRef> expanded;
       
       // Empty folder -> just itself
       rd.getPayload().clear();
       rd.getPayload().add(folder2b);
       expanded = replicationActionExecutor.expandPayload(rd);
       assertEquals(1, expanded.size());
       assertTrue(expanded.contains(folder2b));
       
       // Folder with content and thumbnails - just content + thumbnail + folder
       rd.getPayload().clear();
       rd.getPayload().add(folder1);
       expanded = replicationActionExecutor.expandPayload(rd);
       assertEquals(4, expanded.size());
       assertTrue(expanded.contains(folder1));
       assertTrue(expanded.contains(content1_1));
       assertTrue(expanded.contains(content1_2));
       assertTrue(expanded.contains(thumbnail1_3));
       assertFalse(expanded.contains(authority1_4)); // Wrong type, won't be there
       
       // Folder with folders - descends properly
       rd.getPayload().clear();
       rd.getPayload().add(folder2);
       expanded = replicationActionExecutor.expandPayload(rd);
       assertEquals(5, expanded.size());
       assertTrue(expanded.contains(folder2));
       assertTrue(expanded.contains(folder2a));
       assertTrue(expanded.contains(content2a_1));
       assertTrue(expanded.contains(thumbnail2a_2));
       assertFalse(expanded.contains(zone2a_3)); // Wrong type, won't be there
       assertTrue(expanded.contains(folder2b));
       
       // Multiple things - gets each in turn
       rd.getPayload().clear();
       rd.getPayload().add(folder1);
       rd.getPayload().add(folder2);
       expanded = replicationActionExecutor.expandPayload(rd);
       assertEquals(9, expanded.size());
       assertTrue(expanded.contains(folder1));
       assertTrue(expanded.contains(content1_1));
       assertTrue(expanded.contains(content1_2));
       assertTrue(expanded.contains(thumbnail1_3));
       assertTrue(expanded.contains(folder2));
       assertTrue(expanded.contains(folder2a));
       assertTrue(expanded.contains(content2a_1));
       assertTrue(expanded.contains(thumbnail2a_2));
       assertTrue(expanded.contains(folder2b));
       
       // TODO Test how options like permissions and renditions
       //  affects what gets sent back
    }

    /**
     * Test that we turn a replication definition correctly
     *  into a transfer definition
     */
    public void testTransferDefinitionBuilding() throws Exception
    {
       ReplicationDefinition rd = replicationService.createReplicationDefinition(ACTION_NAME, "Test");
       
       Set<NodeRef> nodes = new HashSet<NodeRef>();
       nodes.add(folder1);
       nodes.add(content1_1);
       
       TransferDefinition td = replicationActionExecutor.buildTransferDefinition(rd, nodes);
       assertEquals(true, td.isComplete());
       assertEquals(2, td.getNodes().size());
       assertEquals(true, td.getNodes().contains(folder1));
       assertEquals(true, td.getNodes().contains(content1_1));
    }
    
    /**
     * Test that, with a mock transfer service, we
     *  pick the right things to replicate and call
     *  the transfer service correctly.
     */
    public void testReplicationExecution() throws Exception
    {
       // TODO
    }
    

    private NodeRef makeNode(NodeRef parent, QName nodeType)
    {
        String uuid = GUID.generate();
        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(ContentModel.PROP_NAME, uuid);
        ChildAssociationRef assoc = nodeService.createNode(parent, ContentModel.ASSOC_CONTAINS, QName.createQName(
                NamespaceService.APP_MODEL_1_0_URI, uuid), nodeType, props);
        return assoc.getChildRef();
    }
}
