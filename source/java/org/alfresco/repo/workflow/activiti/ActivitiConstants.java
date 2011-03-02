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

package org.alfresco.repo.workflow.activiti;

/**
 * @since 3.5
 * @author Nick Smith
 *
 */
public interface ActivitiConstants
{
    public static final String ENGINE_ID = "activiti";
    public static final String NODE_NAME = "name";
    public static final String NODE_DESCRIPTION = "documentation";
    public static final String NODE_TYPE= "type";

    public static final String PROP_START_TASK_END_DATE = "_startTaskCompleted";
    public static final String START_TASK_PREFIX = "start";
    
    public static final String DEFAULT_TRANSITION_NAME = "Next";
    public static final String START_TASK_PROPERTY_PREFIX = "_start_";
    
    public static final String USER_TASK_NODE_TYPE = "userTask";
    public static final String PROP_TASK_FORM_KEY = "taskFormKey";
    public static final String PROP_POOLED_ACTORS_HISTORY = "pooledActorsHistory";
	public static final String DELETE_REASON_DELETED = "deleted";
	public static final String DELETE_REASON_CANCELLED = "cancelled";
    
	public static final String SERVICE_REGISTRY_BEAN_KEY = "services";
	
   
}
