/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * and Open Source Software ("FLOSS") applications as described in Alfresco's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing" */
package org.alfresco.repo.avm;

/**
 * Layered nodes share these methods.
 * @author britt
 */
public interface Layered extends AVMNode
{
    /**
     * Get the indirection, or underlying path that this 
     * node points to.
     * @param lookup The lookup path.  Needed for most nodes to determine
     * underlying path.
     * @return The underlying indirection.
     */
    public String getUnderlying(Lookup lookup);
    
    /**
     * Get the indirection version.
     * @param lookup The lookup path.
     * @return The underlying indirection version.
     */
    public int getUnderlyingVersion(Lookup lookup);

    /**
     * Get the raw indirection of a layered node.
     * @return The raw indirection, which will be null for
     * LayeredDirectoryNodes that are not primary indirections.
     */
    public String getIndirection();
    
    /**
     * Set the indirection version for this layered node.
     * @param version The indirection version to set.
     */
    public void setIndirectionVersion(Integer version);
}
