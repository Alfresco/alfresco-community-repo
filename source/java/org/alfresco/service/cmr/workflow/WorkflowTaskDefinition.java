/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.service.cmr.workflow;

import org.alfresco.service.cmr.dictionary.TypeDefinition;


/**
 * Workflow Task Definition Data Object.
 * 
 * Represents meta-data for a Workflow Task.  The meta-data is described in terms
 * of the Alfresco Data Dictionary.
 * 
 * @author davidc
 */
public class WorkflowTaskDefinition
{
    /** Unique id of Workflow Task Definition */
    public String id;

    /** Workflow Node this task created from */
    public WorkflowNode node;
    
    /** Task Metadata */
    public TypeDefinition metadata;

    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return "WorkflowTaskDefinition[id=" + id + ",metadata=" + metadata + "]";
    }
}
