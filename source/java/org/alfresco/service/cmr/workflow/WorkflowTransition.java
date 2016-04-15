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

/**
 * Workflow Transition.
 * 
 * @author davidc
 */
@AlfrescoPublicApi
public class WorkflowTransition
{
    /** Transition Id */
    @Deprecated
    public String id;

    /** Transition Title (Localised) */
    @Deprecated
    public String title;
    
    /** Transition Description (Localised) */
    @Deprecated
    public String description;
    
    /** Is this the default transition */
    @Deprecated
    public boolean isDefault;

    public WorkflowTransition(String id, String title, String description, boolean isDefault)
    {
        this.id = id;
        this.title = title;
        this.description = description;
        this.isDefault = isDefault;
    }

    /**
     * @return the id
     */
    public String getId()
    {
        return id;
    }
    
    /**
     * @return the title
     */
    public String getTitle()
    {
        return title;
    }
    
    /**
     * @return the description
     */
    public String getDescription()
    {
        return description;
    }
    
    /**
     * @return the isDefault
     */
    public boolean isDefault()
    {
        return isDefault;
    }
    
    /**
    * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "WorkflowTransition[id=" + id + ",title=" + title + "]";
    }
}
