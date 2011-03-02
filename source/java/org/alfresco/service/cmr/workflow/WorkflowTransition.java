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
 * Workflow Transition.
 * 
 * @author davidc
 */
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
