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
package org.alfresco.repo.download;

import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ParameterCheck;
import org.springframework.beans.factory.InitializingBean;

/**
 * Implementation of {@link ActionServiceHelper} which schedules the zip creation process to run in the same alfresco node
 * as the caller.
 * 
 * @author Alex Miller
 */
public class LocalActionServiceHelper implements InitializingBean, ActionServiceHelper
{
    private ActionService localActionService;

    public void setLocalActionService(ActionService localActionService)
    {
        this.localActionService = localActionService; 
    }


    @Override
    public void executeAction(NodeRef downloadNode)
    {
        Action action = localActionService.createAction("createDownloadArchiveAction");
        action.setExecuteAsynchronously(true);
        
        localActionService.executeAction(action, downloadNode);
    }
    
    @Override
    public void afterPropertiesSet() throws Exception
    {
        ParameterCheck.mandatory("localActionServer", localActionService);
    }

}
