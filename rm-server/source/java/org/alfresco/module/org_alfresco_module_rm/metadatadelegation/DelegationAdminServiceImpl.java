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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.module.org_alfresco_module_rm.metadatadelegation;

import static org.alfresco.util.collections.CollectionUtils.transform;

import org.alfresco.module.org_alfresco_module_rm.metadatadelegation.DelegationException.ChainedDelegationUnsupported;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.collections.Function;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Neil Mc Erlean
 * @since 3.0.a
 */
public class DelegationAdminServiceImpl implements DelegationAdminService
{
    private DelegationRegistry registry;
    private NodeService        nodeService;

    public void setNodeService(NodeService service)
    {
        this.nodeService = service;
    }

    public void setDelegationRegistry(DelegationRegistry registry)
    {
        this.registry = registry;
    }

    @Override public Delegation attachDelegate(NodeRef nodeRef, NodeRef delegateNodeRef, QName assocType)
    {
        final Delegation delegation = getDelegationForAssociation(assocType);

        // Prevent the creation of chains of delegation from node A to B to C.

        // If any nodes are already delegating to nodeRef for the specified assoc, then we can't chain.
        final List<AssociationRef> existingDelegatorAssocs = nodeService.getSourceAssocs(nodeRef, assocType);
        if ( !existingDelegatorAssocs.isEmpty())
        {
            final List<NodeRef> existingDelegators = transform(existingDelegatorAssocs,
                    new Function<AssociationRef, NodeRef>()
                    {
                        @Override public NodeRef apply(AssociationRef assocRef)
                        {
                            return assocRef.getSourceRef();
                        }
                    });
            throw new ChainedDelegationUnsupported("Cannot attach delegate", existingDelegators);
        }

        // Likewise if this delegate node is already itself delegating elsewhere, we cannot chain.
        final List<AssociationRef> existingDelegateAssocs = nodeService.getTargetAssocs(delegateNodeRef, assocType);
        if ( !existingDelegateAssocs.isEmpty())
        {
            // If it's not empty, it should only have one value in it, but just in case...
            final List<NodeRef> existingDelegates = transform(existingDelegateAssocs,
                    new Function<AssociationRef, NodeRef>()
                    {
                        @Override public NodeRef apply(AssociationRef assocRef)
                        {
                            return assocRef.getTargetRef();
                        }
                    });
            throw new ChainedDelegationUnsupported("Cannot attach delegate", existingDelegates);
        }

        // OK. We're good to go. We're not making a chain here.
        nodeService.createAssociation(nodeRef, delegateNodeRef, assocType);

        return delegation;
    }

    private Delegation getDelegationForAssociation(QName assocType)
    {
        final Delegation delegation = registry.getDelegateForAssociation(assocType);

        if (delegation == null)
        {
            throw new IllegalArgumentException("No " + Delegation.class.getSimpleName() +
                                               " configured for assocType " + assocType);
        }
        return delegation;
    }

    @Override public Delegation detachDelegate(NodeRef nodeRef, QName assocType)
    {
        // Is the association there?
        final List<AssociationRef> assocs = nodeService.getTargetAssocs(nodeRef, assocType);

        if (assocs == null || assocs.isEmpty())
        {
            return null;
        }
        else
        {
            Delegation result = getDelegationForAssociation(assocType);

            // There should only be one such association... but we'll remove them all just in case
            for (AssociationRef assocRef : assocs)
            {
                nodeService.removeAssociation(nodeRef, assocRef.getTargetRef(), assocType);
            }

            return result;
        }
    }

    @Override public Delegation getDelegationFor(QName aspectName)
    {
        Delegation delegation = null;

        for (Delegation d : getDefinedDelegations())
        {
            if (d.getAspects().contains(aspectName))
            {
                delegation = d;
                break;
            }
        }
        return delegation;
    }

    @Override public Set<Delegation> getDelegationsFrom(NodeRef nodeRef)
    {
        final Set<Delegation> allDelegations = getDefinedDelegations();

        final Set<Delegation> result = new HashSet<>();
        for (Delegation d : allDelegations)
        {
            final QName assocType = d.getAssocType();
            if ( !nodeService.getTargetAssocs(nodeRef, assocType).isEmpty())
            {
                result.add(d);
            }
        }

        return result;
    }

    @Override public Set<Delegation> getDefinedDelegations()
    {
        return registry.getDelegations();
    }
}
