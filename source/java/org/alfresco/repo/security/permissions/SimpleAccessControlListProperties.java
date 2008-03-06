/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.repo.security.permissions;

public class SimpleAccessControlListProperties implements AccessControlListProperties
{
    private String aclId;
    
    private ACLType aclType;
    
    private Long aclVersion;
    
    private Boolean inherits;
    
    private Boolean latest;
    
    private Boolean versioned;
    
    public String getAclId()
    {
       return aclId;
    }

    public ACLType getAclType()
    {
       return aclType;
    }

    public Long getAclVersion()
    {
      return aclVersion;
    }

    public Boolean getInherits()
    {
        return inherits;
    }

    public Boolean isLatest()
    {
        return latest;
    }

    public Boolean isVersioned()
    {
       return versioned;
    }

    public void setAclId(String aclId)
    {
        this.aclId = aclId;
    }

    public void setAclType(ACLType aclType)
    {
        this.aclType = aclType;
    }

    public void setAclVersion(Long aclVersion)
    {
        this.aclVersion = aclVersion;
    }

    public void setInherits(boolean inherits)
    {
        this.inherits = inherits;
    }

    public void setLatest(boolean latest)
    {
        this.latest = latest;
    }

    public void setVersioned(boolean versioned)
    {
        this.versioned = versioned;
    }
    
    

}
