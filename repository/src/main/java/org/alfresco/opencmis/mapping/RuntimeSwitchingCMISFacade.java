/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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
package org.alfresco.opencmis.mapping;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.alfresco.opencmis.dictionary.CMISNodeInfo;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;

public class RuntimeSwitchingCMISFacade implements CMISFacade
{
    private static final Logger LOGGER = LoggerFactory.getLogger(RuntimeSwitchingCMISFacade.class);
    private CMISFacade fallbackFacade;
    private final Map<String, CMISFacadeProvider> facadeProviders;
    private boolean failOnManyFacades;

    public RuntimeSwitchingCMISFacade(Map<String, CMISFacadeProvider> facadeProviders)
    {
        this.facadeProviders = Objects.requireNonNull(facadeProviders);
    }

    public void setFallbackFacade(CMISFacade fallbackFacade)
    {
        this.fallbackFacade = Objects.requireNonNull(fallbackFacade);
    }

    public void setFailOnManyFacades(boolean failOnManyFacades)
    {
        this.failOnManyFacades = failOnManyFacades;
    }

    @Override
    public CMISNodeInfo createNodeInfo(NodeRef nodeRef)
    {
        return getTargetFacade().createNodeInfo(nodeRef);
    }

    @Override
    public CMISNodeInfo createNodeInfo(AssociationRef assocRef)
    {
        return getTargetFacade().createNodeInfo(assocRef);
    }

    @Override
    public boolean isWorkingCopy(NodeRef nodeRef)
    {
        return getTargetFacade().isWorkingCopy(nodeRef);
    }

    @Override
    public String constructObjectId(String currentNodeId, String pwcVersionLabel)
    {
        return getTargetFacade().constructObjectId(currentNodeId, pwcVersionLabel);
    }

    CMISFacade getTargetFacade()
    {
        return getProvidedFacade().orElseGet(this::getRequiredFallbackFacade);
    }

    private Optional<CMISFacade> getProvidedFacade()
    {
        if (facadeProviders.isEmpty())
        {
            return Optional.empty();
        }

        if (!failOnManyFacades)
        {
            return facadeProviders.values().stream()
                    .map(CMISFacadeProvider::getCMISFacade)
                    .filter(Objects::nonNull)
                    .findFirst();
        }

        final Map<String, CMISFacade> allFacades = facadeProviders.entrySet().stream()
                .map(e -> {
                    final CMISFacade facade = e.getValue().getCMISFacade();
                    if (facade == null)
                    {
                        return null;
                    }
                    return Map.entry(e.getKey(), facade);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (allFacades.isEmpty())
        {
            return Optional.empty();
        }

        if (allFacades.size() > 1)
        {
            LOGGER.error("Many runtime facades ({}) are not allowed.", allFacades.keySet());
            throw new IllegalStateException("Many runtime facades are not allowed.");
        }

        return Optional.of(allFacades.values().iterator().next());
    }

    private CMISFacade getRequiredFallbackFacade()
    {
        if (fallbackFacade == null)
        {
            throw new IllegalStateException("Fallback Facade is not set.");
        }
        return fallbackFacade;
    }
}
