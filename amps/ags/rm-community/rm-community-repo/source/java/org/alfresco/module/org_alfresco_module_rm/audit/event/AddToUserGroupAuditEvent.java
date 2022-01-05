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

import static org.alfresco.module.org_alfresco_module_rm.audit.event.UserGroupMembershipUtils.makePropertiesMap;
import static org.alfresco.repo.policy.Behaviour.NotificationFrequency.EVERY_EVENT;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * Add an authority to a user group.
 *
 * @author Tom Page
 * @since 2.7
 */
@BehaviourBean(defaultType = "cm:authorityContainer")
public class AddToUserGroupAuditEvent extends AuditEvent implements OnCreateChildAssociationPolicy
{
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

    /** Behaviour to audit adding an authority to a user group. */
    @Override
    @Behaviour(kind = BehaviourKind.ASSOCIATION, notificationFrequency = EVERY_EVENT, assocType = "cm:member")
    public void onCreateChildAssociation(ChildAssociationRef childAssocRef, boolean isNewNode)
    {
        Map<QName, Serializable> auditProperties = makePropertiesMap(childAssocRef, nodeService);
        recordsManagementAuditService.auditEvent(childAssocRef.getChildRef(), getName(), null, auditProperties, true);
    }
}
