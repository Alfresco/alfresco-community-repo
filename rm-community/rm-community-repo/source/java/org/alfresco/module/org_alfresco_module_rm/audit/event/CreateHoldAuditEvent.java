/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
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

package org.alfresco.module.org_alfresco_module_rm.audit.event;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.hold.HoldServicePolicies;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

import static org.alfresco.model.ContentModel.PROP_DESCRIPTION;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Create hold audit event.
 *
 * @author Sara Aspery
 * @since 3.3
 */
@BehaviourBean
public class CreateHoldAuditEvent extends AuditEvent implements HoldServicePolicies.OnCreateHoldPolicy
{
    /** QNames to display for the hold's properties. */
    private static final QName HOLD_NAME = QName.createQName(RecordsManagementModel.RM_URI, "Hold Name");
    private static final QName HOLD_DESCRIPTION = QName.createQName(RecordsManagementModel.RM_URI, "Hold Description");

    /** Node Service */
    private NodeService nodeService;

    /**
     * Sets the node service
     *
     * @param nodeService nodeService to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.hold.HoldServicePolicies.OnCreateHoldPolicy#onCreateHold(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    @Behaviour
            (
                    kind = BehaviourKind.CLASS,
                    type = "rma:hold"
            )
    public void onCreateHold(NodeRef holdNodeRef)
    {
        Map<QName, Serializable> auditProperties = new HashMap<>();
        auditProperties.put(ContentModel.PROP_NAME, nodeService.getProperty(holdNodeRef, ContentModel.PROP_NAME));
        auditProperties.put(PROP_HOLD_REASON, nodeService.getProperty(holdNodeRef, PROP_HOLD_REASON));
        auditProperties.put(PROP_DESCRIPTION, nodeService.getProperty(holdNodeRef, PROP_DESCRIPTION));

        recordsManagementAuditService.auditEvent(holdNodeRef, getName(), null, auditProperties);
    }
}
