package org.alfresco.service.cmr.replication;

import org.alfresco.repo.replication.ReplicationDefinitionPersister;
import org.alfresco.service.NotAuditable;
import org.alfresco.service.PublicService;

/**
 * The Replication service.
 * @author Nick Burch
 */
public interface ReplicationService extends ReplicationDefinitionPersister {
   /**
    * Creates a new {@link ReplicationDefinition} and sets the replication
    *  name and description to the specified values.
    * @param replicationName A unique identifier used to specify the created
    *  {@link ReplicationDefinition}
    * @param description A description of the replication
    * @return the created {@link ReplicationDefinition}
    */
   @NotAuditable
   ReplicationDefinition createReplicationDefinition(String replicationName, String description);
   
   /**
    * Runs the specified replication.
    * @param replicationDefinition The replication to run
    */
   @NotAuditable
   void replicate(ReplicationDefinition replicationDefinition);
   
   /**
    * Turns on scheduling for the specified replication. You can
    *  then set the scheduling details on the definition.
    */
   @NotAuditable
   void enableScheduling(ReplicationDefinition replicationDefinition);
   
   /**
    * Turns off scheduling for the specified replication 
    */
   @NotAuditable
   void disableScheduling(ReplicationDefinition replicationDefinition);
   
   /**
    * Is the replication service enabled?
    */
   boolean isEnabled();
}
