/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2026 Alfresco Software Limited
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
package org.alfresco.repo.node.propertyextender;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.namespace.QName;

class PropertyCalculator
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyCalculator.class);

    static ExtendedProperties calculateProperties(Set<PropertyExtender> extenders, Map<QName, Serializable> propertyChanges)
    {
        if (propertyChanges.isEmpty() || extenders.isEmpty())
        {
            return new ExtendedProperties(propertyChanges, Collections.emptyMap());
        }

        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace("Calculating additional properties for: {}", propertyChanges.keySet().stream()
                    .map(QName::toPrefixString)
                    .toList());
        }
        Map<QName, Serializable> calculated = extenders.stream()
                .map(ext -> runCalculation(ext, propertyChanges))
                .flatMap(result -> result.additionalProperties().entrySet().stream())
                .collect(HashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), HashMap::putAll);
        if (calculated.isEmpty())
        {
            LOGGER.trace("No additional properties calculated");
            return new ExtendedProperties(propertyChanges, Collections.emptyMap());
        }

        if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace("Additional properties were calculated: {}", calculated.keySet().stream()
                    .map(QName::toPrefixString)
                    .toList());
        }
        return new ExtendedProperties(propertyChanges, calculated);
    }

    static CalculationResult runCalculation(PropertyExtender extender, Map<QName, Serializable> newProperties)
    {
        try
        {
            return extender.calculate(new CalculationContext(newProperties));
        }
        catch (AlfrescoRuntimeException e)
        {
            throw e;
        }
        catch (RuntimeException e)
        {
            throw new AlfrescoRuntimeException("Unexpected failure during properties calculation process", e);
        }
    }

    record ExtendedProperties(
            Map<QName, Serializable> originalProperties,
            Map<QName, Serializable> additionalProperties)
    {
        boolean isExtended()
        {
            return !additionalProperties.isEmpty();
        }

        Map<QName, Serializable> mergeProperties()
        {
            if (isExtended())
            {
                var merged = new HashMap<>(originalProperties);
                merged.putAll(additionalProperties);
                return merged;
            }
            return originalProperties;
        }
    }

    private PropertyCalculator()
    {}
}
