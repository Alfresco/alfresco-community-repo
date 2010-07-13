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

import java.util.HashSet;
import java.util.List;
import java.util.Set;


import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.repo.transfer.ChildAssociatedNodeFinder;
import org.alfresco.repo.transfer.ContentClassFilter;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.replication.ReplicationDefinition;
import org.alfresco.service.cmr.replication.ReplicationService;
import org.alfresco.service.cmr.replication.ReplicationServiceException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.transfer.NodeCrawler;
import org.alfresco.service.cmr.transfer.NodeCrawlerFactory;
import org.alfresco.service.cmr.transfer.TransferCallback;
import org.alfresco.service.cmr.transfer.TransferDefinition;
import org.alfresco.service.cmr.transfer.TransferEvent;
import org.alfresco.service.cmr.transfer.TransferService;

/**
 * @author Nick Burch
 * @since 3.4
 */
public class ReplicationActionExecutor extends ActionExecuterAbstractBase {
   private NodeService nodeService;
   private JobLockService jobLockService;
   private TransferService transferService;
   private ReplicationService replicationService;
   private NodeCrawlerFactory nodeCrawlerFactory;
   
   /**
    * By default, we lock for 30 minutes
    */
   private long replicationActionLockDuration = 30*60*1000;

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
    * @param jobLockService the JobLockService.
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

   /**
    * Injects the NodeCrawlerFactory bean.
    * 
    * @param nodeCrawlerFactory the NodeCrawlerFactory.
    */
   public void setNodeCrawlerFactory(NodeCrawlerFactory nodeCrawlerFactory)
   {
       this.nodeCrawlerFactory = nodeCrawlerFactory;
   }

   @Override
   protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
      // TODO Is this needed?
   }
   
   /**
    * Takes a {@link ReplicationDefinition}, which contains one or
    *  more payloads {@link NodeRef}s, and expands them into a
    *  full list of nodes to be transfered.
    */
   protected Set<NodeRef> expandPayload(ReplicationDefinition replicationDef) {
      // Turn our payload list of root nodes into something that
      //  the transfer service can work with
      Set<NodeRef> toTransfer = new HashSet<NodeRef>(89);

      NodeCrawler crawler = nodeCrawlerFactory.getNodeCrawler(); 
      crawler.setNodeFinders(new ChildAssociatedNodeFinder(ContentModel.ASSOC_CONTAINS));
      crawler.setNodeFilters(new ContentClassFilter(
            ContentModel.TYPE_FOLDER,
            ContentModel.TYPE_CONTENT
      ));
      
      for(NodeRef payload : replicationDef.getPayload()) {
         Set<NodeRef> crawledNodes = crawler.crawl(payload);
         toTransfer.addAll(crawledNodes);
      }
      
      return toTransfer;
   }
   /**
    * Takes a {@link ReplicationDefinition} and a list of
    *  {@link NodeRef}s, and returns the 
    *  {@link TransferDefinition} which will allow the
    *  replication to be run.
    */
   protected TransferDefinition buildTransferDefinition(
         ReplicationDefinition replicationDef, Set<NodeRef> toTransfer
   ) {
      TransferDefinition transferDefinition =
         new TransferDefinition();
      transferDefinition.setNodes(toTransfer);
      transferDefinition.setSync(true);
      
      return transferDefinition;
   }
   
   @Override
   protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
      final ReplicationDefinition replicationDef = (ReplicationDefinition)action;
      if(replicationDef.getTargetName() == null ||
            replicationDef.getTargetName().equals(""))
      {
         throw new ReplicationServiceException("The target is required but wasn't given");
      }
      if(replicationDef.getPayload().size() == 0)
      {
         throw new ReplicationServiceException("No payloads were specified");
      }
      
      // Lock the service - only one instance of the replication
      //  should occur at a time
      ReplicationDefinitionLockExtender lock =
         new ReplicationDefinitionLockExtender(replicationDef);
      
      // Turn our payload list of root nodes into something that
      //  the transfer service can work with
      Set<NodeRef> toTransfer;
      try {
         toTransfer = expandPayload(replicationDef);
      } catch(Exception e) {
         // TODO - Record the error
         System.err.println(e);
         lock.close();
         throw new ReplicationServiceException("Error processing payload list", e);
      }
      
      // Ask the transfer service to do the replication
      //  work for us
      try {
         // Build the definition
         TransferDefinition transferDefinition =
            buildTransferDefinition(replicationDef, toTransfer);
         
         // Off we go
         transferService.transfer(
               replicationDef.getTargetName(),
               transferDefinition,
               lock
         );
      } catch(Exception e) {
         // TODO - Record the error
         System.err.println(e);
         lock.close();
         throw new ReplicationServiceException("Error executing transfer", e);
      }
      
      // All done
      lock.close();
   }

   /**
    * A {@link TransferCallback} which periodically renews the
    *  lock held against a {@link ReplicationDefinition} 
    */
   protected class ReplicationDefinitionLockExtender implements TransferCallback 
   {
      private ReplicationDefinition replicationDef;
      private String lockToken;
      
      protected ReplicationDefinitionLockExtender(ReplicationDefinition replicationDef)
      {
         this.replicationDef = replicationDef;
         acquireLock();
      }
      /**
       * No matter what the event is, refresh
       *  our lock on the {@link ReplicationDefinition}
       */
      public void processEvent(TransferEvent event) 
      {
         refreshLock();
      }
      /**
       * Give up our lock on the 
       *  {@link ReplicationDefinition}
       */
      public void close()
      {
         releaseLock();
      }
      
      /**
       * Get a lock on the job.
       * Tries every 5 seconds for 30 seconds, then
       *  every 30 seconds until 3 times the lock
       *  duration. 
       */
      private void acquireLock()
      {
         long retryTime = 30*1000;
         int retries = (int)(replicationActionLockDuration * 3 / retryTime); 
         
         try {
            // Quick try
            lockToken = jobLockService.getLock(
                  replicationDef.getReplicationName(),
                  replicationActionLockDuration,
                  5 * 1000, // Every 5 seconds
                  6         // 6 times = wait up to 30 seconds
            );
         } catch(LockAcquisitionException e) {
            // Long try - every 30 seconds
            lockToken = jobLockService.getLock(
                  replicationDef.getReplicationName(),
                  replicationActionLockDuration,
                  retryTime,
                  retries
            );
         }
      }
      private void refreshLock()
      {
         jobLockService.refreshLock(
               lockToken,
               replicationDef.getReplicationName(),
               replicationActionLockDuration
         );
      }
      private void releaseLock()
      {
         jobLockService.releaseLock(
               lockToken,
               replicationDef.getReplicationName()
         );
      }
   }
}
