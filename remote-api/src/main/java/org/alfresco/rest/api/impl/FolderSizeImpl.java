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
import java.util.Map;

import lombok.AllArgsConstructor;
import org.alfresco.repo.action.executer.NodeSizeActionExecuter;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AllArgsConstructor
public class FolderSizeImpl {

    private ActionService actionService;
    private static final Logger LOG = LoggerFactory.getLogger(FolderSizeImpl.class);

    public Map<String, Object> executingAsynchronousFolderAction(final int maxItems, final NodeRef nodeRef, final Map<String, Object> result, final SimpleCache<Serializable, Object> simpleCache)
    {
        Action folderSizeAction = actionService.createAction(NodeSizeActionExecuter.NAME);
        folderSizeAction.setTrackStatus(true);
        folderSizeAction.setExecuteAsynchronously(true);
        folderSizeAction.setParameterValue(NodeSizeActionExecuter.PAGE_SIZE, maxItems);
        simpleCache.put(folderSizeAction.getId(),"IN-PROGRESS");
        actionService.executeAction(folderSizeAction, nodeRef, false, true);
        LOG.info("Executing NodeSizeActionExecuter from executingAsynchronousFolderAction method");
        result.putIfAbsent("executionId",folderSizeAction.getId());
        return result;
    }
}