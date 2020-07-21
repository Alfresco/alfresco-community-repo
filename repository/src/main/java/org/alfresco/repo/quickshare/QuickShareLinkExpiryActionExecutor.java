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

package org.alfresco.repo.quickshare;

import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.quickshare.QuickShareLinkExpiryAction;
import org.alfresco.service.cmr.quickshare.QuickShareService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

/**
 * This action executor unshares the shared link when the quick share link expiry action is triggered.
 *
 * @author Jamal Kaabi-Mofrad
 */
public class QuickShareLinkExpiryActionExecutor extends ActionExecuterAbstractBase
{
    private static final Log LOGGER = LogFactory.getLog(QuickShareLinkExpiryActionExecutor.class);

    private QuickShareService quickShareService;

    public void setQuickShareService(QuickShareService quickShareService)
    {
        this.quickShareService = quickShareService;
    }

    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        if (!(action instanceof QuickShareLinkExpiryAction))
        {
            if (action.getActionDefinitionName().equals(QuickShareLinkExpiryActionImpl.EXECUTOR_NAME))
            {
                action = new QuickShareLinkExpiryActionImpl(action);
            }
            else
            {
                return;
            }
        }
        QuickShareLinkExpiryAction quickShareLinkExpiryAction = (QuickShareLinkExpiryAction) action;
        String sharedId = quickShareLinkExpiryAction.getSharedId();
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Unsharing the shared id [" + sharedId + "] for the node:" + quickShareService.getTenantNodeRefFromSharedId(sharedId).getSecond());
        }

        if (StringUtils.isEmpty(sharedId))
        {
            throw new QuickShareLinkExpiryActionException("Shared id is not specified.");

        }
        try
        {
            quickShareService.unshareContent(sharedId);
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("Unshared the shared id:" + sharedId);
            }
        }
        catch (Exception ex)
        {
            if (ex instanceof QuickShareLinkExpiryActionException)
            {
                LOGGER.error("Couldn't delete the quick share expiry action [" + quickShareLinkExpiryAction.getNodeRef() + "] for the sharedId:"
                            + sharedId);
            }
            else
            {
                LOGGER.error("Couldn't unshare the shared Id:" + sharedId);
            }
        }
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        // Not used - our definitions hold everything
    }
}
