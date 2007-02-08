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

import java.io.Serializable;

import org.alfresco.service.namespace.QName;

/**
 * The interface against which permission references are persisted in hibernate.
 * 
 * @author andyh
 */
public interface DbPermission extends Serializable
{
    /**
     * @return Returns the automatically assigned ID
     */
    public long getId();
    
    /**
     * @return Returns the qualified name of this permission
     */
    public QName getTypeQname();
    
    /**
     * @param qname the entity representing the qname for this instance
     */
    public void setTypeQname(QName qname);

    /**
     * @return Returns the permission name
     */
    public String getName();
    
    /**
     * @param name the name of the permission
     */
    public void setName(String name);
    
    /**
     * @return Returns a key combining the {@link #getTypeQname() type}
     *      and {@link #getName() name}
     */
    public DbPermissionKey getKey();
}
