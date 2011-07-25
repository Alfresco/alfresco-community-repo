/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.workflow;

import org.alfresco.repo.workflow.activiti.ActivitiConstants;
import org.alfresco.repo.workflow.jbpm.JBPMEngine;
import org.alfresco.service.cmr.workflow.WorkflowAdminService;

/**
 * Default implementation of the workflow admin service.
 *
 * @author Gavin Cornwell
 * @since 4.0
 */
public class WorkflowAdminServiceImpl implements WorkflowAdminService
{
    private boolean jbpmEngineEnabled = true;
    private boolean activitiEngineEnabled = true;

    public void setJbpmEngineEnabled(boolean jbpmEngineEnabled)
    {
        this.jbpmEngineEnabled = jbpmEngineEnabled;
    }

    public void setActivitiEngineEnabled(boolean activitiEngineEnabled)
    {
        this.activitiEngineEnabled = activitiEngineEnabled;
    }

    @Override
    public boolean isEngineEnabled(String engineId)
    {
        if (JBPMEngine.ENGINE_ID.equals(engineId))
        {
            return jbpmEngineEnabled;
        }
        else if (ActivitiConstants.ENGINE_ID.equals(engineId))
        {
            return activitiEngineEnabled;
        }
        else
        {
            // if the engine id is not recognised it can't be enabled!
            return false;
        }
    }
}
