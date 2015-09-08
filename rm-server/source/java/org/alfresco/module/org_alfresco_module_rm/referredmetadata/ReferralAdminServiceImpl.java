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
package org.alfresco.module.org_alfresco_module_rm.referredmetadata;

import static org.alfresco.util.collections.CollectionUtils.transform;

import org.alfresco.module.org_alfresco_module_rm.referredmetadata.ReferredMetadataException.ChainedMetadataReferralUnsupported;
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
 * @since 2.4.a
 */
public class ReferralAdminServiceImpl implements ReferralAdminService
{
    private ReferralRegistry registry;
    private NodeService      nodeService;

    public void setNodeService(NodeService service)
    {
        this.nodeService = service;
    }

    public void setReferralRegistry(ReferralRegistry registry)
    {
        this.registry = registry;
    }

    @Override public MetadataReferral attachReferrer(NodeRef referrer, NodeRef referent, QName assocType)
    {
        final MetadataReferral metadataReferral = getReferralForAssociation(assocType);

        // Prevent the creation of chains of metadata linking from node A to B to C.

        // If any nodes are already linked to referrer for the specified assoc, then we can't chain.
        final List<AssociationRef> existingReferrerAssocs = nodeService.getSourceAssocs(referrer, assocType);
        if ( !existingReferrerAssocs.isEmpty())
        {
            final List<NodeRef> existingReferrers = transform(existingReferrerAssocs,
                    new Function<AssociationRef, NodeRef>()
                    {
                        @Override public NodeRef apply(AssociationRef assocRef)
                        {
                            return assocRef.getSourceRef();
                        }
                    });
            throw new ChainedMetadataReferralUnsupported("Cannot attach referrer", existingReferrers);
        }

        // Likewise if this referent node is already itself linked elsewhere, we cannot chain.
        final List<AssociationRef> existingReferentAssocs = nodeService.getTargetAssocs(referent, assocType);
        if ( !existingReferentAssocs.isEmpty())
        {
            // If it's not empty, it should only have one value in it, but just in case...
            final List<NodeRef> existingReferents = transform(existingReferentAssocs,
                    new Function<AssociationRef, NodeRef>()
                    {
                        @Override public NodeRef apply(AssociationRef assocRef)
                        {
                            return assocRef.getTargetRef();
                        }
                    });
            throw new ChainedMetadataReferralUnsupported("Cannot attach referent", existingReferents);
        }

        // OK. We're good to go. We're not making a chain here.
        nodeService.createAssociation(referrer, referent, assocType);

        return metadataReferral;
    }

    /** Gets the {@link MetadataReferral} which uses the specified {@code assocType}. */
    private MetadataReferral getReferralForAssociation(QName assocType)
    {
        final MetadataReferral metadataReferral = registry.getReferralForAssociation(assocType);

        if (metadataReferral == null)
        {
            throw new IllegalArgumentException("No " + MetadataReferral.class.getSimpleName() +
                                               " configured for assocType " + assocType);
        }
        return metadataReferral;
    }

    @Override public MetadataReferral detachReferrer(NodeRef referrer, QName assocType)
    {
        // Is the association there?
        final List<AssociationRef> assocs = nodeService.getTargetAssocs(referrer, assocType);

        if (assocs == null || assocs.isEmpty())
        {
            return null;
        }
        else
        {
            MetadataReferral result = getReferralForAssociation(assocType);

            // There should only be one such association... but we'll remove them all just in case
            for (AssociationRef assocRef : assocs)
            {
                nodeService.removeAssociation(referrer, assocRef.getTargetRef(), assocType);
            }

            return result;
        }
    }

    @Override public MetadataReferral getReferralFor(QName aspectName)
    {
        MetadataReferral metadataReferral = null;

        for (MetadataReferral d : getDefinedReferrals())
        {
            if (d.getAspects().contains(aspectName))
            {
                metadataReferral = d;
                break;
            }
        }
        return metadataReferral;
    }

    @Override public Set<MetadataReferral> getAttachedReferralsFrom(NodeRef referrer)
    {
        final Set<MetadataReferral> allMetadataReferrals = getDefinedReferrals();

        final Set<MetadataReferral> result = new HashSet<>();
        for (MetadataReferral d : allMetadataReferrals)
        {
            final QName assocType = d.getAssocType();
            if ( !nodeService.getTargetAssocs(referrer, assocType).isEmpty())
            {
                result.add(d);
            }
        }

        return result;
    }

    @Override public MetadataReferral getAttachedReferralFrom(NodeRef referrer, QName aspectName)
    {
        final Set<MetadataReferral> allMetadataReferrals = getAttachedReferralsFrom(referrer);

        for (MetadataReferral d : allMetadataReferrals)
        {
            if (d.getAspects().contains(aspectName)) return d;
        }
        return null;
    }

    @Override public Set<MetadataReferral> getDefinedReferrals()
    {
        return registry.getMetadataReferrals();
    }
}
