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
package org.alfresco.module.org_alfresco_module_rm.record;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Inplace Record Service Interface.
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public interface InplaceRecordService
{
    /**
     * Hides a record within a collaboration site
     *
     * @param nodeRef   The record which should be hidden
     */
    void hideRecord(NodeRef nodeRef);

    /**
     * Moves a record within a collaboration site
     *
     * @param nodeRef                The record which should be moved
     * @param targetNodeRef          The target node reference where it should be moved to
     */
    void moveRecord(NodeRef nodeRef, NodeRef targetNodeRef);
}
