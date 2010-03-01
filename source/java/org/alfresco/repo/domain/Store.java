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
package org.alfresco.repo.domain;

import org.alfresco.service.cmr.repository.StoreRef;

/**
 * Represents a store entity
 * 
 * @author Derek Hulley
 */
public interface Store
{
    /**
     * @return  Returns the current version number used for optimistic locking
     */
    public Long getVersion();
    
    /**
     * @return          Returns the unique ID of the object
     */
    public Long getId();

    /**
     * @return                  the store protocol
     */
    public String getProtocol();
    
    /**
     * @param protocol          the store protocol
     */
    public void setProtocol(String protocol);
    
    /**
     * @return                  the store identifier
     */
    public String getIdentifier();
    
    /**
     * @param identifier        the store identifier
     */
    public void setIdentifier(String identifier);

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
