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
package org.alfresco.repo.domain.hibernate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alfresco.repo.domain.DbAccessControlList;
import org.alfresco.repo.security.permissions.ACLType;
import org.alfresco.repo.security.permissions.AccessControlEntry;
import org.alfresco.repo.security.permissions.AccessControlList;
import org.alfresco.repo.security.permissions.SimpleAccessControlListProperties;
import org.alfresco.repo.security.permissions.impl.AclChange;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Old permissions dao component impl
 * 
 * @author andyh
 */
public class PermissionsDaoComponentImpl extends AbstractPermissionsDaoComponentImpl
{

    @Override
    protected CreationReport createAccessControlList(NodeRef nodeRef, boolean inherit, DbAccessControlList existing)
    {
        if (existing == null)
        {
            SimpleAccessControlListProperties properties = new SimpleAccessControlListProperties();
            properties.setAclType(ACLType.DEFINING);
            // Accept default versioning
            Long id = aclDaoComponent.createAccessControlList(properties);
            List<AclChange> changes = new ArrayList<AclChange>();
            DbAccessControlList acl = aclDaoComponent.getDbAccessControlList(id);
            changes.add(new AclDaoComponentImpl.AclChangeImpl(null, id, null, acl.getAclType()));
            changes.addAll(getACLDAO(nodeRef).setInheritanceForChildren(nodeRef, id));
            getACLDAO(nodeRef).setAccessControlList(nodeRef, acl);
            return new CreationReport(acl, changes);
        }
        SimpleAccessControlListProperties properties;
        Long id;
        List<AclChange> changes;
        DbAccessControlList acl;
        switch (existing.getAclType())
        {
        case OLD:
            throw new IllegalStateException("Can not mix old and new style permissions");
        case DEFINING:
            return new CreationReport(existing, Collections.<AclChange> emptyList());
        case FIXED:
        case GLOBAL:
        case SHARED:
            // create new defining, wire up and report changes to acl required.
            properties = new SimpleAccessControlListProperties();
            properties.setAclType(ACLType.DEFINING);
            properties.setInherits(existing.getInherits());
            // Accept default versioning
            id = aclDaoComponent.createAccessControlList(properties);
            changes = new ArrayList<AclChange>();
            acl = aclDaoComponent.getDbAccessControlList(id);
            changes.add(new AclDaoComponentImpl.AclChangeImpl(existing.getId(), id, existing.getAclType(), acl.getAclType()));
            changes.addAll(aclDaoComponent.mergeInheritedAccessControlList(existing.getId(), id));
            // set this to inherit to children
            changes.addAll(getACLDAO(nodeRef).setInheritanceForChildren(nodeRef, id));

            getACLDAO(nodeRef).setAccessControlList(nodeRef, acl);
            return new CreationReport(acl, changes);
        case LAYERED:
            // Need to get the indirected node ACL
            Long indirectAclId = getACLDAO(nodeRef).getIndirectAcl(nodeRef);
            Long inheritedAclId = getACLDAO(nodeRef).getInheritedAcl(nodeRef);

            // create new defining, wire up and report changes to acl required.
            properties = new SimpleAccessControlListProperties();
            properties.setAclType(ACLType.DEFINING);
            if (indirectAclId != null)
            {
                properties.setInherits(aclDaoComponent.getAccessControlListProperties(indirectAclId).getInherits());
            }
            // Accept default versioning
            id = aclDaoComponent.createAccessControlList(properties);
            changes = new ArrayList<AclChange>();
            acl = aclDaoComponent.getDbAccessControlList(id);
            changes.add(new AclDaoComponentImpl.AclChangeImpl(existing.getId(), id, existing.getAclType(), acl.getAclType()));
            if (indirectAclId != null)
            {
                AccessControlList indirectAcl = aclDaoComponent.getAccessControlList(indirectAclId);
                for (AccessControlEntry entry : indirectAcl.getEntries())
                {
                    if (entry.getPosition() == 0)
                    {
                        aclDaoComponent.setAccessControlEntry(id, entry);
                    }
                }
            }
            if (inheritedAclId != null)
            {
                changes.addAll(aclDaoComponent.mergeInheritedAccessControlList(inheritedAclId, id));
            }
            // set this to inherit to children
            changes.addAll(getACLDAO(nodeRef).setInheritanceForChildren(nodeRef, id));

            getACLDAO(nodeRef).setAccessControlList(nodeRef, acl);
            return new CreationReport(acl, changes);
        default:
            throw new IllegalStateException("Unknown type " + existing.getAclType());
        }
    }

    public void deletePermissions(NodeRef nodeRef)
    {
        DbAccessControlList acl = null;
        try
        {
            acl = getAccessControlList(nodeRef);
        }
        catch (InvalidNodeRefException e)
        {
            return;
        }
        if (acl != null)
        {
            switch (acl.getAclType())
            {
            case OLD:
                throw new IllegalStateException("Can not mix old and new style permissions");
            case DEFINING:

                if (acl.getInheritsFrom() != null)
                {
                    @SuppressWarnings("unused")
                    Long deleted = acl.getId();
                    Long inheritsFrom = acl.getInheritsFrom();
                    getACLDAO(nodeRef).setAccessControlList(nodeRef, aclDaoComponent.getDbAccessControlList(inheritsFrom));
                    List<AclChange> changes = new ArrayList<AclChange>();
                    changes.addAll(getACLDAO(nodeRef).setInheritanceForChildren(nodeRef, inheritsFrom));
                    getACLDAO(nodeRef).updateChangedAcls(nodeRef, changes);
                    aclDaoComponent.deleteAccessControlList(acl.getId());
                }
                else
                {
                    // TODO: could just cear out existing
                    @SuppressWarnings("unused")
                    Long deleted = acl.getId();
                    SimpleAccessControlListProperties properties = new SimpleAccessControlListProperties();
                    properties.setAclType(ACLType.DEFINING);
                    properties.setInherits(Boolean.FALSE);
                    // Accept default versioning
                    Long id = aclDaoComponent.createAccessControlList(properties);
                    getACLDAO(nodeRef).setAccessControlList(nodeRef, aclDaoComponent.getDbAccessControlList(id));
                    List<AclChange> changes = new ArrayList<AclChange>();
                    changes.addAll(getACLDAO(nodeRef).setInheritanceForChildren(nodeRef, id));
                    getACLDAO(nodeRef).updateChangedAcls(nodeRef, changes);
                    aclDaoComponent.deleteAccessControlList(acl.getId());
                }
                break;
            case FIXED:
                throw new IllegalStateException("Delete not supported for fixed permissions");
            case GLOBAL:
                throw new IllegalStateException("Delete not supported for global permissions");
            case SHARED:
                // nothing to do
                return;
            case LAYERED:
                throw new IllegalStateException("Layering is not supported for DM permissions");
            default:
                throw new IllegalStateException("Unknown type " + acl.getAclType());
            }
        }
    }
}
