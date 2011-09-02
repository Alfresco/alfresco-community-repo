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

package org.alfresco.repo.publishing;

import java.util.List;

import org.alfresco.service.cmr.workflow.WorkflowPath;


/**
 * @author Nick Smith
 * @author Frederik Heremans
 * @since 4.0
 */
public class PublishWebContentActivitiTest extends PublishWebContentProcessTest
{
    private static final String DEF_NAME = "activiti$publishWebContent";
    
    @Override
    protected String getWorkflowDefinitionName()
    {
        return DEF_NAME;
    }
    
    /**
     * Activiti has 2 paths: a timer-scope-path and the main execution-path
     */
    protected void checkNode(String expNode)
    {
        List<WorkflowPath> paths = workflowService.getWorkflowPaths(instanceId);
        assertEquals(2, paths.size());
        WorkflowPath path = paths.get(0);
        assertEquals(expNode, path.getNode().getName());
    }
}
