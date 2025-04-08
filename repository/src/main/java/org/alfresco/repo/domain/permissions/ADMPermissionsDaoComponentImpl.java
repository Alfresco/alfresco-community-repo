/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.domain.permissions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alfresco.repo.security.permissions.ACLType;
import org.alfresco.repo.security.permissions.SimpleAccessControlListProperties;
import org.alfresco.repo.security.permissions.impl.AclChange;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * ADM permissions dao component impl
 * 
 * Manage creation and deletion of ACL entries for the new ADM ACL implementation
 * 
 * @author andyh
 *
 */
public class ADMPermissionsDaoComponentImpl extends AbstractPermissionsDaoComponentImpl
{
    @Override
    protected CreationReport createAccessControlList(NodeRef nodeRef, boolean inherit, Acl existing)
    {
        if (existing == null)
        {
            SimpleAccessControlListProperties properties = new SimpleAccessControlListProperties();
            properties.setAclType(ACLType.DEFINING);
            properties.setInherits(inherit);
            properties.setVersioned(false);

            Acl acl = aclDaoComponent.createAccessControlList(properties);
            long id = acl.getId();

            List<AclChange> changes = new ArrayList<AclChange>();
            changes.add(new AclDAOImpl.AclChangeImpl(null, id, null, acl.getAclType()));
            changes.addAll(getACLDAO(nodeRef).setInheritanceForChildren(nodeRef, id, null));
            getACLDAO(nodeRef).setAccessControlList(nodeRef, acl);
            return new CreationReport(acl, changes);
        }
        SimpleAccessControlListProperties properties;
        Long id;
        List<AclChange> changes;
        Acl acl;
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
            properties.setVersioned(false);

            acl = aclDaoComponent.createAccessControlList(properties);
            id = acl.getId();

            changes = new ArrayList<AclChange>();
            changes.add(new AclDAOImpl.AclChangeImpl(existing.getId(), id, existing.getAclType(), acl.getAclType()));
            changes.addAll(aclDaoComponent.mergeInheritedAccessControlList(existing.getId(), id));
            // set this to inherit to children
            changes.addAll(getACLDAO(nodeRef).setInheritanceForChildren(nodeRef, id, aclDaoComponent.getInheritedAccessControlList(existing.getId())));

            getACLDAO(nodeRef).setAccessControlList(nodeRef, acl);
            return new CreationReport(acl, changes);
        case LAYERED:
            throw new IllegalStateException("Layering is not supported for DM permissions");
        default:
            throw new IllegalStateException("Unknown type " + existing.getAclType());
        }
    }

    public void deletePermissions(NodeRef nodeRef)
    {
        Acl acl = null;
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
                if (acl.getInherits())
                {
                    // Check the primary parent to set inheritance
                    Long inheritsFrom = getACLDAO(nodeRef).getInheritedAcl(nodeRef);
                    if (inheritsFrom != null)
                    {
                        inheritsFrom = aclDaoComponent.getInheritedAccessControlList(inheritsFrom);
                        getACLDAO(nodeRef).setAccessControlList(nodeRef, aclDaoComponent.getAcl(inheritsFrom));
                        List<AclChange> changes = new ArrayList<AclChange>();
                        changes.addAll(getACLDAO(nodeRef).setInheritanceForChildren(nodeRef, inheritsFrom, aclDaoComponent.getInheritedAccessControlList(acl.getId())));
                        getACLDAO(nodeRef).updateChangedAcls(nodeRef, changes);
                        aclDaoComponent.deleteAccessControlList(acl.getId());
                    }
                    else
                    {
                        replaceWithCleanDefiningAcl(nodeRef, acl);
                    }
                }
                else
                {
                    replaceWithCleanDefiningAcl(nodeRef, acl);
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

    /**
     * @param nodeRef
     *            NodeRef
     * @param acl
     *            Acl
     */
    private void replaceWithCleanDefiningAcl(NodeRef nodeRef, Acl acl)
    {
        // TODO: could just clear out existing
        SimpleAccessControlListProperties properties = new SimpleAccessControlListProperties();
        properties = new SimpleAccessControlListProperties();
        properties.setAclType(ACLType.DEFINING);
        properties.setInherits(Boolean.FALSE);
        properties.setVersioned(false);

        Acl newAcl = aclDaoComponent.createAccessControlList(properties);
        long id = newAcl.getId();

        getACLDAO(nodeRef).setAccessControlList(nodeRef, newAcl);
        List<AclChange> changes = new ArrayList<AclChange>();
        changes.addAll(getACLDAO(nodeRef).setInheritanceForChildren(nodeRef, id, acl.getInheritedAcl()));
        getACLDAO(nodeRef).updateChangedAcls(nodeRef, changes);
        aclDaoComponent.deleteAccessControlList(acl.getId());
    }
}
