/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.repo.security.authority;

/**
 * @author Andy
 *
 */
public class AuthorityBridgeParametersEntity
{
    Long typeQNameId;
    
    Long storeId;
    
    Long childAssocTypeQNameId;
    
    Long authorityNameQNameId;
    
    Long nodeId;
    
    public AuthorityBridgeParametersEntity()
    {
        
    }
    
    public AuthorityBridgeParametersEntity(Long typeQNameId, Long childAssocTypeQNameId, Long authorityNameQNameId, Long storeId)
    {
        this.typeQNameId = typeQNameId;
        this.childAssocTypeQNameId = childAssocTypeQNameId;
        this.storeId = storeId;
        this.authorityNameQNameId = authorityNameQNameId;
    }
    
    public AuthorityBridgeParametersEntity(Long typeQNameId, Long childAssocTypeQNameId, Long authorityNameQNameId, Long storeId, Long nodeId)
    {
        this(typeQNameId, childAssocTypeQNameId, authorityNameQNameId, storeId);
        this.nodeId = nodeId;
    }

    /**
     * @return the typeQNameId
     */
    public Long getTypeQNameId()
    {
        return typeQNameId;
    }

    /**
     * @param typeQNameId the typeQNameId to set
     */
    public void setTypeQNameId(Long typeQNameId)
    {
        this.typeQNameId = typeQNameId;
    }

    /**
     * @return the storeId
     */
    public Long getStoreId()
    {
        return storeId;
    }

    /**
     * @param storeId the storeId to set
     */
    public void setStoreId(Long storeId)
    {
        this.storeId = storeId;
    }

    /**
     * @return the childAssocTypeQNameId
     */
    public Long getChildAssocTypeQNameId()
    {
        return childAssocTypeQNameId;
    }

    /**
     * @param childAssocTypeQNameId the childAssocTypeQNameId to set
     */
    public void setChildAssocTypeQNameId(Long childAssocTypeQNameId)
    {
        this.childAssocTypeQNameId = childAssocTypeQNameId;
    }

    /**
     * @return the authorityNameQNameId
     */
    public Long getAuthorityNameQNameId()
    {
        return authorityNameQNameId;
    }

    /**
     * @param authorityNameQNameId the authorityNameQNameId to set
     */
    public void setAuthorityNameQNameId(Long authorityNameQNameId)
    {
        this.authorityNameQNameId = authorityNameQNameId;
    }

    /**
     * @return the childName
     */
    public Long getNodeId()
    {
        return nodeId;
    }

    /**
     * @param childName the childName to set
     */
    public void setNodeId(Long nodeId)
    {
        this.nodeId = nodeId;
    }

    
}
