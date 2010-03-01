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


/**
 * Workflow Node Data Object
 * 
 * Represents a Node within the Workflow Definition.
 * 
 * @author davidc
 */
public class WorkflowNode
{
    /** Workflow Node Name */
    public String name;
    
    /** Workflow Node Title (Localised) */
    public String title;
    
    /** Workflow Node Description (Localised) */
    public String description;

    /** Type of the Workflow Node (typically this is BPM engine specific - informational only */
    public String type;

    /** Does this Workflow Node represent human interaction? */
    public boolean isTaskNode;
    
    /** The transitions leaving this node (or null, if none) */
    public WorkflowTransition[] transitions;
    
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        String transitionsArray = "{";
        for (int i = 0; i < transitions.length; i++)
        {
            transitionsArray += ((i == 0) ? "" : ",") + "'" + transitions[i] + "'";  
        }
        transitionsArray += "}";
        return "WorkflowNode[title=" + title + ",type=" + type + ",transitions=" + transitionsArray + "]";
    }
}
