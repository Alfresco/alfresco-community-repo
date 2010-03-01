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
package org.alfresco.repo.domain.hibernate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.domain.DbAccessControlEntry;
import org.alfresco.repo.domain.DbAccessControlList;
import org.alfresco.repo.domain.DbAccessControlListChangeSet;
import org.alfresco.repo.domain.DbAccessControlListMember;
import org.alfresco.repo.domain.DbAuthority;
import org.alfresco.repo.domain.DbPermission;
import org.alfresco.repo.domain.DbPermissionKey;
import org.alfresco.repo.security.permissions.ACLCopyMode;
import org.alfresco.repo.security.permissions.ACLType;
import org.alfresco.repo.security.permissions.AccessControlEntry;
import org.alfresco.repo.security.permissions.SimpleAccessControlListProperties;
import org.alfresco.repo.security.permissions.impl.AclDaoComponent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.CallbackException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

/**
 * The hibernate persisted class for node permission entries.
 * 
 * @author andyh
 */
public class DbAccessControlListImpl extends LifecycleAdapter implements DbAccessControlList, Serializable
{
    private static AclDaoComponent s_aclDaoComponent;

    private static final long serialVersionUID = 3123277428227075648L;

    private static Log logger = LogFactory.getLog(DbAccessControlListImpl.class);

    private Long id;

    private Long version;

    private String aclId;

    private long aclVersion;

    private boolean latest;

    private boolean inherits;

    private int aclType;

    private Long inheritedAclId;

    private boolean versioned;

    private DbAccessControlListChangeSet aclChangeSet;

    private Long inheritsFrom;

    private boolean requiresVersion;

    public static void setAclDaoComponent(AclDaoComponent aclDaoComponent)
    {
        s_aclDaoComponent = aclDaoComponent;
    }

    public DbAccessControlListImpl()
    {
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(128);
        sb.append("DbAccessControlListImpl").append("[ id=").append(id).append(", version=").append(version).append(", aclId=").append(aclId).append(", aclVersion=").append(
                aclVersion).append(", latest=").append(latest).append(", inherits=").append(inherits).append(", aclType=").append(ACLType.getACLTypeFromId(aclType)).append(
                ", inheritedAclId=").append(inheritedAclId).append(", versioned=").append(versioned).append(", changesetId=").append(aclChangeSet).append(", inheritsFrom=")
                .append(inheritsFrom).append(", requiresVersion=").append(requiresVersion).append("]");
        return sb.toString();
    }

    
    
    /**
     * Support cascade delete of ACLs from DM nodes (which cascade delete the ACL)
     */
    @Override
    public boolean onDelete(Session session) throws CallbackException
    {
        s_aclDaoComponent.onDeleteAccessControlList(id);
        return super.onDelete(session);
    }

    public Long getId()
    {
        return id;
    }

    /**
     * Hibernate use
     */
    @SuppressWarnings("unused")
    private void setId(Long id)
    {
        this.id = id;
    }

    public Long getVersion()
    {
        return version;
    }

    /**
     * For Hibernate use
     */
    @SuppressWarnings("unused")
    private void setVersion(Long version)
    {
        this.version = version;
    }

    public boolean getInherits()
    {
        return inherits;
    }

    public void setInherits(boolean inherits)
    {
        this.inherits = inherits;
    }

    public String getAclId()
    {
        return aclId;
    }

    public void setAclId(String aclId)
    {
        this.aclId = aclId;
    }

    public ACLType getAclType()
    {
        return ACLType.getACLTypeFromId(aclType);
    }

    public void setAclType(ACLType aclType)
    {
        this.aclType = aclType.getId();
    }

    /**
     * Hibernate
     */

    private int getType()
    {
        return aclType;
    }

    private void setType(int aclType)
    {
        this.aclType = aclType;
    }

    public long getAclVersion()
    {
        return aclVersion;
    }

    public void setAclVersion(long aclVersion)
    {
        this.aclVersion = aclVersion;
    }

    public Long getInheritedAclId()
    {
        return inheritedAclId;
    }

    public void setInheritedAclId(Long inheritedAclId)
    {
        this.inheritedAclId = inheritedAclId;
    }

    public boolean isLatest()
    {
        return latest;
    }

    public void setLatest(boolean latest)
    {
        this.latest = latest;
    }

    public boolean isVersioned()
    {
        return versioned;
    }

    public void setVersioned(boolean versioned)
    {
        this.versioned = versioned;
    }

    public DbAccessControlListChangeSet getAclChangeSet()
    {
        return aclChangeSet;
    }

    public void setAclChangeSet(DbAccessControlListChangeSet aclChangeSet)
    {
        this.aclChangeSet = aclChangeSet;
    }

    public static DbAccessControlList find(Session session)
    {
        // TODO: Needs to use a query
        throw new UnsupportedOperationException("TODO");
    }

    public Long getInheritsFrom()
    {
        return inheritsFrom;
    }

    public void setInheritsFrom(Long id)
    {
        this.inheritsFrom = id;
    }

    public DbAccessControlList getCopy(Long parentAcl, ACLCopyMode mode)
    {
        return s_aclDaoComponent.getDbAccessControlListCopy(this.getId(), parentAcl, mode);
    }

    public static DbAccessControlList createLayeredAcl(Long indirectedAcl)
    {
        SimpleAccessControlListProperties properties = new SimpleAccessControlListProperties();
        properties.setAclType(ACLType.LAYERED);
        Long id = s_aclDaoComponent.createAccessControlList(properties);
        if (indirectedAcl != null)
        {
            s_aclDaoComponent.mergeInheritedAccessControlList(indirectedAcl, id);
        }
        return s_aclDaoComponent.getDbAccessControlList(id);
    }

    public boolean getRequiresVersion()
    {
        return requiresVersion;
    }

    public void setRequiresVersion(boolean requiresVersion)
    {
        this.requiresVersion = requiresVersion;
    }

}
