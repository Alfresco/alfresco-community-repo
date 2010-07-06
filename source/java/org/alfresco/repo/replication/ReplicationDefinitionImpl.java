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
import java.util.List;

import org.alfresco.repo.action.ActionImpl;
import org.alfresco.service.cmr.action.Action;
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

    public static final String REPLICATION_DEFINITION_NAME = "replicationActionName";
    public static final String REPLICATION_DEFINITION_TARGET = "replicationTarget";
    public static final String REPLICATION_DEFINITION_PAYLOAD = "replicationPayload";
    public static final String REPLICATION_DEFINITION_TRANSFER_REPORT = "replicationTransferReport";

    /**
     * @param id
     *            the action id
     * @param replicationName
     *            a unique name for the replication action.
     */
    public ReplicationDefinitionImpl(String id, QName replicationName)
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
    public ReplicationDefinitionImpl(String id, QName replicationName, String description)
    {
        super(null, id, "replicationActionExecutor");
        setParameterValue(REPLICATION_DEFINITION_NAME, replicationName);
        setDescription(description);
    }

    public ReplicationDefinitionImpl(Action action)
    {
        super(action);
    }

    /*
     * @see org.alfresco.service.cmr.replication.ReplicationDefinition#getReplicationName()
     */
    public QName getReplicationName()
    {
        Serializable parameterValue = getParameterValue(REPLICATION_DEFINITION_NAME);
        return (QName) parameterValue;
    }

    /*
     * @see
     * org.alfresco.service.cmr.replication.ReplicationDefinition#getPayload()
     */
   @SuppressWarnings("unchecked")
   public List<NodeRef> getPayload() {
      List<NodeRef> payload = (List<NodeRef>)
            getParameterValue(REPLICATION_DEFINITION_PAYLOAD);
      if(payload == null) {
         payload = new ArrayList<NodeRef>();
         setParameterValue(REPLICATION_DEFINITION_PAYLOAD, (Serializable)payload);
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
      return (NodeRef)getParameterValue(REPLICATION_DEFINITION_TRANSFER_REPORT);
   }
   
   /*
    * @see
    * org.alfresco.service.cmr.replication.ReplicationDefinition#setLocalTransferReport(NodeRef)
    */
   public void setLocalTransferReport(NodeRef report) {
      setParameterValue(REPLICATION_DEFINITION_TRANSFER_REPORT, report);
   }
}
