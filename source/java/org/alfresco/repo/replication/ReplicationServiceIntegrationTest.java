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

import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.replication.ReplicationDefinition;
import org.alfresco.service.cmr.replication.ReplicationService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseAlfrescoSpringTest;

/**
 * @author Nick Burch
 */
public class ReplicationServiceIntegrationTest extends BaseAlfrescoSpringTest
{
    private ReplicationService replicationService;
    private NodeService nodeService;
    
    private final QName ACTION_NAME  = QName.createQName(NamespaceService.ALFRESCO_URI, "testName");
    private final QName ACTION_NAME2 = QName.createQName(NamespaceService.ALFRESCO_URI, "testName2");
    
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        super.onSetUpInTransaction();
        replicationService = (ReplicationService) this.applicationContext.getBean("replicationService");
        nodeService = (NodeService) this.applicationContext.getBean("nodeService");
        
        // Set the current security context as admin
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        // Zap any existing entries
        NodeRef replicationRoot = ReplicationDefinitionPersisterImpl.REPLICATION_ACTION_ROOT_NODE_REF;
        for(ChildAssociationRef child : nodeService.getChildAssocs(replicationRoot)) {
           QName type = nodeService.getType( child.getChildRef() );
           if(ReplicationDefinitionPersisterImpl.ACTION_TYPES.contains(type)) {
              nodeService.deleteNode(child.getChildRef());
           }
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

}