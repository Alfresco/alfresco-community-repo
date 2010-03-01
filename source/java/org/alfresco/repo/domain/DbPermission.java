/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.domain;

import java.io.Serializable;

import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.service.namespace.QName;

/**
 * The interface against which permission references are persisted in hibernate.
 * 
 * @author andyh
 */
public interface DbPermission extends Serializable
{
    /**
     * Convenience method to get the type QName of the permission
     * 
     * @param qnameDAO          helper DAO
     * @return                  the permission's type QName
     */
    public QName getTypeQName(QNameDAO qnameDAO);
    
    /**
     * @return Returns the automatically assigned ID
     */
    public Long getId();
    
    /**
     * @return  Returns the version number for optimistic locking
     */
    public Long getVersion();
    
    /**
     * @return Returns the qualified name of this permission
     */
    public Long getTypeQNameId();
    
    /**
     * @param typeQNameId       the ID of the QName for this instance
     */
    public void setTypeQNameId(Long typeQNameId);

    /**
     * @return Returns the permission name
     */
    public String getName();
    
    /**
     * @param name the name of the permission
     */
    public void setName(String name);
    
    /**
     * @return Returns a key combining the {@link #getTypeQnameId() type}
     *      and {@link #getName() name}
     */
    public DbPermissionKey getKey();
}
