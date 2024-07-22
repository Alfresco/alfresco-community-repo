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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import org.alfresco.repo.action.executer.NodeSizeActionExecuter;
import org.alfresco.repo.cache.DefaultSimpleCache;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.ActionTrackingService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AllArgsConstructor
public class FolderSizeImpl {

    private ActionService actionService;
    private ActionTrackingService actionTrackingService;
    private NodeService nodeService;

    private static final Logger LOG = LoggerFactory.getLogger(FolderSizeImpl.class);

    public Map<String, Object> executingAsynchronousFolderAction(final int maxItems, final NodeRef nodeRef, final Map<String, Object> result, final SimpleCache<Serializable, Object> sharedCache) throws Exception
    {
        Action folderSizeAction = actionService.createAction(NodeSizeActionExecuter.NAME);
        folderSizeAction.setTrackStatus(true);
        folderSizeAction.setExecuteAsynchronously(true);
        folderSizeAction.setParameterValue(NodeSizeActionExecuter.PAGE_SIZE, maxItems);
        actionService.executeAction(folderSizeAction, nodeRef, false, true);
        NodeSizeActionExecuter.actionsRecords.put(folderSizeAction.getId(),folderSizeAction);
        LOG.info("Executing NodeSizeActionExecuter from executingAsynchronousFolderAction method");
        result.putIfAbsent("executionId",folderSizeAction.getId());
        return result;
    }

    public Action getAction(NodeRef nodeRef, String executionId)
    {
        if (this.nodeService.exists(nodeRef) == true)
        {
                Map<String, Action> actionsRecords = NodeSizeActionExecuter.actionsRecords;
                List<Action> actionList = new ArrayList<>(actionsRecords.values());
                 for (Action action : actionList)
                 {
                    if (action.getId().equals(executionId)) {
                        return action;
                    }
                }
        }
        return null;
    }
}