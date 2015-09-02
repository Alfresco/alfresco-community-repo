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

import org.alfresco.module.org_alfresco_module_rm.metadatadelegation.DelegationException.InvalidDelegation;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.QName;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * A Delegation is a definition of a {@link #aspects set of aspects} whose metadata are to be delegated.
 * Using a Delegation, you can attach a delegate node to any node and {@code hasAspect} and
 * {@code getPropert[y|ies]} calls can be delegated to the delegate node.
 * <p/>
 * The connection between the nodes is made with a specified {@link #assocType peer association}.
 *<p/>
 * Note that a Delegation is not an instance of a link between two nodes, but the definition of such a link.
 *
 * @author Neil Mc Erlean
 * @since 3.0.a
 */
public class Delegation
{
    private DictionaryService  dictionaryService;
    private DelegationRegistry delegationRegistry;
    private Set<QName> aspects;
    private QName      assocType;

    public Delegation()
    {
        // Intentionally empty
    }

    public void setDictionaryService(DictionaryService service)
    {
        this.dictionaryService = service;
    }

    public void setDelegationRegistry(DelegationRegistry registry)
    {
        this.delegationRegistry = registry;
    }

    public void setAssocType(QName assocType)
    {
        this.assocType = assocType;
    }

    public void setAspects(Set<QName> aspects)
    {
        this.aspects = aspects;
    }

    public void validateAndRegister()
    {
        if (this.assocType == null)
        {
            throw new InvalidDelegation("Illegal null assocType");
        }
        if (aspects == null || aspects.isEmpty())
        {
            throw new InvalidDelegation("Illegal null or empty aspects set");
        }
        if (dictionaryService.getAssociation(assocType) == null)
        {
            throw new InvalidDelegation("Association not found: " + assocType);
        }
        for (QName aspect : aspects)
        {
            if (dictionaryService.getAspect(aspect) == null)
            {
                throw new InvalidDelegation("Aspect not found: " + aspect);
            }
        }

        this.delegationRegistry.register(this);
    }

    /** Gets the type of the peer association linking the node to its delegate. */
    public QName getAssocType()
    {
        return assocType;
    }

    /** Gets the set of aspects which are being delegated. */
    public Set<QName> getAspects()
    {
        return Collections.unmodifiableSet(aspects);
    }

    @Override public int hashCode()
    {
        return Objects.hash(aspects, assocType);
    }

    @Override public boolean equals(Object other)
    {
        boolean result = false;
        if (other instanceof Delegation)
        {
            Delegation that = (Delegation)other;
            result = this.aspects.equals(that.aspects) &&
                     this.assocType.equals(that.assocType);
        }
        return result;
    }

    @Override public String toString()
    {
        StringBuilder result = new StringBuilder();
        result.append(this.getClass().getSimpleName()).append(':')
              .append("--").append(assocType).append("->")
              .append(aspects);
        return result.toString();
    }
}
