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

package org.alfresco.module.org_alfresco_module_rm.audit.event;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * Create hold audit event.
 * This listens to the NodeServicePolicies.OnCreateNodePolicy in order to cover the create hold action from Share
 * since that does not call the createHold from HoldService
 *
 * @author Sara Aspery
 * @since 3.3
 */
@BehaviourBean
public class CreateHoldAuditEvent extends AuditEvent implements NodeServicePolicies.OnCreateNodePolicy
{
    /**
     * Node Service
     */
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
     * @see org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy#onCreateNode(org.alfresco.service.cmr.repository.ChildAssociationRef)
     */
    @Override
    @Behaviour
            (
                    kind = BehaviourKind.CLASS,
                    type = "rma:hold",
                    notificationFrequency = NotificationFrequency.TRANSACTION_COMMIT
            )
    public void onCreateNode(ChildAssociationRef childAssociationRef)
    {
        NodeRef holdNodeRef = childAssociationRef.getChildRef();

        Map<QName, Serializable> auditProperties = HoldUtils.makePropertiesMap(holdNodeRef, nodeService);
        auditProperties.put(PROP_HOLD_REASON, nodeService.getProperty(holdNodeRef, PROP_HOLD_REASON));

        recordsManagementAuditService.auditEvent(holdNodeRef, getName(), null, auditProperties);
    }
}
