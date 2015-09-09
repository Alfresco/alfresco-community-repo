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

import org.alfresco.module.org_alfresco_module_rm.referredmetadata.ReferredMetadataException.MetadataReferralAlreadyExists;
import org.alfresco.module.org_alfresco_module_rm.referredmetadata.ReferredMetadataException.InvalidMetadataReferral;
import org.alfresco.service.namespace.QName;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This is a registry of {@link MetadataReferral}s which have been defined in the system.
 *
 * @author Neil Mc Erlean
 * @since 2.4.a
 */
public class ReferralRegistry
{
    private final Set<MetadataReferral> metadataReferrals = new HashSet<>();

    public void register(MetadataReferral metadataReferral)
    {
        // Various validation steps to do here to ensure we get consistent, sensible referrals registered.
        if (metadataReferrals.contains(metadataReferral))
        {
            throw new MetadataReferralAlreadyExists("Cannot register duplicate referral", metadataReferral);
        }
        for (MetadataReferral existingMetadataReferral : metadataReferrals)
        {
            if (existingMetadataReferral.getAssocType().equals(metadataReferral.getAssocType()))
            {
                throw new InvalidMetadataReferral("Cannot register two referrals with the same assocType. " +
                                            "Existing: " + existingMetadataReferral +
                                            " New: " + metadataReferral);
            }
            // Yes this is a for loop inside a for loop but we're assuming these sets will not be large.
            for (QName existingAspect : existingMetadataReferral.getAspects())
            {
                if (metadataReferral.getAspects().contains(existingAspect))
                {
                    throw new InvalidMetadataReferral("Cannot register two referrals with the same aspect. " +
                                                "Existing: " + existingMetadataReferral +
                                                " New: " + metadataReferral);
                }
            }
        }

        this.metadataReferrals.add(metadataReferral);
    }

    public Set<MetadataReferral> getMetadataReferrals()
    {
        return Collections.unmodifiableSet(metadataReferrals);
    }

    /**
     * Gets the {@link MetadataReferral} which is defined to use the specified {@code assocType}.
     *
     * @param assocType the peer association type whose {@link MetadataReferral} is sought.
     * @return the {@link MetadataReferral} defined to use the specified {@code assocType} if there is one, else {@code null}.
     */
    public MetadataReferral getReferralForAssociation(QName assocType)
    {
        for (MetadataReferral mr : metadataReferrals)
        {
            if (mr.getAssocType().equals(assocType))
            {
                return mr;
            }
        }
        return null;
    }

    /**
     * Gets the {@link MetadataReferral} which is defined to handle the specified aspect.
     *
     * @param aspectName the name of the aspect whose {@link MetadataReferral} is sought.
     * @return the {@link MetadataReferral} handling the specified aspect if there is one, else {@code null}.
     */
    public MetadataReferral getReferralForAspect(QName aspectName)
    {
        for (MetadataReferral mr : metadataReferrals)
        {
            if (mr.getAspects().contains(aspectName))
            {
                return mr;
            }
        }
        return null;
    }
}
