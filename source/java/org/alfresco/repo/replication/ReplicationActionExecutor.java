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
import org.alfresco.repo.action.ActionCancelledException;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.repo.transfer.ChildAssociatedNodeFinder;
import org.alfresco.repo.transfer.ContentClassFilter;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionTrackingService;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.replication.ReplicationDefinition;
import org.alfresco.service.cmr.replication.ReplicationService;
import org.alfresco.service.cmr.replication.ReplicationServiceException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.transfer.NodeCrawler;
import org.alfresco.service.cmr.transfer.NodeCrawlerFactory;
import org.alfresco.service.cmr.transfer.TransferCallback;
import org.alfresco.service.cmr.transfer.TransferCancelledException;
import org.alfresco.service.cmr.transfer.TransferDefinition;
import org.alfresco.service.cmr.transfer.TransferEvent;
import org.alfresco.service.cmr.transfer.TransferEventBegin;
import org.alfresco.service.cmr.transfer.TransferEventEnterState;
import org.alfresco.service.cmr.transfer.TransferEventReport;
import org.alfresco.service.cmr.transfer.TransferService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Nick Burch
 * @since 3.4
 */
public class ReplicationActionExecutor extends ActionExecuterAbstractBase {
   /**
    * The logger
    */
   private static Log logger = LogFactory.getLog(ReplicationActionExecutor.class);
   
   private JobLockService jobLockService;
   private TransferService transferService;
   private ReplicationService replicationService;
   private NodeCrawlerFactory nodeCrawlerFactory;
   private ActionTrackingService actionTrackingService;
   
   /**
    * By default, we lock for 30 minutes
    */
   private long replicationActionLockDuration = 30*60*1000;

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

   /**
    * Injects the ActionTrackingService bean.
    * 
    * @param actionTrackingService the ActionTrackingService.
    */
   public void setActionTrackingService(ActionTrackingService actionTrackingService)
   {
       this.actionTrackingService = actionTrackingService;
   }

   @Override
   protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
      // Not used - our definitions hold everything on them
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
//      transferDefinition.setReadOnly(true); // TODO Make read only, but then need to fix tests
      
      return transferDefinition;
   }
   
   @Override
   protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
      // Specialise the action if needed, eg when loaded directly from
      //  the NodeRef without going via the replication service
      if(action.getActionDefinitionName().equals(ReplicationDefinitionImpl.EXECUTOR_NAME))
      {
         action = new ReplicationDefinitionImpl(action);
      }
      
      // Off we go
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
      if(!replicationDef.isEnabled())
      {
         throw new ReplicationServiceException("Unable to execute a disabled replication definition");
      }
      
      // Clear the previous transfer report references
      replicationDef.setLocalTransferReport(null);
      replicationDef.setRemoteTransferReport(null);
      
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
         lock.close();
         throw new ReplicationServiceException("Error processing payload list - " + e.getMessage(), e);
      }

      // Holder for reports generated by the transfer
      ReplicationReportCollector reports = new ReplicationReportCollector();
      
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
               lock, reports
         );
         
         // Record the details of the transfer reports
         replicationDef.setLocalTransferReport(reports.getLocalReport());
         replicationDef.setRemoteTransferReport(reports.getRemoteReport());
      } catch(Exception e) {
         if(! (e instanceof TransferCancelledException))
         {
            lock.close();
            throw new ReplicationServiceException("Error executing transfer - " + e.getMessage(), e);
         }
      }
      
      // All done, release our lock
      lock.close();
      
      // If we were cancelled, throw the magic exception so
      //  that this is correctly recorded
      if(actionTrackingService.isCancellationRequested(replicationDef))
      {
         throw new ActionCancelledException(replicationDef);
      }
   }

   /**
    * A {@link TransferCallback} which periodically renews the
    *  lock held against a {@link ReplicationDefinition} 
    */
   protected class ReplicationDefinitionLockExtender implements TransferCallback 
   {
      private ReplicationDefinition replicationDef;
      private String transferId;
      private String lockToken;
      
      protected ReplicationDefinitionLockExtender(ReplicationDefinition replicationDef)
      {
         this.replicationDef = replicationDef;
         acquireLock();
      }
      /**
       * No matter what the event is, refresh
       *  our lock on the {@link ReplicationDefinition}, and
       *  handle a cancel if it was requested.
       */
      public void processEvent(TransferEvent event) 
      {
         // Extend our lock
         refreshLock();

         // If it's the enter event, do skip
         if(event instanceof TransferEventEnterState)
         {
            return;
         }
         
         // If this is a begin event, make a note of the ID
         if(event instanceof TransferEventBegin)
         {
            transferId = ((TransferEventBegin)event).getTransferId();
         }
         
         // Has someone tried to cancel us?
         if(actionTrackingService.isCancellationRequested(replicationDef))
         {
            // Tell the transfer service to cancel, if we can
            if(transferId != null)
            {
               transferService.cancelAsync(transferId);
            }
            else
            {
               logger.warn("Unable to cancel replication as requested, as transfer has yet to reach a cancellable state");
            }
         }
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
                  replicationDef.getReplicationQName(),
                  replicationActionLockDuration,
                  5 * 1000, // Every 5 seconds
                  6         // 6 times = wait up to 30 seconds
            );
         } catch(LockAcquisitionException e) {
            // Long try - every 30 seconds
            lockToken = jobLockService.getLock(
                  replicationDef.getReplicationQName(),
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
               replicationDef.getReplicationQName(),
               replicationActionLockDuration
         );
      }
      private void releaseLock()
      {
         jobLockService.releaseLock(
               lockToken,
               replicationDef.getReplicationQName()
         );
      }
   }
   
   /**
    * A {@link TransferCallback} which collects the various reports generated by
    * the transfer. 
    */
   protected class ReplicationReportCollector implements TransferCallback 
   {
      private NodeRef localReport;
      private NodeRef remoteReport;
      
      protected ReplicationReportCollector()
      {
      }
      
      /**
       * Collect source and destination repository target reports
       */
      public void processEvent(TransferEvent event) 
      {
         if(event instanceof TransferEventReport)
         {
             TransferEventReport reportEvent = (TransferEventReport)event;
             if (reportEvent.getReportType().equals(TransferEventReport.ReportType.SOURCE))
             {
                 localReport = reportEvent.getNodeRef();
             }
             else if (reportEvent.getReportType().equals(TransferEventReport.ReportType.DESTINATION))
             {
                 remoteReport = reportEvent.getNodeRef();
             }
         }
      }

      /**
       * @return local transfer report
       */
      public NodeRef getLocalReport()
      {
          return localReport;
      }
      
      /**
       * @return target transfer report
       */
      public NodeRef getRemoteReport()
      {
          return remoteReport;
      }
   }
}
