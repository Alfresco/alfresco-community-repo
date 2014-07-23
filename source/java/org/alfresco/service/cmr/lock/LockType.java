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
package org.alfresco.service.cmr.lock;

import org.alfresco.api.AlfrescoPublicApi;

/**
 * The type of lock to be used by the lock service
 * <p>
 * The lock owner or the administrator can release the lock.
 */
@AlfrescoPublicApi
public enum LockType
{
    /**
     * No-one can update or delete the locked node. No one can add children to the locked node.
     *
     * No-one can update or delete the locked node. 
     * <p>
     * No one can add children to the locked node.
     *
    * @deprecated Deprecated in 4.1.6.  Will be replaced by a more descriptive name.
     */
    @Deprecated
    READ_ONLY_LOCK,
    /**
     * READ_ONLY_LOCK - no-one can update or delete the locked node. No one can add children to the locked node.
     * 
     * @deprecated Deprecated in 4.1.6.  Will be replaced by a more descriptive name.
     */
    @Deprecated
    WRITE_LOCK,

    /**


     * No-one can update or delete the locked node.    
     * <p>
     * There are no restrictions on adding children to the locked node.
     * 
     * @deprecated Deprecated in 4.1.6.  Will be replaced by a more descriptive name.
     */
    @Deprecated
    NODE_LOCK
}