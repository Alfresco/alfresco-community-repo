/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.module.org_alfresco_module_rm.freeze;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.hold.HoldService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.util.ServiceBaseImpl;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.lang.StringUtils;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Freeze Service Implementation
 *
 * @author Roy Wetherall
 * @author Tuna Aksoy
 * @since 2.1
 */
public class FreezeServiceImpl extends    ServiceBaseImpl
                               implements FreezeService,
                                          RecordsManagementModel
{
    /** I18N */
    private static final String MSG_HOLD_NAME = "rm.hold.name";

    /** File Plan Service */
    private FilePlanService filePlanService;

    /** Hold service */
    private HoldService holdService;

    /**
     * @return File plan service
     */
    protected FilePlanService getFilePlanService()
    {
        return this.filePlanService;
    }

    /**
     * @return Hold service
     */
    protected HoldService getHoldService()
    {
        return this.holdService;
    }

    /**
     * @param filePlanService file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }

    /**
     * @param holdService hold service
     */
    public void setHoldService(HoldService holdService)
    {
        this.holdService = holdService;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService#isFrozen(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public boolean isFrozen(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);

        return nodeService.hasAspect(nodeRef, ASPECT_FROZEN);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService#hasFrozenChildren(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public boolean hasFrozenChildren(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);

        List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(nodeRef, ContentModel.ASSOC_CONTAINS,
                RegexQNamePattern.MATCH_ALL);
        if (childAssocs != null && !childAssocs.isEmpty())
        {
            for (ChildAssociationRef childAssociationRef : childAssocs)
            {
                if (isFrozen(childAssociationRef.getChildRef())) { return true; }
            }
        }

        return false;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService#getFreezeDate(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public Date getFreezeDate(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);

        if (isFrozen(nodeRef))
        {
            Serializable property = nodeService.getProperty(nodeRef, PROP_FROZEN_AT);
            if (property != null) { return (Date) property; }
        }

        return null;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService#getFreezeInitiator(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public String getFreezeInitiator(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);

        if (isFrozen(nodeRef))
        {
            Serializable property = nodeService.getProperty(nodeRef, PROP_FROZEN_BY);
            if (property != null) { return (String) property; }
        }

        return null;
    }

    /**
     * Deprecated Method Implementations
     */

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService#getFrozen(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    @Deprecated
    public Set<NodeRef> getFrozen(NodeRef hold)
    {
        return new HashSet<NodeRef>(getHoldService().getHeld(hold));
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService#freeze(java.lang.String,
     *      org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    @Deprecated
    public NodeRef freeze(String reason, NodeRef nodeRef)
    {
        NodeRef hold = createHold(nodeRef, reason);
        getHoldService().addToHold(hold, nodeRef);
        return hold;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService#freeze(org.alfresco.service.cmr.repository.NodeRef,
     *      org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    @Deprecated
    public void freeze(NodeRef hold, NodeRef nodeRef)
    {
        ParameterCheck.mandatory("hold", hold);
        ParameterCheck.mandatory("nodeRef", nodeRef);

        getHoldService().addToHold(hold, nodeRef);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService#freeze(java.lang.String,
     *      java.util.Set)
     */
    @Override
    @Deprecated
    public NodeRef freeze(String reason, Set<NodeRef> nodeRefs)
    {
        NodeRef hold = null;
        if (!nodeRefs.isEmpty())
        {
            List<NodeRef> list = new ArrayList<NodeRef>(nodeRefs);
            hold = createHold(list.get(0), reason);
            getHoldService().addToHold(hold, list);
        }
        return hold;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService#freeze(org.alfresco.service.cmr.repository.NodeRef,
     *      java.util.Set)
     */
    @Override
    @Deprecated
    public void freeze(NodeRef hold, Set<NodeRef> nodeRefs)
    {
        ParameterCheck.mandatory("hold", hold);
        ParameterCheck.mandatoryCollection("nodeRefs", nodeRefs);

        for (NodeRef nodeRef : nodeRefs)
        {
            freeze(hold, nodeRef);
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService#unFreeze(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    @Deprecated
    public void unFreeze(NodeRef nodeRef)
    {
        List<NodeRef> holds = getHoldService().heldBy(nodeRef, true);
        for (NodeRef hold : holds)
        {
            getHoldService().removeFromHold(hold, nodeRef);
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService#unFreeze(java.util.Set)
     */
    @Override
    @Deprecated
    public void unFreeze(Set<NodeRef> nodeRefs)
    {
        ParameterCheck.mandatoryCollection("nodeRefs", nodeRefs);

        for (NodeRef nodeRef : nodeRefs)
        {
            unFreeze(nodeRef);
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService#relinquish(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    @Deprecated
    public void relinquish(NodeRef hold)
    {
        getHoldService().deleteHold(hold);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService#getReason(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    @Deprecated
    public String getReason(NodeRef hold)
    {
        return getHoldService().getHoldReason(hold);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService#updateReason(org.alfresco.service.cmr.repository.NodeRef,
     *      java.lang.String)
     */
    @Override
    @Deprecated
    public void updateReason(NodeRef hold, String reason)
    {
        getHoldService().setHoldReason(hold, reason);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService#getHold(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public Set<NodeRef> getHolds(NodeRef filePlan)
    {
        ParameterCheck.mandatory("filePlan", filePlan);

        return new HashSet<NodeRef>(getHoldService().getHolds(filePlan));
    }

    /**
     * Creates a hold using the given nodeRef and reason
     *
     * @param nodeRef the nodeRef which will be frozen
     * @param reason the reason why the record will be frozen
     * @return NodeRef of the created hold
     */
    private NodeRef createHold(NodeRef nodeRef, String reason)
    {
        // get the hold container
        final NodeRef filePlan = getFilePlanService().getFilePlan(nodeRef);
        NodeRef holdContainer = getFilePlanService().getHoldContainer(filePlan);

        // calculate the hold name
        int nextCount = getNextCount(holdContainer);
        String holdName = I18NUtil.getMessage(MSG_HOLD_NAME) + " " + StringUtils.leftPad(Integer.toString(nextCount), 10, "0");

        // create hold
        return getHoldService().createHold(filePlan, holdName, reason, null);
    }
}