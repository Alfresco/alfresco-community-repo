
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