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

/** 
 * The interface against which recipients of permission are persisted
 * @author andyh
 */
public interface DbAuthority extends Serializable 
{
    /**
     * Get the object id
     * @return
     */
    public Long getId();
    
    /**
     * @return  Returns the version number for optimistic locking
     */
    public Long getVersion();
    
    /**
     * @return Returns the authority
     */
    public String getAuthority();
    
    /**
     * @param the authority
     */
    public void setAuthority(String authority);
    
    /**
     * Use a crc to enforce case sensitive unique key
     * @param crc
     */
    public void setCrc(Long crc);
    
    /**
     * Get the CRC
     * 
     * @return
     */
    public Long getCrc();
   
}
