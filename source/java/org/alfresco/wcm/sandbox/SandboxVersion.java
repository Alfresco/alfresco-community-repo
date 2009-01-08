/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
     * Get the short description
     * 
     * @return The short description
     */
    public String getTag();
    
    /**
     * Get the long description
     * 
     * @return The long description
     */
    public String getDescription();
    
    /**
     * Return true if system generated snapshot
     * 
     * @return TRUE if system (implicit) snapshot
     */
    boolean isSystemGenerated();
}
