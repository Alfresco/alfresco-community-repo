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
    // Author's implementation note
    // ----------------------------
    //
    // I can imagine that these services would be potentially useful in core Alfresco.
    // However they are not yet full services and couldn't be moved as is into core.
    // They solve a very specific RM problem in a fairly generic way that should allow
    // someone to use them as the basis for fuller services within core.
    //
    // The problem they solve is that of 'classified renditions' whereby the classification
    // metadata on a node should appear to be on its renditions as well. This particular problem
    // is simplified by the fact that renditions are not 'normal' nodes, as they are usually
    // not accessed directly. This implementation also relies on the fact that RM already
    // has interceptors for checking content classification and we can programmatically add
    // the calls to metadata referral within the ContentClassificationService.
    //
    // To solve the problem of Metadata Referral in a general way would require the provision
    // of 'MetadataReferral' interceptors that could sit in front of the NodeService. Only in this
    // way could the services be used declaratively, thus minimising their impact on calling code.
    // To add these to core would require careful assessment of their impact, not least in
    // performance terms. This work is beyond RM's scope at this stage.
    // The addition of such interceptors to the NodeService would also ensure that any metadata
    // returned to e.g. Share for a particular node could automatically include 'linked' metadata
    // which would be important.
    //
    // There are further enhancements that should be considered if these were developed into
    // fuller services including the automatic registration of behaviours (onAddAspect, onRemoveAspect)
    // for the aspect types which are linked. Currently these behaviours need to be hand-coded.
    // See ClassifiedAspect.java for an example.

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

    @Override public MetadataReferral attachReferrer(NodeRef referrer, NodeRef referent, QName aspectName)
    {
        final MetadataReferral metadataReferral = registry.getReferralForAspect(aspectName);
        if (metadataReferral == null)
        {
            throw new IllegalArgumentException("No defined " + MetadataReferral.class.getSimpleName() +
                                               " for aspect " + aspectName);
        }
        final QName assocType = metadataReferral.getAssocType();

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

    @Override public MetadataReferral detachReferrer(NodeRef referrer, QName aspectName)
    {
        final MetadataReferral referral = registry.getReferralForAspect(aspectName);
        final QName assocType = referral.getAssocType();

        // Is the association there?
        final List<AssociationRef> assocs = nodeService.getTargetAssocs(referrer, assocType);

        if (assocs == null || assocs.isEmpty())
        {
            return null;
        }
        else
        {
            // There should only be one such association... but we'll remove them all just in case
            for (AssociationRef assocRef : assocs)
            {
                nodeService.removeAssociation(referrer, assocRef.getTargetRef(), assocType);
            }

            return referral;
        }
    }

    @Override public Set<MetadataReferral> getAttachedReferralsFrom(NodeRef referrer)
    {
        final Set<MetadataReferral> allMetadataReferrals = registry.getMetadataReferrals();

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
}
