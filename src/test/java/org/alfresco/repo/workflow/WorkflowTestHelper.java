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

package org.alfresco.repo.workflow;

import java.util.Arrays;
import java.util.Set;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
public class WorkflowTestHelper
{
    private final WorkflowAdminServiceImpl workflowAdminService;
    private final String engineId;
    private final Set<String> enabledEngines;
    private final Set<String> visibleEngines;
    
    public WorkflowTestHelper(WorkflowAdminServiceImpl workflowAdminService, String engineId, boolean enableEngineOnly)
    {
        this.workflowAdminService = workflowAdminService;
        this.engineId = engineId;
        this.enabledEngines = workflowAdminService.getEnabledEngines();
        this.visibleEngines = workflowAdminService.getVisibleEngines();
        if(enableEngineOnly)
        {
            enableThisEngineOnly();
        }
    }

    public void enableThisEngineOnly()
    {
        workflowAdminService.setEnabledEngines(Arrays.asList(engineId));
        workflowAdminService.setVisibleEngines(Arrays.asList(engineId));
    }
    
    public void tearDown()
    {
        workflowAdminService.setEnabledEngines(enabledEngines);
        workflowAdminService.setVisibleEngines(visibleEngines);
    }

    public void setVisible(boolean isVisible)
    {
        workflowAdminService.setEngineVisibility(engineId, isVisible);
    }
    
    public void setEnabled(boolean isEnabled)
    {
        workflowAdminService.setEngineEnabled(engineId, isEnabled);
    }
    
}
