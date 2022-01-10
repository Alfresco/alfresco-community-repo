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

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for creating audit events about holds.
 *
 * @author Sara Aspery
 * @since 3.3
 */
class HoldUtils
{
    /** A QName to display for the hold name. */
    public static final QName HOLD_NAME = QName.createQName(RecordsManagementModel.RM_URI, "Hold Name");
    /** A QName to display for the hold node ref. */
    public static final QName HOLD_NODEREF = QName.createQName(RecordsManagementModel.RM_URI, "Hold NodeRef");

    /**
     * Create a properties map containing the hold name and node ref for the given hold.
     *
     * @param nodeRef The nodeRef of the hold.
     * @param nodeService The node service.
     * @return A map containing the name and noderef of the hold.
     */
    static Map<QName, Serializable> makePropertiesMap(NodeRef nodeRef, NodeService nodeService)
    {
        Map<QName, Serializable> auditProperties = new HashMap<>();

        auditProperties.put(HOLD_NAME, nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
        auditProperties.put(HOLD_NODEREF, nodeRef);

        return auditProperties;
    }
}
