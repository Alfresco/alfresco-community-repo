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
package org.alfresco.service.cmr.replication;

import java.io.Serializable;
import java.util.List;

import org.alfresco.service.cmr.action.CancellableAction;
import org.alfresco.service.cmr.action.scheduled.SchedulableAction;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * This class is used to fully specify an inter-repository replication. It 
 * specifies which node(s) should be transfered, which associated content
 * should be sent with them, and which target they should be sent to. 
 * <P/>
 * Every ReplicationDefinition has a <code>replicationName</code> attribute 
 * which uniquely identifies it. It also has a single target.
 * 
 * @author Nick Burch
 */
public interface ReplicationDefinition extends CancellableAction, SchedulableAction, Serializable {
   /**
    * @return the name which uniquely identifies this replication definition.
    */
   String getReplicationName();
   
   /**
    * @return the qualified name which uniquely identifies this replication definition.
    */
   QName getReplicationQName();
   
   /**
    * @return the name of the target repository.
    */
   String getTargetName();

   /**
    * Sets the name of the target repository.
    */
   void setTargetName(String targetName);

   /**
    * Is this Replication Definition currently
    *  enabled (can be run), or disabled
    *  (can't be run)?
    * @return Whether the definition is enabled or not
    */
   boolean isEnabled();
   
   /**
    * Enable or Disable the Replication
    *  Definition.
    */
   void setEnabled(boolean enabled);
   
   /**
    * The list of Nodes to be transfered. This
    *  list can be edited as required.
    * @return An editable list of the nodes to be transfered
    */
   List<NodeRef> getPayload();
   
   /**
    * Returns the local side of the report on
    *  the transfer.
    * The transfer service generates two reports,
    *  one on the local repository, and one on the
    *  remote repository. This returns the 
    *  local version of the report.
    * @return The transfer report on the local repository
    */
   NodeRef getLocalTransferReport();
   
   /**
    * Records the location on the local repository
    *  of the transfer service report on the
    *  replication.
    */
   void setLocalTransferReport(NodeRef report);
   
   /**
    * Returns the remote side of the report on
    *  the transfer.
    * The transfer service generates two reports,
    *  one on the local repository, and one on the
    *  remote repository. This returns the 
    *  remote version of the report.
    * @return The transfer report on the remote repository
    */
   NodeRef getRemoteTransferReport();
   
   /**
    * Records the location on the local repository
    *  of the transfer service report that was
    *  generated on the remote repository for the
    *  replication.
    */
   void setRemoteTransferReport(NodeRef report);
   
   /**
    * Is scheduling currently enabled?
    * See {@link ReplicationService#enableScheduling(ReplicationDefinition)} and
    * {@link ReplicationService#disableScheduling(ReplicationDefinition)}
    */
   boolean isSchedulingEnabled();
   
   // TODO Replication options, such as permissions and rules
}
