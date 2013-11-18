/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.transfer;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Transfer Service Interface
 *
 * @author Tuna Aksoy
 * @since 2.2
 */
public interface TransferService
{
    /**
     * Indicates whether the given node is a transfer (container) or not.
     *
     * @param nodeRef   node reference
     * @return boolean  true if transfer, false otherwise
     *
     * @since 2.0
     */
    boolean isTransfer(NodeRef nodeRef);
}
