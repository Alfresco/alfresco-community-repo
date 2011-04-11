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
import org.alfresco.service.cmr.action.scheduled.ScheduledPersistedAction;
import org.alfresco.service.cmr.action.scheduled.ScheduledPersistedActionService;
import org.alfresco.service.cmr.replication.ReplicationDefinition;
import org.alfresco.service.cmr.replication.ReplicationService;
import org.alfresco.service.cmr.replication.ReplicationServiceException;
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
   private ScheduledPersistedActionService scheduledPersistedActionService;
   private ReplicationParams replicationParams;
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
    * Injects the ActionService bean.
    * @param actionService
    */
   public void setActionService(ActionService actionService)
   {
       this.actionService = actionService;
   }

   /**
    * Injects the Scheduled Persisted Action Service bean
    * @param scheduledPersistedActionService
    */
   public void setScheduledPersistedActionService(ScheduledPersistedActionService scheduledPersistedActionService) 
   {
      this.scheduledPersistedActionService = scheduledPersistedActionService;
   }

   /*
    * (non-Javadoc)
    * @see
    * org.alfresco.service.cmr.replication.ReplicationService#createReplicationDefinition
    * (org.alfresco.service.namespace.QName, java.lang.String)
    */
   public ReplicationDefinition createReplicationDefinition(
         String replicationDefinitionName, String description) {
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
   public ReplicationDefinition loadReplicationDefinition(String replicationDefinitionName) {
      ReplicationDefinitionImpl rd = (ReplicationDefinitionImpl)
            replicationDefinitionPersister.loadReplicationDefinition(replicationDefinitionName);
      if(rd != null) {
         rd.setSchedule(
               scheduledPersistedActionService.getSchedule(rd)
         );
      }
      return rd;
   }
   
   private List<ReplicationDefinition> attachSchedules(List<ReplicationDefinition> definitions) {
      for(ReplicationDefinition rd : definitions) {
         if(rd != null) {
            ReplicationDefinitionImpl rdi = (ReplicationDefinitionImpl)rd;
            rdi.setSchedule(
                  scheduledPersistedActionService.getSchedule(rdi)
            );
         }
      }
      return definitions;
   }

   /*
    * (non-Javadoc)
    * @see
    * org.alfresco.service.cmr.replication.ReplicationService#loadReplicationDefinitions()
    */
   public List<ReplicationDefinition> loadReplicationDefinitions() {
      return attachSchedules( replicationDefinitionPersister.loadReplicationDefinitions() );
   }

   /*
    * (non-Javadoc)
    * @see
    * org.alfresco.service.cmr.replication.ReplicationService#loadReplicationDefinitions
    * (String)
    */
   public List<ReplicationDefinition> loadReplicationDefinitions(String target) {
      return attachSchedules( replicationDefinitionPersister.loadReplicationDefinitions(target) );
   }
   
   /*
    * (non-Javadoc)
    * @see
    * org.alfresco.service.cmr.replication.ReplicationService#renameReplicationDefinition
    * (String,String)
    */
   public void renameReplicationDefinition(String oldReplicationName, String newReplicationName) 
   {
      replicationDefinitionPersister.renameReplicationDefinition(oldReplicationName, newReplicationName);
   }

   /*
    * (non-Javadoc)
    * @see
    * org.alfresco.service.cmr.replication.ReplicationService#saveReplicationDefinition
    * (ReplicationDefinition)
    */
   public void saveReplicationDefinition(
         ReplicationDefinition replicationDefinition) {
      // Save the replication definition
      replicationDefinitionPersister.saveReplicationDefinition(replicationDefinition);
      
      // If required, now also save the schedule for it
      if(replicationDefinition.isSchedulingEnabled())
      {
         scheduledPersistedActionService.saveSchedule(
               ((ReplicationDefinitionImpl)replicationDefinition).getSchedule()
         );
      }
   }

   /*
    * (non-Javadoc)
    * @see
    * org.alfresco.service.cmr.replication.ReplicationService#deleteReplicationDefinition
    * (ReplicationDefinition)
    */
   public void deleteReplicationDefinition(
         ReplicationDefinition replicationDefinition) {
      if(replicationDefinition.isSchedulingEnabled())
      {
         scheduledPersistedActionService.deleteSchedule(
               ((ReplicationDefinitionImpl)replicationDefinition).getSchedule()
         );
      }
      replicationDefinitionPersister.deleteReplicationDefinition(replicationDefinition);
   }

   /*
    * (non-Javadoc)
    * @see..
    * org.alfresco.service.cmr.replication.ReplicationService#replication
    * (ReplicationDefinition)
    */
    public void replicate(ReplicationDefinition replicationDefinition) 
    {
      
        if(isEnabled())
        {
            actionService.executeAction(
                replicationDefinition,
                ReplicationDefinitionPersisterImpl.REPLICATION_ACTION_ROOT_NODE_REF);
        }
        else
        {
            throw new ReplicationServiceException("Unable to replicate. The replication service is not enabled");
        }
    }

   public void disableScheduling(ReplicationDefinition replicationDefinition) {
      ReplicationDefinitionImpl definition = (ReplicationDefinitionImpl)replicationDefinition; 
      if(replicationDefinition.isSchedulingEnabled())
      {
         scheduledPersistedActionService.deleteSchedule(
               definition.getSchedule()
         );
      }
      definition.setSchedule(null);
   }

   public void enableScheduling(ReplicationDefinition replicationDefinition) {
      if(!replicationDefinition.isSchedulingEnabled())
      {
         ScheduledPersistedAction schedule = 
            scheduledPersistedActionService.createSchedule(replicationDefinition);
         ((ReplicationDefinitionImpl)replicationDefinition).setSchedule(schedule);
      }
   }

    @Override
    public boolean isEnabled()
    {
        if(replicationParams != null)
        {
            return replicationParams.isEnabled();
        }    
        return true;
    }
    
    /**
     * Sets Replication Parameters
     *  
     * @param replicationParams  replication parameters
     */
    public void setReplicationParams(ReplicationParams replicationParams)
    {
        this.replicationParams = replicationParams;
    }
}
