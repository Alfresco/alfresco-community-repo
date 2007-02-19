/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
