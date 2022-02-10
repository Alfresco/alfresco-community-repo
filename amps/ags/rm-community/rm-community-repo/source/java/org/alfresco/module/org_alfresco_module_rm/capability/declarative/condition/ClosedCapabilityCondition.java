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

package org.alfresco.module.org_alfresco_module_rm.capability.declarative.condition;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.capability.declarative.AbstractCapabilityCondition;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.RegexQNamePattern;

/**
 * @author Roy Wetherall
 */
public class ClosedCapabilityCondition extends AbstractCapabilityCondition
{
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.declarative.CapabilityCondition#evaluate(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public boolean evaluateImpl(NodeRef nodeRef)
    {
        boolean result = false;
        if (recordFolderService.isRecordFolder(nodeRef))
        {
            result = recordFolderService.isRecordFolderClosed(nodeRef);
        }
        else if (recordService.isRecord(nodeRef))
        {
            final List<ChildAssociationRef> assocs = nodeService.getParentAssocs(nodeRef, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
            
            result = AuthenticationUtil.runAsSystem(new RunAsWork<Boolean>()
            {
                @Override
                public Boolean doWork()
                {
                    for (ChildAssociationRef assoc : assocs)
                    {
                        NodeRef parent = assoc.getParentRef();
                        if (recordFolderService.isRecordFolder(parent) && recordFolderService.isRecordFolderClosed(parent))
                        {
                            return true;
                        }
                    }
                    return false;
                }
            });
          
        }
        return result;
    }

}
