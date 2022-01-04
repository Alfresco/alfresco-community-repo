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

package org.alfresco.module.org_alfresco_module_rm.patch.v21;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService;
import org.alfresco.module.org_alfresco_module_rm.dod5015.DOD5015Model;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.security.FilePlanPermissionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.springframework.beans.factory.BeanNameAware;

/**
 * RM v2.1 patch to support InPlace functional updates
 *
 * @author Roy Wetherall
 * @since 2.1
 */
@SuppressWarnings("deprecation")
public class RMv21InPlacePatch extends RMv21PatchComponent
                               implements BeanNameAware, RecordsManagementModel, DOD5015Model
{
    /** Extended reader and writer role details */
    private static final String ROLE_READERS_LABEL = "In-Place Readers";
    private static final String[] ROLE_READERS_CAPABILITIES = new String[]
    {
       "ViewRecords"
    };
    private static final String ROLE_WRITERS_LABEL = "In-Place Writers";
    private static final String[] ROLE_WRITERS_CAPABILITIES = new String[]
    {
       "ViewRecords",
       "EditNonRecordMetadata"
    };

    /** file plan role service */
    private FilePlanRoleService filePlanRoleService;

    /** file plan service */
    private FilePlanService filePlanService;

    /** File plan permission service */
    private FilePlanPermissionService filePlanPermissionService;

    /** capability service */
    private CapabilityService capabilityService;

    /** rule service */
    private RuleService ruleService;

    /** node service */
    private NodeService nodeService;

    /**
     * @param filePlanRoleService   file plan role service
     */
    public void setFilePlanRoleService(FilePlanRoleService filePlanRoleService)
    {
        this.filePlanRoleService = filePlanRoleService;
    }

    /**
     * @param filePlanPermissionService file plan permission service
     */
    public void setFilePlanPermissionService(FilePlanPermissionService filePlanPermissionService)
    {
        this.filePlanPermissionService = filePlanPermissionService;
    }

    /**
     * @param filePlanService   file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }

    /**
     * @param capabilityService capability service
     */
    public void setCapabilityService(CapabilityService capabilityService)
    {
        this.capabilityService = capabilityService;
    }

    /**
     * @param ruleService   rule service
     */
    public void setRuleService(RuleService ruleService)
    {
        this.ruleService = ruleService;
    }

    /**
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @see org.alfresco.repo.module.AbstractModuleComponent#executeInternal()
     */
    @Override
    protected void executePatch()
    {
        Set<NodeRef> filePlans = filePlanService.getFilePlans();

        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("  ... updating " + filePlans.size() + " file plans");
        }

        for (NodeRef filePlan : filePlans)
        {
            if (filePlanService.getUnfiledContainer(filePlan) == null)
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("  ... updating file plan " + filePlan.toString());
                }

                ruleService.disableRules();
                try
                {
                    // create fileplan containers
                    filePlanService.createHoldContainer(filePlan);
                    filePlanService.createTransferContainer(filePlan);
                    filePlanService.createUnfiledContainer(filePlan);

                    // move any existing holds to new container
                    moveExistingHolds(filePlan);

                    // move any existing transfers to new container
                    moveExistingTransfers(filePlan);

                    // add the inplace roles
                    filePlanRoleService.createRole(filePlan, FilePlanRoleService.ROLE_EXTENDED_READERS, ROLE_READERS_LABEL, getCapabilities(ROLE_READERS_CAPABILITIES));
                    filePlanRoleService.createRole(filePlan, FilePlanRoleService.ROLE_EXTENDED_WRITERS, ROLE_WRITERS_LABEL, getCapabilities(ROLE_WRITERS_CAPABILITIES));
                }
                finally
                {
                    ruleService.enableRules();
                }
            }
        }
    }

    private Set<Capability> getCapabilities(String[] capabilityNames)
    {
        Set<Capability> capabilities = new HashSet<>(3);
        for (String capabilityName : capabilityNames)
        {
            capabilities.add(capabilityService.getCapability(capabilityName));
        }
        return capabilities;
    }

    private void moveExistingHolds(NodeRef filePlan)
    {
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("  ... moving existing holds for file plan " + filePlan.toString());
        }

        NodeRef container = filePlanService.getHoldContainer(filePlan);

        List<ChildAssociationRef> assocs = nodeService.getChildAssocs(filePlan, ASSOC_HOLDS, RegexQNamePattern.MATCH_ALL);
        for (ChildAssociationRef assoc : assocs)
        {
            NodeRef hold = assoc.getChildRef();
            String name = (String)nodeService.getProperty(hold, ContentModel.PROP_NAME);
            nodeService.moveNode(hold, container, ContentModel.ASSOC_CONTAINS, QName.createQName(RM_URI, name));
        }
    }

    private void moveExistingTransfers(NodeRef filePlan)
    {
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("  ... moving existing transfers for file plan " + filePlan.toString());
        }

        NodeRef container = filePlanService.getTransferContainer(filePlan);

        List<ChildAssociationRef> assocs = nodeService.getChildAssocs(filePlan, ASSOC_TRANSFERS, RegexQNamePattern.MATCH_ALL);
        for (ChildAssociationRef assoc : assocs)
        {
            NodeRef transfer = assoc.getChildRef();
            String name = (String)nodeService.getProperty(transfer, ContentModel.PROP_NAME);
            nodeService.moveNode(transfer, container, ContentModel.ASSOC_CONTAINS, QName.createQName(RM_URI, name));
        }

    }
}
