/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.api.model.rules;

import com.fasterxml.jackson.annotation.JsonValue;

import org.alfresco.service.Experimental;

/** The reason why a rule set applies to a folder. */
@Experimental
public enum InclusionType
{
    OWNED, INHERITED, LINKED;

    /**
     * Load an InclusionType from a given string (case insensitively).
     *
     * @param inclusionType The given string.
     * @return The inclusion type.
     */
    public static InclusionType from(String inclusionType)
    {
        return InclusionType.valueOf(inclusionType.toUpperCase());
    }

    /**
     * The lower case version of the inclusion type.
     *
     * @return A lowercase string.
     */
    @JsonValue
    public String toString()
    {
        return super.toString().toLowerCase();
    }
}
