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

/**
 * Basic implementation of access control list properties
 * 
 * @author andyh
 */
public class SimpleAccessControlListProperties implements AccessControlListProperties
{
    private Long id;

    private ACLType aclType;

    private Long aclVersion;

    private Boolean inherits;

    private Boolean latest;

    private Boolean versioned;

    private String aclId;

    private Long aclChangeSetId;

    // Default constructor
    public SimpleAccessControlListProperties()
    {
    }

    public SimpleAccessControlListProperties(AccessControlListProperties props)
    {
        this.id = props.getId();
        this.aclType = props.getAclType();
        this.aclVersion = props.getAclVersion();
        this.inherits = props.getInherits();
        this.latest = props.isLatest();
        this.versioned = props.isVersioned();
        this.aclId = props.getAclId();
    }

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

    /**
     * Set the acl id
     * 
     * @param aclId
     */
    public void setAclId(String aclId)
    {
        this.aclId = aclId;
    }

    /**
     * Set the acl type
     * 
     * @param aclType
     */
    public void setAclType(ACLType aclType)
    {
        this.aclType = aclType;
    }

    /**
     * Set the acl version
     * 
     * @param aclVersion
     */
    public void setAclVersion(Long aclVersion)
    {
        this.aclVersion = aclVersion;
    }

    /**
     * Set inheritance
     * 
     * @param inherits
     */
    public void setInherits(boolean inherits)
    {
        this.inherits = inherits;
    }

    /**
     * Set latest
     * 
     * @param latest
     */
    public void setLatest(boolean latest)
    {
        this.latest = latest;
    }

    /**
     * Set versioned
     * 
     * @param versioned
     */
    public void setVersioned(boolean versioned)
    {
        this.versioned = versioned;
    }

    public Long getId()
    {
        return id;
    }

    /**
     * Set the id
     * 
     * @param id
     */
    public void setId(Long id)
    {
        this.id = id;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.security.permissions.AccessControlListProperties#getChangeSetId()
     */
    @Override
    public Long getAclChangeSetId()
    {
        return aclChangeSetId;
    }

    public void setAclChangeSetId(Long aclChangeSetId)
    {
        this.aclChangeSetId = aclChangeSetId;
    }

}
