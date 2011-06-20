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
