/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.job.publish;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.module.org_alfresco_module_rm.action.impl.BroadcastDispositionActionDefinitionUpdateAction;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * Disposition action definition publish executor
 *
 * @author Roy Wetherall
 */
public class DispositionActionDefinitionPublishExecutor extends BasePublishExecutor
{
    /** Node service */
    private NodeService nodeService;

    /** Records management action service */
    private RecordsManagementActionService rmActionService;

    /**
     * Set node service
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Set records management service
     * @param rmActionService   records management service
     */
    public void setRmActionService(RecordsManagementActionService rmActionService)
    {
        this.rmActionService = rmActionService;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.job.publish.PublishExecutor#getName()
     */
    @Override
    public String getName()
    {
        return RecordsManagementModel.UPDATE_TO_DISPOSITION_ACTION_DEFINITION;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.job.publish.PublishExecutor#publish(org.alfresco.service.cmr.repository.NodeRef)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void publish(NodeRef nodeRef)
    {
        List<QName> updatedProps = (List<QName>)nodeService.getProperty(nodeRef, RecordsManagementModel.PROP_UPDATED_PROPERTIES);
        if (updatedProps != null)
        {
            Map<String, Serializable> params = new HashMap<>();
            params.put(BroadcastDispositionActionDefinitionUpdateAction.CHANGED_PROPERTIES, (Serializable)updatedProps);
            rmActionService.executeRecordsManagementAction(nodeRef, BroadcastDispositionActionDefinitionUpdateAction.NAME, params);
        }
    }
}
