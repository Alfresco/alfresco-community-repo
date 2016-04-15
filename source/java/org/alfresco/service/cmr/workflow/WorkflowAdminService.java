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

/**
 * Client facing API for providing administration information about the
 * {@link WorkflowService}.
 *
 * @author Gavin Cornwell
 * @since 4.0
 */
public interface WorkflowAdminService
{
    /**
     * Determines whether the engine with the given id is enabled.
     * 
     * @param engineId The id of a workflow engine
     * @return true if the engine id is valid and is enabled
     */
    boolean isEngineEnabled(String engineId);

    /**
     * Enables/disables the engine with the given id.
     * 
     * @param engineId The id of a workflow engine
     * @param isEnabled true to enable the engine, false to disable
     */
    public void setEngineEnabled(String engineId, boolean isEnabled);

    /**
     * Determines whether the workflow definitions are visible
     * for the engine with the given id.
     * 
     * NOTE: Workflow definitions can always be retrieved directly 
     * i.e. via name or id
     * 
     * @param engineId The id of a workflow engine
     * @return true if the definitions are visible
     */
    boolean isEngineVisible(String engineId);
    
    /**
     * Sets the visiblity of workflow definitions
     * for the engine with the given id.
     * 
     * NOTE: Workflow definitions can always be retrieved directly 
     * i.e. via name or id
     * 
     * @param engineId The id of a workflow engine
     * @param isVisible true if the definitions are visible
     */
    public void setEngineVisibility(String engineId, boolean isVisible);
}
