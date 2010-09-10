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

import static org.mockito.Mockito.mock;
import junit.framework.TestCase;

import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.replication.ReplicationDefinition;

/**
 * @author Nick Burch
 */
public class ReplicationServiceImplTest extends TestCase
{
    private ActionService actionService = mock(ActionService.class);
    
    private final ReplicationDefinitionPersisterImpl replicationDefinitionPersister = mock(ReplicationDefinitionPersisterImpl.class);
    private ReplicationServiceImpl replicationService;
    
    private final String ACTION_NAME  = "testName";
    private final String ACTION_NAME2 = "testName2";

    @Override
    protected void setUp() throws Exception
    {
       replicationService = new ReplicationServiceImpl();
       replicationService.setActionService(actionService);
       replicationService.setReplicationDefinitionPersister(replicationDefinitionPersister);
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
       assertNull(replicationAction.getRemoteTransferReport());
    }

}