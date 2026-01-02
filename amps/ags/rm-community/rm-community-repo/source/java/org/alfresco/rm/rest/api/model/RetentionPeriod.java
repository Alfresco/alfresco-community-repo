/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rm.rest.api.model;

/**
 * Retention period values
 */
public enum RetentionPeriod
{
    DAY("day"),
    END_OF_FINANCIAL_MONTH("fmend"),
    END_OF_FINANCIAL_QUARTER("fqend"),
    END_OF_FINANCIAL_YEAR("fyend"),
    IMMEDIATELY("immediately"),
    END_OF_MONTH("monthend"),
    END_OF_QUARTER("quarterend"),
    END_OF_YEAR("yearend"),
    MONTH("month"),
    NONE("none"),
    QUARTER("quarter"),
    WEEK("week"),
    XML_DURATION("duration"),
    YEAR("year");

    public final String periodName;

    RetentionPeriod(String periodName)
    {
        this.periodName = periodName;
    }
}
