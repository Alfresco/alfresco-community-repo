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

import org.alfresco.module.org_alfresco_module_rm.metadatadelegation.DelegationException.DelegationAlreadyExists;
import org.alfresco.module.org_alfresco_module_rm.metadatadelegation.DelegationException.InvalidDelegation;
import org.alfresco.service.namespace.QName;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This is a registry of {@link Delegation delegations} which have been defined in the system.
 *
 * @author Neil Mc Erlean
 * @since 3.0.a
 */
public class DelegationRegistry
{
    private final Set<Delegation> delegations = new HashSet<>();

    public void register(Delegation delegation)
    {
        // Various validation steps to do here to ensure we get consistent, sensible Delegations registered.
        if (delegations.contains(delegation))
        {
            throw new DelegationAlreadyExists("Cannot register duplicate delegation", delegation);
        }
        for (Delegation existingDelegation : delegations)
        {
            if (existingDelegation.getAssocType().equals(delegation.getAssocType()))
            {
                throw new InvalidDelegation("Cannot register two delegations with the same assocType. " +
                                            "Existing: " + existingDelegation +
                                            " New: " + delegation);
            }
            // Yes this is a for loop inside a for loop but we're assuming these sets will not be large.
            for (QName existingAspect : existingDelegation.getAspects())
            {
                if (delegation.getAspects().contains(existingAspect))
                {
                    throw new InvalidDelegation("Cannot register two delegations with the same aspect. " +
                                                "Existing: " + existingDelegation +
                                                " New: " + delegation);
                }
            }
        }

        this.delegations.add(delegation);
    }

    public Set<Delegation> getDelegations()
    {
        return Collections.unmodifiableSet(delegations);
    }

    public Delegation getDelegateForAssociation(QName assocType)
    {
        for (Delegation d : delegations)
        {
            if (d.getAssocType().equals(assocType))
            {
                return d;
            }
        }
        return null;
    }
}
