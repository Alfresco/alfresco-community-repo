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
package org.alfresco.module.org_alfresco_module_rm.fileplan.hold;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;

/**
 * Hold service implementation
 *
 * @author Tuna Aksoy
 * @since 2.2
 */
public class HoldServiceImpl implements HoldService
{
    /** File Plan Service */
    private FilePlanService filePlanService;

    /** Node Service */
    private NodeService nodeService;

    /**
     * Set the file plan service
     *
     * @param filePlanService the file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }

    /**
     * Set the node service
     *
     * @param nodeService the node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.fileplan.hold.HoldService#getHolds(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public List<NodeRef> getHolds(NodeRef filePlan)
    {
        ParameterCheck.mandatory("filePlan", filePlan);

        NodeRef holdContainer = filePlanService.getHoldContainer(filePlan);
        List<ChildAssociationRef> holdsAssocs = nodeService.getChildAssocs(holdContainer);
        List<NodeRef> holds = new ArrayList<NodeRef>(holdsAssocs.size());
        for (ChildAssociationRef holdAssoc : holdsAssocs)
        {
            holds.add(holdAssoc.getChildRef());
        }

        return holds;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.fileplan.hold.HoldService#addToHoldContainer(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void addToHoldContainer(NodeRef hold, NodeRef record)
    {
        ParameterCheck.mandatory("hold", hold);
        ParameterCheck.mandatory("record", record);

        List<NodeRef> holds = new ArrayList<NodeRef>(1);
        holds.add(hold);
        addToHoldContainers(Collections.unmodifiableList(holds), record);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.fileplan.hold.HoldService#addToHoldContainers(java.util.List, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void addToHoldContainers(List<NodeRef> holds, NodeRef record)
    {
        ParameterCheck.mandatoryCollection("holds", holds);
        ParameterCheck.mandatory("record", record);

        String recordName = (String) nodeService.getProperty(record, ContentModel.PROP_NAME);
        String validLocalName = QName.createValidLocalName(recordName);
        QName name = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, validLocalName);

        for (NodeRef hold : holds)
        {
            nodeService.addChild(hold, record, ContentModel.ASSOC_CONTAINS, name);
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.fileplan.hold.HoldService#removeFromHoldContainer(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void removeFromHoldContainer(NodeRef hold, NodeRef record)
    {
        ParameterCheck.mandatory("hold", hold);
        ParameterCheck.mandatory("record", record);

        List<NodeRef> holds = new ArrayList<NodeRef>(1);
        holds.add(hold);
        removeFromHoldContainers(Collections.unmodifiableList(holds), record);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.fileplan.hold.HoldService#removeFromHoldContainers(java.util.List, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void removeFromHoldContainers(List<NodeRef> holds, NodeRef record)
    {
        ParameterCheck.mandatory("holds", holds);
        ParameterCheck.mandatory("record", record);

        for (NodeRef hold : holds)
        {
            nodeService.removeChild(hold, record);
        }
    }
}
