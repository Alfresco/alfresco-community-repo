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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */
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
