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
package org.alfresco.repo.download;

import org.springframework.beans.factory.InitializingBean;

import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ParameterCheck;

/**
 * Implementation of {@link ActionServiceHelper} which schedules the zip creation process to run in the same alfresco node as the caller.
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
