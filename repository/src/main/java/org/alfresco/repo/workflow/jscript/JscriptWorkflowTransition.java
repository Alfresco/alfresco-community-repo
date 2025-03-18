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
package org.alfresco.repo.workflow.jscript;

import java.io.Serializable;

import org.alfresco.service.cmr.workflow.WorkflowTransition;

public class JscriptWorkflowTransition implements Serializable
{
    static final long serialVersionUID = 8370298400161156357L;

    /** Workflow transition id */
    private String id;

    /** Localised workflow transition title */
    private String title;

    /** Localised workflow transition description */
    private String description;

    /**
     * Constructor to create a new instance of this class from scratch
     * 
     * @param id
     *            Workflow transition ID
     * @param title
     *            Workflow transition title
     * @param description
     *            Workflow transition description
     */
    public JscriptWorkflowTransition(String id, String title, String description)
    {
        this.id = id;
        this.title = title;
        this.description = description;
    }

    /**
     * Constructor to create a new instance of this class from an existing instance of WorkflowTransition from the CMR workflow object model
     * 
     * @param transition
     *            CMR WorkflowTransition object from which to create a new instance of this class
     */
    public JscriptWorkflowTransition(WorkflowTransition transition)
    {
        this.id = transition.id;
        this.title = transition.title;
        this.description = transition.description;
    }

    /**
     * Gets the value of the <code>id</code> property
     *
     * @return the id
     */
    public String getId()
    {
        return id;
    }

    /**
     * Gets the value of the <code>title</code> property
     *
     * @return the title
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Gets the value of the <code>description</code> property
     *
     * @return the description
     */
    public String getDescription()
    {
        return description;
    }

    /* (non-Javadoc)
     * 
     * @see java.lang.Object#toString() */
    public String toString()
    {
        return "JscriptWorkflowTransition[id=" + id + ",title=" + title + ",description=" + description + "]";
    }
}
