/*
 * Copyright (C) 2005 Alfresco, Inc.
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

import java.util.Date;

import org.alfresco.service.cmr.repository.NodeRef;


/**
 * Workflow Instance Data Object
 * 
 * Represents an "in-flight" workflow.
 * 
 * @author davidc
 */
public class WorkflowInstance
{
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

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return "WorkflowInstance[id=" + id + ",active=" + active + ",def=" + definition.toString() + "]";
    }
}
