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

import static org.alfresco.module.org_alfresco_module_rm.referredmetadata.ReferredMetadataException.MetadataReferralNotFound;
import static org.alfresco.module.org_alfresco_module_rm.referredmetadata.ReferredMetadataException.TypeMetadataReferralUnsupported;

import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import java.io.Serializable;
import java.util.Map;

/**
 * This service provides read-only access to linked metadata. It is primarily concerned with data transfer.
 * For an overview, see the package javadoc.
 *
 * @author Neil Mc Erlean
 * @since 2.4.a
 */
public interface ReferredMetadataService
{
    /**
     * Checks if the specified referrer has an attached {@link MetadataReferral} for the specified aspect.
     *
     * @param potentialReferrer the referrer which may or may not be linked to a referent node.
     * @param aspectName        the name of the aspect.
     * @return whether the node is linked to a referent node for the specified aspect.
     * @throws InvalidNodeRefException if the supplied referrer does not exist.
     * @throws MetadataReferralNotFound if no {@link MetadataReferral} is defined for the specified aspect.
     */
    boolean isReferringMetadata(NodeRef potentialReferrer, QName aspectName);

    /**
     * Gets the referent node for the specified aspect, if there is one.
     *
     * @param referrer   the node whose referent is sought.
     * @param aspectName the aspect name.
     * @return the referent of the provided referrer if there is one, else {@code null}.
     * @throws InvalidNodeRefException if the supplied referrer does not exist.
     * @throws MetadataReferralNotFound if no {@link MetadataReferral} is defined for the specified aspect.
     */
    NodeRef getReferentNode(NodeRef referrer, QName aspectName);

    /**
     * Gets all the property values from the referent node for the specified aspect.
     *
     * @param referrer   the referring node.
     * @param aspectName the aspect name which holds the properties we want.
     * @return the property values as obtained from the referent node.
     */
    Map<QName, Serializable> getReferredProperties(NodeRef referrer, QName aspectName);

    /**
     * Gets the specified property value from the referent node.
     *
     * @param referrer    the referring node.
     * @param propertyName the property name whose value is sought.
     * @return the property value as obtained from the referent node.
     * @throws IllegalArgumentException if the specified property is not defined.
     * @throws TypeMetadataReferralUnsupported if the specified property is not defined on an aspect.
     */
    Serializable getReferredProperty(NodeRef referrer, QName propertyName);

    /**
     * Determines if the specified aspect is present on a node's referent.
     *
     * @param referrer   the referring node.
     * @param aspectName the aspect which is to be checked on the referent node.
     * @return Returns true if the aspect has been applied to the referent node,
     *      otherwise false
     */
    boolean hasReferredAspect(NodeRef referrer, QName aspectName);

    /**
     * Gets all {@link MetadataReferral referrals} currently attached to the specified node.
     *
     * @param potentialReferrer the node whose attached {@link MetadataReferral referrals} are sought.
     * @return Returns a map of all attached {@link MetadataReferral referrals} for the specified nodeRef.
     *         The map has the form {@code (key, value) = (MetadataReferral, referent Node for that Referral)}
     *         The map may be empty but will not be {@code null}.
     */
    Map<MetadataReferral, NodeRef> getAttachedReferrals(NodeRef potentialReferrer);
}

