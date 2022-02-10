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

package org.alfresco.module.org_alfresco_module_rm.patch.v24;

import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.patch.AbstractModulePatch;
import org.alfresco.repo.rule.RuleModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

/**
 * RM v2.4 patch that ensures that file plan root containers do not inherited rules, because this is no longer enforced
 * in the service code anymore.
 * 
 * See https://issues.alfresco.com/jira/browse/RM-3154
 *
 * @author Roy Wetherall
 * @since 2.4
 */
public class RMv24FilePlanContainerRuleInheritancePatch extends AbstractModulePatch
{
    /** file plan service */
    private FilePlanService filePlanService;
    
    /** node service */
    private NodeService nodeService;

    /**
     * @param filePlanService file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }

    /**
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.patch.AbstractModulePatch#applyInternal()
     * 
     * Note that we do not break rule inheritance for the root file since this wasn't previously 
     * the behaviour and we don't want to prevent this from happening if the current installation 
     * has been setup to allow it.
     */
    @Override
    public void applyInternal()
    {
        // get all the file plans
        Set<NodeRef> filePlans = filePlanService.getFilePlans();
        for (NodeRef filePlan : filePlans)
        {
            // set rule inheritance for all root file plan containers
            nodeService.addAspect(filePlanService.getUnfiledContainer(filePlan), RuleModel.ASPECT_IGNORE_INHERITED_RULES, null);
            nodeService.addAspect(filePlanService.getHoldContainer(filePlan), RuleModel.ASPECT_IGNORE_INHERITED_RULES, null);        
            nodeService.addAspect(filePlanService.getTransferContainer(filePlan), RuleModel.ASPECT_IGNORE_INHERITED_RULES, null);
        }
    }
}
