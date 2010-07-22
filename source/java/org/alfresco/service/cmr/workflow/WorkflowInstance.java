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
package org.alfresco.service.cmr.workflow;

import java.io.Serializable;
import java.util.Date;

import org.alfresco.service.cmr.repository.NodeRef;


/**
 * Workflow Instance Data Object
 * 
 * Represents an "in-flight" workflow.
 * 
 * @author davidc
 */
public class WorkflowInstance implements Serializable
{
    private static final long serialVersionUID = 4221926809419223452L;

    /** Workflow Instance unique id */
    public String id;

    /** Workflow Instance description */
    public String description;

    /** Is this Workflow instance still "in-flight" or has it completed? */
    public boolean active;

    /** Initiator (cm:person) - null if System initiated */
    public NodeRef initiator;
    
    /** Workflow Start Date */
    public Date startDate;
    
    /** Workflow End Date */
    public Date endDate;

    /** Workflow Package */
    public NodeRef workflowPackage;
    
    /** Workflow Context */
    public NodeRef context;
    
    /** Workflow Definition */
    public WorkflowDefinition definition;

    /**
     * @return the serialversionuid
     */
    public static long getSerialversionuid()
    {
        return serialVersionUID;
    }

    /**
     * @return the id
     */
    public String getId()
    {
        return id;
    }

    /**
     * @return the description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * @return the active
     */
    public boolean isActive()
    {
        return active;
    }

    /**
     * @return the initiator
     */
    public NodeRef getInitiator()
    {
        return initiator;
    }

    /**
     * @return the startDate
     */
    public Date getStartDate()
    {
        return startDate;
    }

    /**
     * @return the endDate
     */
    public Date getEndDate()
    {
        return endDate;
    }

    /**
     * @return the workflowPackage
     */
    public NodeRef getWorkflowPackage()
    {
        return workflowPackage;
    }

    /**
     * @return the context
     */
    public NodeRef getContext()
    {
        return context;
    }

    /**
     * @return the definition
     */
    public WorkflowDefinition getDefinition()
    {
        return definition;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "WorkflowInstance[id=" + id + ",active=" + active + ",def=" + definition.toString() + "]";
    }
}
