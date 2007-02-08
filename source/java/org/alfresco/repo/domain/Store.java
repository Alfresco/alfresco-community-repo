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
package org.alfresco.repo.domain;

import org.alfresco.repo.domain.StoreKey;
import org.alfresco.service.cmr.repository.StoreRef;

/**
 * Represents a store entity
 * 
 * @author Derek Hulley
 */
public interface Store
{
    /**
     * @return Returns the key for the class
     */
    public StoreKey getKey();

    /**
     * @param key the key uniquely identifying this store
     */
    public void setKey(StoreKey key);
    
    /**
     * @return Returns the root of the store
     */
    public Node getRootNode();
    
    /**
     * @param rootNode mandatory association to the root of the store
     */
    public void setRootNode(Node rootNode);
    
    /**
     * Convenience method to access the reference
     * @return Returns the reference to the store
     */
    public StoreRef getStoreRef();
}
