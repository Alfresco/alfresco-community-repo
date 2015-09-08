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

import static org.alfresco.module.org_alfresco_module_rm.referredmetadata.ReferredMetadataException.ChainedMetadataReferralUnsupported;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import java.util.Set;

/**
 * A service to manage the referral of aspect metadata.
 * Using this service a node can be {@link #attachReferrer linked} to a referrer node for a specific set of aspects.
 * (Note that this referrer node must already exist within the database.)
 * Then any read request for relevant metadata such as hasAspect or getProperties can be delegated to the
 * linked node.
 * <p/>
 * For a link to be made, there must be a {@link #getDefinedReferrals() defined MetadataReferral} already in the system.
 * This means that a peer-association type will have to have been declared and that a spring bean will have to have
 * defined which aspects are to be handled by this {@link MetadataReferral}.
 *
 * @author Neil Mc Erlean
 * @since 2.4.a
 */
public interface ReferralAdminService
{
    /**
     * Creates a link between two nodes such that the first {@code referrer} can 'inherit' or reuse some aspect
     * metadata from another node - the {@code referrer}.
     * <p/>
     * Note that links can currently only extend between two pairs of nodes and cannot be chained.
     *
     * @param referrer  the node which is to inherit additional metadata.
     * @param referent  the node which will provide the additional metadata.
     * @param assocType the type of the peer association which will link the two nodes.
     * @return a {@link MetadataReferral} object which defines the link type.
     * @throws ChainedMetadataReferralUnsupported if an attempt is made to attach nodes such that a chain would be made.
     */
    // FIXME Remove assocType entirely from the API
    MetadataReferral attachReferrer(NodeRef referrer, NodeRef referent, QName assocType);

    /**
     * Removes an existing metadata link between two nodes.
     *
     * @param referrer  the node which has been linked to a metadata source.
     * @param assocType the type of the peer association forming the link.
     * @return the removed {@link MetadataReferral}.
     */
    MetadataReferral detachReferrer(NodeRef referrer, QName assocType);

    /**
     * Gets the set of defined {@link MetadataReferral}s.
     *
     * @return the set of defined {@link MetadataReferral}.
     */
    Set<MetadataReferral> getDefinedReferrals();

    /**
     * Gets the {@link MetadataReferral} which contains the specified {@code aspectName} if there is one.
     * Note that only one {@link MetadataReferral} may declare that it handles any particular aspect.
     *
     * @param aspectName the name of the aspect whose {@link MetadataReferral} is sought.
     * @return the {@link MetadataReferral} which handles the specified aspect, if there is one.
     */
    MetadataReferral getReferralFor(QName aspectName);

    /**
     * Gets the set of {@link MetadataReferral}s which are currently applied from the specified {@code referrer}.
     * From these, the types of peer associations which are linked to the specified
     * {@code referrer} as well as the aspect types that are handled can be retrieved.
     *
     * @param referrer the NodeRef whose {@link MetadataReferral}s are sought.
     * @return the set of {@link MetadataReferral}s from the specified referrer.
     */
    Set<MetadataReferral> getAttachedReferralsFrom(NodeRef referrer);

    /**
     * Gets the {@link MetadataReferral} from the specified {@code referrer} for the specified {@code aspectName},
     * if there is one.
     *
     * @param referrer   the node whose {@link MetadataReferral} is sought.
     * @param aspectName the aspect name for which a {@link MetadataReferral} is sought.
     * @return the {@link MetadataReferral} which is attached to the specified node if there is one, else {@code null}.
     */
    MetadataReferral getAttachedReferralFrom(NodeRef referrer, QName aspectName);
}
