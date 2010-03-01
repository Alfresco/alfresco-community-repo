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
package org.alfresco.wcm.sandbox;

import java.util.Date;

/**
*  Provides information about a WCM sandbox version (snapshot).
*/
public interface SandboxVersion
{
    /**
     * Get the sandbox id
     * 
     * @return  String  sandbox id
     */
    public String getSandboxId();
    
    /**
     * Get the sandbox version ID
     * 
     * @return The version
     */
    public int getVersion();
    
    /**
     * Get the creator of this version
     * 
     * @return The creator
     */
    public String getCreator();
 
    /**
     * Get the creation date
     * 
     * @return The creation date
     */
    public Date getCreatedDate();
    
    /**
     * Get the submitted (short) label / tag
     * 
     * @return The label
     */
    public String getLabel();
    
    /**
     * Get the submitted (long) description
     * 
     * @return The description
     */
    public String getDescription();
    
    /**
     * Return true if system generated snapshot
     * 
     * @return TRUE if system (implicit) snapshot
     */
    boolean isSystemGenerated();
}
