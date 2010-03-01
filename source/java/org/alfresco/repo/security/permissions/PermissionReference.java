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
package org.alfresco.repo.security.permissions;

import java.io.Serializable;

import org.alfresco.service.namespace.QName;

/**
 * A Permission is a named permission against a type or aspect which is defined
 * by QName. So a permission string is scoped by type.
 * 
 * @author Andy Hind
 */
public interface PermissionReference extends Serializable
{

    /**
     * Get the QName of the type or aspect against which the permission is
     * defined.
     * 
     * @return the qname
     */
    public QName getQName();

    /**
     * Get the name of the permission
     * 
     * @return the name
     */
    public String getName();
    
    
}
