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
package org.alfresco.repo.web.scripts.action;

import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.ActionTrackingService;
import org.alfresco.service.cmr.repository.NodeService;

/**
 * Builds up models for running actions
 *  
 * @author Nick Burch
 * @since 3.4
 */
public class RunningActionModelBuilder 
{
    protected static final String MODEL_DATA_ITEM = "runningAction";
    protected static final String MODEL_DATA_LIST = "runningActions";
    
    
    protected NodeService nodeService;
    protected ActionService actionService;
    protected ActionTrackingService actionTrackingService;

    public RunningActionModelBuilder(NodeService nodeService, ActionService actionService,
                                   ActionTrackingService actionTrackingService) 
    {
       this.nodeService = nodeService;
       this.actionService = actionService;
       this.actionTrackingService = actionTrackingService;
    }
}