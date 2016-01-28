/* 
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see http://www.gnu.org/licenses/.
 */

package org.alfresco.repo.virtual.bundle;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.alfresco.repo.security.permissions.NodePermissionEntry;
import org.alfresco.repo.security.permissions.PermissionEntry;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.repo.security.permissions.impl.SimpleNodePermissionEntry;
import org.alfresco.repo.security.permissions.impl.traitextender.PermissionServiceExtension;
import org.alfresco.repo.security.permissions.impl.traitextender.PermissionServiceTrait;
import org.alfresco.repo.virtual.VirtualizationException;
import org.alfresco.repo.virtual.ref.Reference;
import org.alfresco.repo.virtual.store.VirtualStore;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionContext;
import org.alfresco.service.namespace.QName;
import org.alfresco.traitextender.SpringBeanExtension;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class VirtualPermissionServiceExtension extends
            SpringBeanExtension<PermissionServiceExtension, PermissionServiceTrait> implements
            PermissionServiceExtension
{
    private static Log logger = LogFactory.getLog(VirtualPermissionServiceExtension.class);

    private VirtualStore smartStore;

    public VirtualPermissionServiceExtension()
    {
        super(PermissionServiceTrait.class);
    }

    public void setSmartStore(VirtualStore smartStore)
    {
        this.smartStore = smartStore;
    }

    public AccessStatus hasPermission(NodeRef nodeRef, String perm)
    {
        PermissionServiceTrait theTrait = getTrait();
        if (!Reference.isReference(nodeRef))
        {
            return theTrait.hasPermission(nodeRef,
                                          perm);
        }
        else
        {
            Reference reference = Reference.fromNodeRef(nodeRef);
            AccessStatus virtualAccessStatus = smartStore.hasPermission(reference,
                                                                          perm);
            if (!AccessStatus.UNDETERMINED.equals(virtualAccessStatus))
            {
                return virtualAccessStatus;
            }
            else
            {
                NodeRef nodeToAdhereTo = establishPermisisonAdherence(reference);
                if (nodeToAdhereTo == null)
                {
                    return AccessStatus.UNDETERMINED;
                }
                else
                {
                    return theTrait.hasPermission(nodeToAdhereTo,
                                                  perm);
                }
            }
        }
    }

    public AccessStatus hasPermission(NodeRef nodeRef, PermissionReference perm)
    {
        PermissionServiceTrait theTrait = getTrait();
        if (!Reference.isReference(nodeRef))
        {
            return theTrait.hasPermission(nodeRef,
                                          perm);
        }
        else
        {
            Reference reference = Reference.fromNodeRef(nodeRef);
            AccessStatus virtualAccessStatus = smartStore.hasPermission(reference,
                                                                          perm);
            if (!AccessStatus.UNDETERMINED.equals(virtualAccessStatus))
            {
                return virtualAccessStatus;
            }
            else
            {
                NodeRef nodeToAdhereTo = establishPermisisonAdherence(reference);
                if (nodeToAdhereTo == null)
                {
                    return AccessStatus.UNDETERMINED;
                }
                else
                {
                    return theTrait.hasPermission(nodeToAdhereTo,
                                                  perm);
                }
            }
        }
    }

    @Override
    public PermissionReference getAllPermissionReference()
    {
        return getTrait().getAllPermissionReference();
    }

    @Override
    public Set<PermissionReference> getSettablePermissionReferences(QName type)
    {
        return getTrait().getSettablePermissionReferences(type);
    }

    @Override
    public Set<PermissionReference> getSettablePermissionReferences(NodeRef nodeRef)
    {
        if (!Reference.isReference(nodeRef))
        {
            return getTrait().getSettablePermissionReferences(nodeRef);
        }
        else
        {
            return Collections.emptySet();
        }
    }

    @Override
    public NodePermissionEntry getSetPermissions(NodeRef nodeRef)
    {
        PermissionServiceTrait theTrait = getTrait();
        if (!Reference.isReference(nodeRef))
        {
            return theTrait.getSetPermissions(nodeRef);
        }
        else
        {
            Reference reference = Reference.fromNodeRef(nodeRef);
            NodePermissionEntry virtualSetPermissions = smartStore.getSetPermissions(reference);

            NodeRef nodeToAdhereTo = establishPermisisonAdherence(reference);
            List<? extends PermissionEntry> actualPermissionEntries;
            boolean inheritPermissions = false;
            if (nodeToAdhereTo != null)
            {
                NodePermissionEntry actualSetPermissions = theTrait.getSetPermissions(nodeToAdhereTo);
                actualPermissionEntries = actualSetPermissions.getPermissionEntries();
                inheritPermissions = actualSetPermissions.inheritPermissions();
            }
            else
            {
                actualPermissionEntries = Collections.emptyList();
                inheritPermissions = false;
            }

            List<PermissionEntry> mergedEntries = new LinkedList<>();

            List<? extends PermissionEntry> virtualPermissionEntries = virtualSetPermissions.getPermissionEntries();
            Set<QName> overridenPermissions = new HashSet<>();
            for (PermissionEntry permissionEntry : virtualPermissionEntries)
            {
                overridenPermissions.add(permissionEntry.getPermissionReference().getQName());
                mergedEntries.add(permissionEntry);
            }
            for (PermissionEntry permissionEntry : actualPermissionEntries)
            {
                if (!overridenPermissions.contains(permissionEntry.getPermissionReference().getQName()))
                {
                    mergedEntries.add(permissionEntry);
                }
            }
            return new SimpleNodePermissionEntry(nodeRef,
                                                 inheritPermissions,
                                                 mergedEntries);
        }
    }

    private NodeRef establishPermisisonAdherence(Reference reference)
    {
        NodeRef nodeToAdhereTo = smartStore.adhere(reference,
                                                     VirtualStore.FILING_OR_MATERIAL_ADHERENCE);
        if (logger.isDebugEnabled())
        {
            logger.debug("Could not establish permission adherence for " + reference.toString());
        }

        return nodeToAdhereTo;
    }

    @Override
    public NodePermissionEntry explainPermission(NodeRef nodeRef, PermissionReference perm)
    {
        return getTrait().explainPermission(smartStore.materializeIfPossible(nodeRef),
                                            perm);
    }

    @Override
    public void deletePermissions(NodePermissionEntry nodePermissionEntry)
    {
        if (!Reference.isReference(nodePermissionEntry.getNodeRef()))
        {
            getTrait().deletePermissions(nodePermissionEntry);
        }
        else
        {
            // no action taken on virtual nodes
        }
    }

    @Override
    public void deletePermission(PermissionEntry permissionEntry)
    {
        if (!Reference.isReference(permissionEntry.getNodeRef()))
        {
            getTrait().deletePermission(permissionEntry);
        }
        else
        {
            // no action taken on virtual nodes
        }
    }

    @Override
    public void setPermission(PermissionEntry permissionEntry)
    {
        if (!Reference.isReference(permissionEntry.getNodeRef()))
        {
            getTrait().setPermission(permissionEntry);
        }
        else
        {
            // no action taken on virtual nodes
        }
    }

    @Override
    public void setPermission(NodePermissionEntry nodePermissionEntry)
    {
        if (!Reference.isReference(nodePermissionEntry.getNodeRef()))
        {
            getTrait().setPermission(nodePermissionEntry);
        }
        else
        {
            // no action taken on virtual nodes
        }
    }

    @Override
    public PermissionReference getPermissionReference(QName qname, String permissionName)
    {
        return getTrait().getPermissionReference(permissionName);
    }

    @Override
    public PermissionReference getPermissionReference(String permissionName)
    {
        return getTrait().getPermissionReference(permissionName);
    }

    @Override
    public String getPermission(PermissionReference permissionReference)
    {
        return getTrait().getPermission(permissionReference);
    }

    @Override
    public void deletePermissions(String recipient)
    {
        getTrait().deletePermissions(recipient);
    }

    @Override
    public NodePermissionEntry getSetPermissions(StoreRef storeRef)
    {
        return getTrait().getSetPermissions(storeRef);
    }

    @Override
    public String getOwnerAuthority()
    {
        return getTrait().getOwnerAuthority();
    }

    @Override
    public String getAllAuthorities()
    {
        return getTrait().getAllAuthorities();
    }

    @Override
    public String getAllPermission()
    {
        return getTrait().getAllPermission();
    }

    @Override
    public Set<AccessPermission> getPermissions(NodeRef nodeRef)
    {
        PermissionServiceTrait theTrait = getTrait();
        if (!Reference.isReference(nodeRef))
        {
            return theTrait.getPermissions(nodeRef);
        }
        else
        {
            Reference reference = Reference.fromNodeRef(nodeRef);
            Set<AccessPermission> virtualSetPermissions = smartStore.getAllSetPermissions(reference);
            NodeRef nodeToAdhereTo = establishPermisisonAdherence(reference);
            Set<AccessPermission> mergedEntries = new HashSet<>(virtualSetPermissions);
            if (nodeToAdhereTo != null)
            {
                Set<AccessPermission> actualSetPermissions = theTrait.getPermissions(nodeToAdhereTo);
                mergedEntries.addAll(actualSetPermissions);
            }
            return mergedEntries;
        }
    }

    @Override
    public Set<AccessPermission> getAllSetPermissions(NodeRef nodeRef)
    {
        PermissionServiceTrait theTrait = getTrait();
        if (!Reference.isReference(nodeRef))
        {
            return theTrait.getAllSetPermissions(nodeRef);
        }
        else
        {
            Reference reference = Reference.fromNodeRef(nodeRef);
            Set<AccessPermission> virtualSetPermissions = smartStore.getAllSetPermissions(reference);
            NodeRef nodeToAdhereTo = establishPermisisonAdherence(reference);
            Set<AccessPermission> actualSetPermissions;
            if (nodeToAdhereTo != null)
            {
                actualSetPermissions = theTrait.getAllSetPermissions(nodeToAdhereTo);
            }
            else
            {
                actualSetPermissions = Collections.emptySet();
            }

            Set<String> overridenPermissions = new HashSet<>();
            Set<AccessPermission> mergedEntries = new HashSet<>();
            for (AccessPermission permission : virtualSetPermissions)
            {
                overridenPermissions.add(permission.getPermission());
                mergedEntries.add(permission);
            }
            for (AccessPermission permission : actualSetPermissions)
            {
                if (!overridenPermissions.contains(permission.getPermission()))
                {
                    mergedEntries.add(permission);
                }
            }

            return mergedEntries;
        }
    }

    @Override
    public Set<String> getSettablePermissions(NodeRef nodeRef)
    {
        if (!Reference.isReference(nodeRef))
        {
            return getTrait().getSettablePermissions(nodeRef);
        }
        else
        {
            return Collections.emptySet();
        }
    }

    @Override
    public Set<String> getSettablePermissions(QName type)
    {
        return getTrait().getSettablePermissions(type);
    }

    @Override
    public AccessStatus hasReadPermission(NodeRef nodeRef)
    {
        return getTrait().hasReadPermission(nodeRef);
    }

    @Override
    public Set<String> getReaders(Long aclId)
    {
        return getTrait().getReaders(aclId);
    }

    @Override
    public Set<String> getReadersDenied(Long aclId)
    {
        return getTrait().getReadersDenied(aclId);
    }

    @Override
    public AccessStatus hasPermission(Long aclID, PermissionContext context, String permission)
    {
        return getTrait().hasPermission(aclID,
                                        context,
                                        permission);
    }

    @Override
    public void deletePermissions(NodeRef nodeRef)
    {
        if (!Reference.isReference(nodeRef))
        {
            getTrait().deletePermissions(nodeRef);
        }
        else
        {
            // no action taken on virtual nodes
        }
    }

    @Override
    public void clearPermission(NodeRef nodeRef, String authority)
    {
        if (!Reference.isReference(nodeRef))
        {
            getTrait().clearPermission(nodeRef,
                                       authority);
        }
        else
        {
            // no action taken on virtual nodes
        }
    }

    @Override
    public void deletePermission(NodeRef nodeRef, String authority, String permission)
    {
        if (!Reference.isReference(nodeRef))
        {
            getTrait().deletePermission(nodeRef,
                                        authority,
                                        permission);
        }
        else
        {
            // no action taken on virtual nodes
        }
    }

    @Override
    public void setPermission(NodeRef nodeRef, String authority, String permission, boolean allow)
    {
        if (!Reference.isReference(nodeRef))
        {
            getTrait().setPermission(nodeRef,
                                     authority,
                                     permission,
                                     allow);
        }
        else
        {
            // no action taken on virtual nodes
        }
    }

    @Override
    public void setInheritParentPermissions(NodeRef nodeRef, boolean inheritParentPermissions)
    {
        if (!Reference.isReference(nodeRef))
        {
            getTrait().setInheritParentPermissions(nodeRef,
                                                   inheritParentPermissions);
        }
        else
        {
            // no action taken on virtual nodes
        }

    }

    @Override
    public boolean getInheritParentPermissions(NodeRef nodeRef)
    {
        if (!Reference.isReference(nodeRef))
        {
            return getTrait().getInheritParentPermissions(nodeRef);
        }
        else
        {
            return false;
        }
    }

    @Override
    public void setPermission(StoreRef storeRef, String authority, String permission, boolean allow)
    {
        getTrait().setPermission(storeRef,
                                 authority,
                                 permission,
                                 allow);
    }

    @Override
    public void deletePermission(StoreRef storeRef, String authority, String permission)
    {
        getTrait().deletePermission(storeRef,
                                    authority,
                                    permission);
    }

    @Override
    public void clearPermission(StoreRef storeRef, String authority)
    {
        getTrait().clearPermission(storeRef,
                                   authority);
    }

    @Override
    public void deletePermissions(StoreRef storeRef)
    {
        getTrait().deletePermissions(storeRef);
    }

    @Override
    public Set<AccessPermission> getAllSetPermissions(StoreRef storeRef)
    {
        return getTrait().getAllSetPermissions(storeRef);
    }

    @Override
    public Set<String> getAuthorisations()
    {
        return getTrait().getAuthorisations();
    }

}
