/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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