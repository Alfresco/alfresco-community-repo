/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
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
package org.alfresco.rest.api.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.action.executer.NodeSizeActionExecuter;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FolderSizeImpl {

    private ActionService actionService;
    private static final Logger LOG = LoggerFactory.getLogger(FolderSizeImpl.class);
    private static final String IN_PROGRESS = "IN-PROGRESS";
    private static final String STATUS = "status";
    private static final String MESSAGE = "Request has been acknowledged";

    public FolderSizeImpl(ActionService actionService)
    {
        this.actionService = actionService;
    }

    public Map<String, Object> executingAsynchronousFolderAction(final NodeRef nodeRef, final int defaultItems, final Map<String, Object> result, final SimpleCache<Serializable, Object> simpleCache)
    {
        if(simpleCache.get(nodeRef.getId()) == null)
        {
            executeAction(nodeRef, defaultItems, simpleCache);
        }
        LOG.debug("Executing NodeSizeActionExecuter from executingAsynchronousFolderAction method");

        result.putIfAbsent("message",MESSAGE);
        return result;
    }

    protected void executeAction(NodeRef nodeRef, int defaultItems, SimpleCache<Serializable, Object> simpleCache)
    {
        Map<String, Object> currentStatus = new HashMap<>();
        currentStatus.put(STATUS,IN_PROGRESS);

        Action folderSizeAction = actionService.createAction(NodeSizeActionExecuter.NAME);
        folderSizeAction.setTrackStatus(true);
        folderSizeAction.setExecuteAsynchronously(true);
        folderSizeAction.setParameterValue(NodeSizeActionExecuter.DEFAULT_SIZE, defaultItems);
        simpleCache.put(nodeRef.getId(),currentStatus);
        actionService.executeAction(folderSizeAction, nodeRef, false, true);
    }
}