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

/**
 * Hibernate persistence for authority aliases
 * 
 * @author andyh
 *
 */
public interface DbAuthorityAlias
{
    /**
     * Get the object id.
     * @return
     */
    public Long getId();
    
    /**
     * Get the version used for optimistic locking
     * @return
     */
    public Long getVersion();
    
    /**
     * Get the authority for which this is an alias
     * @return
     */
    public DbAuthority getAuthority();
    
    /**
     * Get the alias for the authority
     * @return
     */
    public DbAuthority getAlias();
    
    /**
     * Set the authority
     * @param authority
     */
    public void setAuthority(DbAuthority authority);
    
    /**
     * Set the alias
     * @param alias
     */
    public void setAlias(DbAuthority alias);
}
