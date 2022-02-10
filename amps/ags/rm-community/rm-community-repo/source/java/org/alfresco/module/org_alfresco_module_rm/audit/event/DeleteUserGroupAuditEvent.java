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
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * Audits user group deletion.
 *
 * @author Tom Page
 * @since 2.7
 */
@BehaviourBean
public class DeleteUserGroupAuditEvent extends AuditEvent implements BeforeDeleteNodePolicy
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

    /**
     * Behaviour that will audit user group deletion
     *
     * @param nodeRef the node to be deleted
     */
    @Override
    @Behaviour(kind = BehaviourKind.CLASS, type = "cm:authorityContainer")
    public void beforeDeleteNode(NodeRef nodeRef)
    {
        // Retrieve the authority name property to be audited
        Map<QName, Serializable> auditProperties = new HashMap<>();
        auditProperties.put(ContentModel.PROP_AUTHORITY_DISPLAY_NAME,
                    nodeService.getProperty(nodeRef, ContentModel.PROP_AUTHORITY_DISPLAY_NAME));

        //audit the property values before the delete event
        recordsManagementAuditService.auditEvent(nodeRef, getName(), auditProperties, null, true, false);
    }
}
