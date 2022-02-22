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

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * Utility class for creating audit events about user group membership.
 *
 * @author Tom Page
 * @since 2.7
 */
public class UserGroupMembershipUtils
{
    /** A QName to display for the parent group's name. */
    public static final QName PARENT_GROUP = QName.createQName(RecordsManagementModel.RM_URI, "Parent Group");
    /** A QName to display for a child group's name. */
    private static final QName CHILD_GROUP = QName.createQName(RecordsManagementModel.RM_URI, "Child Group");

    /**
     * Create a properties map from the given cm:member association.
     *
     * @param childAssocRef The association to use.
     * @param nodeService The node service.
     * @return A map containing the names of the parent and child.
     */
    public static Map<QName, Serializable> makePropertiesMap(ChildAssociationRef childAssocRef, NodeService nodeService)
    {
        Map<QName, Serializable> auditProperties = new HashMap<>();
        // Set exactly one of the child group property or the child user name property.
        String childGroupName = getUserGroupName(childAssocRef.getChildRef(), nodeService);
        if (!isBlank(childGroupName))
        {
            auditProperties.put(CHILD_GROUP, childGroupName);
        }
        String childUserName = (String) nodeService.getProperty(childAssocRef.getChildRef(), ContentModel.PROP_USERNAME);
        if (!isBlank(childUserName))
        {
            auditProperties.put(ContentModel.PROP_USERNAME, childUserName);
        }
        // Set the parent group name.
        auditProperties.put(PARENT_GROUP, getUserGroupName(childAssocRef.getParentRef(), nodeService));
        return auditProperties;
    }

    /** Get a name that can be displayed for the user group. */
    private static String getUserGroupName(NodeRef nodeRef, NodeService nodeService)
    {
        String groupName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_AUTHORITY_DISPLAY_NAME);
        if (isBlank(groupName))
        {
            groupName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_AUTHORITY_NAME);
        }
        return groupName;
    }
}
