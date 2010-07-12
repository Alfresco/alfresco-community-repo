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

import java.util.List;

import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.replication.ReplicationDefinition;
import org.alfresco.service.cmr.replication.ReplicationService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.transfer.TransferService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 * @author Nick Burch
 * @since 3.4
 */
public class ReplicationServiceImpl implements ReplicationService, ReplicationDefinitionPersister {
   private static final Log log = LogFactory.getLog(ReplicationServiceImpl.class);

   private ActionService actionService;
   private DictionaryService dictionaryService;
   private TransferService transferService;
   private NodeService nodeService;
   
   private ReplicationDefinitionPersisterImpl replicationDefinitionPersister;
   
   /**
    * Injects the ReplicationDefinitionPersister bean.
    * @param replicationDefinitionPersister
    */
   public void setReplicationDefinitionPersister(ReplicationDefinitionPersisterImpl replicationDefinitionPersister)
   {
       this.replicationDefinitionPersister = replicationDefinitionPersister;
   }
   
   /**
    * Injects the TransferService bean
    * @param transferService
    */
   public void setTransferService(TransferService transferService)
   {
       this.transferService = transferService;
   }
   
   /**
    * Injects the NodeService bean.
    * @param nodeService
    */
   public void setNodeService(NodeService nodeService)
   {
       this.nodeService = nodeService;
   }

   /**
    * Injects the ActionService bean.
    * @param actionService
    */
   public void setActionService(ActionService actionService)
   {
       this.actionService = actionService;
   }

   /**
    * Injects the DictionaryService bean.
    * @param dictionaryService
    */
   public void setDictionaryService(DictionaryService dictionaryService)
   {
       this.dictionaryService = dictionaryService;
   }

   /*
    * (non-Javadoc)
    * @see
    * org.alfresco.service.cmr.replication.ReplicationService#createReplicationDefinition
    * (org.alfresco.service.namespace.QName, java.lang.String)
    */
   public ReplicationDefinition createReplicationDefinition(
         QName replicationDefinitionName, String description) {
      if (log.isDebugEnabled())
      {
          StringBuilder msg = new StringBuilder();
          msg.append("Creating replication definition ")
              .append(replicationDefinitionName);
          log.debug(msg.toString());
      }
      return new ReplicationDefinitionImpl(GUID.generate(), replicationDefinitionName, description);
   }

   /*
    * (non-Javadoc)
    * @see
    * org.alfresco.service.cmr.replication.ReplicationService#loadReplicationDefinition
    * (org.alfresco.service.namespace.QName)
    */
   public ReplicationDefinition loadReplicationDefinition(QName replicationDefinitionName) {
      return replicationDefinitionPersister.loadReplicationDefinition(replicationDefinitionName);
   }

   /*
    * (non-Javadoc)
    * @see
    * org.alfresco.service.cmr.replication.ReplicationService#loadReplicationDefinitions()
    */
   public List<ReplicationDefinition> loadReplicationDefinitions() {
      return replicationDefinitionPersister.loadReplicationDefinitions();
   }

   /*
    * (non-Javadoc)
    * @see
    * org.alfresco.service.cmr.replication.ReplicationService#loadReplicationDefinitions
    * (String)
    */
   public List<ReplicationDefinition> loadReplicationDefinitions(String target) {
      return replicationDefinitionPersister.loadReplicationDefinitions(target); // TODO is this right
   }

   /*
    * (non-Javadoc)
    * @see
    * org.alfresco.service.cmr.replication.ReplicationService#saveReplicationDefinition
    * (ReplicationDefinition)
    */
   public void saveReplicationDefinition(
         ReplicationDefinition replicationDefinition) {
      replicationDefinitionPersister.saveReplicationDefinition(replicationDefinition);
   }

   /*
    * (non-Javadoc)
    * @see
    * org.alfresco.service.cmr.replication.ReplicationService#deleteReplicationDefinition
    * (ReplicationDefinition)
    */
   public void deleteReplicationDefinition(
         ReplicationDefinition replicationDefinition) {
      replicationDefinitionPersister.deleteReplicationDefinition(replicationDefinition);
   }

   /*
    * (non-Javadoc)
    * @see
    * org.alfresco.service.cmr.replication.ReplicationService#replication
    * (ReplicationDefinition)
    */
   public void replicate(ReplicationDefinition replicationDefinition) {
      actionService.executeAction(
            replicationDefinition,
            ReplicationDefinitionPersisterImpl.REPLICATION_ACTION_ROOT_NODE_REF
      );
   }
}
