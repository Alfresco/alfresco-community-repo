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

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Interface for implementing property extenders. A property extender is used to calculate additional properties based on the properties that are being changed on a node.
 * <p>
 * Contract:
 * <p>
 * By implementing this interface you take the responsibility to correctly implement {@link Object#equals} and {@link Object#hashCode()} methods. This is required to ensure that only one instance of the extender is registered for specific business case, even if the {@link PropertyExtendersHolder#registerExtender(PropertyExtender)} is invoked many times.
 * <p>
 * The {@link Object#toString()} method should be implemented for logging purposes.
 */
@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface PropertyExtender
{
    /**
     * Calculate additional node properties.
     *
     * @param context
     *            the context of the calculation, which contains the property changes on a node.
     * @return result containing calculated additional properties mapping. Empty map when no relevant properties were provided in the context.
     * @throws AlfrescoRuntimeException
     *             for expected calculation process failures.
     * @throws RuntimeException
     *             for any unexpected errors during the calculation process.
     */
    CalculationResult calculate(CalculationContext context);
}
