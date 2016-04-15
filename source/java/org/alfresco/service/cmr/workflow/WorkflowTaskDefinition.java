/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.service.cmr.workflow;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.cmr.dictionary.TypeDefinition;


/**
 * Workflow Task Definition Data Object.
 * 
 * Represents meta-data for a Workflow Task.  The meta-data is described in terms
 * of the Alfresco Data Dictionary.
 * 
 * @author davidc
 */
@AlfrescoPublicApi
public class WorkflowTaskDefinition
{
    /** Unique id of Workflow Task Definition */
    @Deprecated
    public String id;

    /** Workflow Node this task created from */
    @Deprecated
    public WorkflowNode node;
    
    /** Task Metadata */
    @Deprecated
    public TypeDefinition metadata;

    public WorkflowTaskDefinition(String id, WorkflowNode node, TypeDefinition metadata)
    {
        this.id = id;
        this.node = node;
        this.metadata = metadata;
    }

    /**
     * @return the id
     */
    public String getId()
    {
        return id;
    }
    
    /**
     * @return the metadata
     */
    public TypeDefinition getMetadata()
    {
        return metadata;
    }
    
    /**
     * @return the node
     */
    public WorkflowNode getNode()
    {
        return node;
    }
    
    /**
     * 
    * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "WorkflowTaskDefinition[id=" + id + ",metadata=" + metadata + "]";
    }
}
