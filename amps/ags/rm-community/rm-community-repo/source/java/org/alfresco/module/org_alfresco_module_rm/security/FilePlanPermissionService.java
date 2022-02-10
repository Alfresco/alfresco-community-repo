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

package org.alfresco.module.org_alfresco_module_rm.security;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * File plan permission service.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
@AlfrescoPublicApi
public interface FilePlanPermissionService
{
    /**
     * Setup permissions for a record category
     *
     * @param recordCategory   record category node reference
     */
    void setupRecordCategoryPermissions(NodeRef recordCategory);

    /**
     * Setup permissions for an object within a given parent.
     *
     * @param parent    parent node to inherit permissions from
     * @param nodeRef   node ref to setup permissions on
     */
    void setupPermissions(NodeRef parent, NodeRef nodeRef);

    /**
     * Sets a permission on a file plan object.  Assumes allow is true.  Cascades permission down to record folder.
     * Cascades ReadRecord up to file plan.
     *
     * @param nodeRef       node reference
     * @param authority     authority
     * @param permission    permission
     */
    void setPermission(NodeRef nodeRef, String authority, String permission);

    /**
     * Deletes a permission from a file plan object.  Cascades removal down to record folder.
     *
     * @param nodeRef       node reference
     * @param authority     authority
     * @param permission    permission
     */
    void deletePermission(NodeRef nodeRef, String authority, String permission);

}
