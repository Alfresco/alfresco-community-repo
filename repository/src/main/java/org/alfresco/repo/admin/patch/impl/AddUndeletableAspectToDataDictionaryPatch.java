/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.admin.patch.impl;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 *  Add sys:undeletable aspect to DataDictionary and prevent it to be deleted
 */
public class AddUndeletableAspectToDataDictionaryPatch extends AbstractPatch
{
    private static final String MSG_DC_PATCHED = "patch.addUndeletableAspectToDataDictionaryPatch.success";
    private static final String MSG_DC_NOT_PATCHED = "patch.addUndeletableAspectToDataDictionaryPatch.skipped";
    private NodeService nodeService;
    private BehaviourFilter behaviourFilter;

    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }

    @Override
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    @Override
    protected String applyInternal() throws Exception {
        NodeRef rootNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        String primaryPath = "/app:company_home/app:dictionary";
        NodeRef nodeRef = nodeService.getChildByName(rootNodeRef, ContentModel.ASSOC_CONTAINS, primaryPath);
        if (nodeRef != null)
        {
            try
            {
                behaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
                nodeService.addAspect(nodeRef, ContentModel.ASPECT_UNDELETABLE, null);
            }
            finally
            {
                behaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
            }
            return I18NUtil.getMessage(MSG_DC_PATCHED);
        }
        else
        {
            return I18NUtil.getMessage(MSG_DC_NOT_PATCHED);
        }
    }
}