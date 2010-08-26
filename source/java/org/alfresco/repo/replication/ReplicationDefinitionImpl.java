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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.repo.action.ActionImpl;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.scheduled.ScheduledPersistedAction;
import org.alfresco.service.cmr.replication.ReplicationDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * @author Nick Burch
 * @since 3.4
 */
public class ReplicationDefinitionImpl extends ActionImpl implements ReplicationDefinition
{
    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 3183721054220388564L;

    public static final String EXECUTOR_NAME = "replicationActionExecutor";
    public static final String REPLICATION_DEFINITION_NAME = "replicationActionName";
    public static final String REPLICATION_DEFINITION_TARGET = "replicationTarget";
    public static final String REPLICATION_DEFINITION_PAYLOAD = "replicationPayload";
    public static final String REPLICATION_DEFINITION_ENABLED = "replicationDefinitionEnabled";
    public static final String REPLICATION_DEFINITION_LOCAL_TRANSFER_REPORT = "replicationTransferLocalReport";
    public static final String REPLICATION_DEFINITION_REMOTE_TRANSFER_REPORT = "replicationTransferRemoteReport";

    private ScheduledPersistedAction schedule;
    
    /**
     * @param id
     *            the action id
     * @param replicationName
     *            a unique name for the replication action.
     */
    public ReplicationDefinitionImpl(String id, String replicationName)
    {
        this(id, replicationName, null);
    }

    /**
     * @param id
     *            the action id
     * @param replicationName
     *            a unique name for the replication action.
     * @param description
     *            a description of the replication
     */
    public ReplicationDefinitionImpl(String id, String replicationName, String description)
    {
        super(null, id, EXECUTOR_NAME);
        setReplicationQName(QName.createQName(null, replicationName));
        setDescription(description);
    }

    public ReplicationDefinitionImpl(Action action)
    {
        super(action);
    }

    /*
     * @see org.alfresco.service.cmr.replication.ReplicationDefinition#getReplicationName()
     */
    public String getReplicationName()
    {
        return getReplicationQName().getLocalName();
    }
    
    /*
     * @see org.alfresco.service.cmr.replication.ReplicationDefinition#getReplicationQName()
     */
    public QName getReplicationQName()
    {
        Serializable parameterValue = getParameterValue(REPLICATION_DEFINITION_NAME);
        return (QName)parameterValue;
    }
    
    /*
     * Sets or changes the replication name, which should match
     *  the association name in the data dictionary
     */
    protected void setReplicationQName(QName replicationName)
    {
        setParameterValue(REPLICATION_DEFINITION_NAME, replicationName);
    }
    
    /*
     * @see org.alfresco.service.cmr.replication.ReplicationDefinition#isEnabled()
     */
    public boolean isEnabled()
    {
       Serializable parameterValue = getParameterValue(REPLICATION_DEFINITION_ENABLED);
       if(parameterValue == null)
       {
          // Default is enabled
          return true;
       }
       return ((Boolean)parameterValue).booleanValue();
    }
    
    /*
     * @see org.alfresco.service.cmr.replication.ReplicationDefinition#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled)
    {
       setParameterValue(REPLICATION_DEFINITION_ENABLED, new Boolean(enabled));
    }

    /*
     * @see
     * org.alfresco.service.cmr.replication.ReplicationDefinition#getPayload()
     */
   @SuppressWarnings("unchecked")
   public List<NodeRef> getPayload() {
      // Retrieve the previous payload
      // Could be null, could be a list, or could be a single
      //  NodeRef (if the list had one entry when saved)
      Object payloadO =
         getParameterValue(REPLICATION_DEFINITION_PAYLOAD);
      
      // Ensure we always have a list, no matter what
      //  we got back
      List<NodeRef> payload;
      if(payloadO == null) {
         payload = new ArrayList<NodeRef>();
         setParameterValue(REPLICATION_DEFINITION_PAYLOAD, (Serializable)payload);
      } else {
         // If there's only one entry, comes back as just
         //  that, unwrapped from the list
         if(payloadO instanceof List) {
            payload = (List<NodeRef>)payloadO;
         } else {
            // Turn it into a list
            payload = new ArrayList<NodeRef>();
            payload.add((NodeRef)payloadO);
            // And switch to using the list from now on
            setParameterValue(REPLICATION_DEFINITION_PAYLOAD, (Serializable)payload);
         }
      }

      return payload;
   }

   /*
    * @see
    * org.alfresco.service.cmr.replication.ReplicationDefinition#getTargetName()
    */
   public String getTargetName() {
      return (String)getParameterValue(REPLICATION_DEFINITION_TARGET);
   }

   /*
    * @see
    * org.alfresco.service.cmr.replication.ReplicationDefinition#setTargetName(String)
    */
   public void setTargetName(String targetName) {
      setParameterValue(REPLICATION_DEFINITION_TARGET, targetName);
   }

   /*
    * @see
    * org.alfresco.service.cmr.replication.ReplicationDefinition#getLocalTransferReport()
    */
   public NodeRef getLocalTransferReport() {
      return (NodeRef)getParameterValue(REPLICATION_DEFINITION_LOCAL_TRANSFER_REPORT);
   }
   
   /*
    * @see
    * org.alfresco.service.cmr.replication.ReplicationDefinition#setLocalTransferReport(NodeRef)
    */
   public void setLocalTransferReport(NodeRef report) {
      setParameterValue(REPLICATION_DEFINITION_LOCAL_TRANSFER_REPORT, report);
   }

   /*
    * @see
    * org.alfresco.service.cmr.replication.ReplicationDefinition#getRemoteTransferReport()
    */
   public NodeRef getRemoteTransferReport() {
      return (NodeRef)getParameterValue(REPLICATION_DEFINITION_REMOTE_TRANSFER_REPORT);
   }
   
   /*
    * @see
    * org.alfresco.service.cmr.replication.ReplicationDefinition#setRemoteTransferReport(NodeRef)
    */
   public void setRemoteTransferReport(NodeRef report) {
      setParameterValue(REPLICATION_DEFINITION_REMOTE_TRANSFER_REPORT, report);
   }
   
   public ScheduledPersistedAction getSchedule()
   {
      return schedule;
   }
   
   public void setSchedule(ScheduledPersistedAction schedule)
   {
      this.schedule = schedule;
   }

   public Integer getScheduleIntervalCount() {
      if(schedule == null)
         return null;
      return schedule.getScheduleIntervalCount();
   }

   public IntervalPeriod getScheduleIntervalPeriod() {
      if(schedule == null)
         return null;
      return schedule.getScheduleIntervalPeriod();
   }

   public Date getScheduleStart() {
      if(schedule == null)
         return null;
      return schedule.getScheduleStart();
   }

   public void setScheduleIntervalCount(Integer count) {
      if(schedule == null)
         throw new IllegalStateException("Scheduling not enabled");
      schedule.setScheduleIntervalCount(count);
   }

   public void setScheduleIntervalPeriod(IntervalPeriod period) {
      if(schedule == null)
         throw new IllegalStateException("Scheduling not enabled");
      schedule.setScheduleIntervalPeriod(period);
   }

   public void setScheduleStart(Date startDate) {
      if(schedule == null)
         throw new IllegalStateException("Scheduling not enabled");
      schedule.setScheduleStart(startDate);
   }

   public boolean isSchedulingEnabled() {
      return (schedule != null);
   }
}
