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

package org.alfresco.module.org_alfresco_module_rm.capability;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;

/**
 * Capability Interface.
 *
 * @author andyh
 * @author Roy Wetherall
 */
@AlfrescoPublicApi
public interface Capability
{
    /**
     * Does this capability apply to this nodeRef?
     * @param nodeRef
     * @return
     */
    AccessStatus hasPermission(NodeRef nodeRef);

    /**
     *
     * @param nodeRef
     * @return
     */
    int hasPermissionRaw(NodeRef nodeRef);

    /**
     * Evaluates the capability.
     *
     * @param nodeRef
     * @return
     */
    int evaluate(NodeRef nodeRef);

    /**
     * Evaluates the capability, taking into account a target.
     *
     * @param source    source node reference
     * @param target    target node reference
     * @return int      permission value
     */
    int evaluate(NodeRef source, NodeRef target);

    /**
     * Indicates whether this is a private capability or not.  Private capabilities are used internally, otherwise
     * they are made available to the user to assign to roles.
     *
     * @return  boolean true if private, false otherwise
     */
    boolean isPrivate();

    /**
     * Get the name of the capability
     *
     * @return  String  capability name
     */
    String getName();

    /**
     * Get the title of the capability
     *
     * @return  String  capability title
     */
    String getTitle();

    /**
     * Get the description of the capability
     *
     * @return  String  capability description
     */
    String getDescription();

    /**
     * Gets the group of a capability
     *
     * @return Group capability group
     */
    Group getGroup();

    /**
     * Gets the index of a capability
     *
     * @return int capability index
     */
    int getIndex();
}
