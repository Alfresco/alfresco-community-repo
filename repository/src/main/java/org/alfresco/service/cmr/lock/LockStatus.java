/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.service.cmr.lock;

import org.alfresco.api.AlfrescoPublicApi;

/**
 * Used to indicate lock status.
 * 
 * <ul>
 * <li>NO_LOCK - Indicates that there is no lock present</li>
 * <li>LOCKED - Indicates that the node is locked by somebody else</li>
 * <li>LOCK_OWNER - Indicates that the node is locked and the current user has lock ownership rights</li>
 * <li>LOCK_EXPIRED - Indicates that the lock has expired and the node can be considered to be unlocked</li>
 * </ul>
 * 
 * @author Roy Wetherall
 */
@AlfrescoPublicApi
public enum LockStatus 
{
    /**
     * Indicates that there is no lock present 
     */
    NO_LOCK, 
    /**
     * Indicates that the node is locked
     */
    LOCKED,
    /**
     * Indicates that the node is locked and you have lock ownership rights 
     */
    LOCK_OWNER, 
    /**
     * Indicates that the lock has expired and the node can be considered to be unlocked
     */
    LOCK_EXPIRED 
}