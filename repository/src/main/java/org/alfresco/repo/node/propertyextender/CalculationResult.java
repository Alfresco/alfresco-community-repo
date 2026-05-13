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
import java.util.Map;
import java.util.Objects;

import org.alfresco.service.namespace.QName;

/**
 * The result of the property extender calculation.
 *
 * @param additionalProperties
 *            the additional properties calculated by the property extender, which will be applied to the node together with the original property changes.
 */
public record CalculationResult(Map<QName, Serializable> additionalProperties)
{
    public static final CalculationResult NO_OP = new CalculationResult(Collections.emptyMap());

    public CalculationResult
    {
        Objects.requireNonNull(additionalProperties);
    }
}
