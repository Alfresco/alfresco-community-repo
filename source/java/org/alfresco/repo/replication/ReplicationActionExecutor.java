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

import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.replication.ReplicationService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.transfer.TransferService;

/**
 * @author Nick Burch
 * @since 3.4
 */
public class ReplicationActionExecutor extends ActionExecuterAbstractBase {
   private NodeService nodeService;
   private JobLockService jobLockService;
   private ReplicationService replicationService;
   private TransferService transferService;

   /**
    * Injects the NodeService bean.
    * 
    * @param nodeService the NodeService.
    */
   public void setNodeService(NodeService nodeService)
   {
       this.nodeService = nodeService;
   }

   /**
    * Injects the JobLockService bean.
    * 
    * @param nodeService the JobLockService.
    */
   public void setJobLockService(JobLockService jobLockService)
   {
       this.jobLockService = jobLockService;
   }

   /**
    * Injects the ReplicationService bean.
    * 
    * @param nodeService the ReplicationService.
    */
   public void setReplicationService(ReplicationService replicationService)
   {
       this.replicationService = replicationService;
   }
   
   /**
    * Injects the TransferService bean.
    * 
    * @param transferService the TransferService.
    */
   public void setTransferService(TransferService transferService)
   {
       this.transferService = transferService;
   }

   @Override
   protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
      // TODO
   }
   
   @Override
   protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
      // Lock the service - only one instance of the replication
      //  should occur at a time
      
      // Turn our payload list of root nodes into something that
      //  the transfer service can work with
      
      // Ask the transfer service to do the replication
      //  work for us
      
      // TODO
   }
}
