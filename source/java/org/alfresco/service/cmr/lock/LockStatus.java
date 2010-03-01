/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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

/**
 * Enum used to indicate lock status.
 * 
 * @author Roy Wetherall
 */
public enum LockStatus 
{
    NO_LOCK,        // Indicates that there is no lock present 
    LOCKED,         // Indicates that the node is locked
    LOCK_OWNER,     // Indicates that the node is locked and you have lock ownership rights 
    LOCK_EXPIRED    // Indicates that the lock has expired and the node can be considered to be unlocked
}