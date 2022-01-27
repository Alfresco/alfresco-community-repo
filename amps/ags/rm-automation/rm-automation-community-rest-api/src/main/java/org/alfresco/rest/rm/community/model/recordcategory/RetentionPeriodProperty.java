/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
package org.alfresco.rest.rm.community.model.recordcategory;

/**
 * The property of the dispositioned item that is used to calculate the "as of" period.
 */
public enum RetentionPeriodProperty
{
    /** Item created date. */
    CREATED_DATE("cm:created"),
    /** Record filed date. */
    DATE_FILED("rma:dateFiled"),
    /** Item cut off date. */
    CUT_OFF_DATE("rma:cutOffDate");

    String periodProperty;

    RetentionPeriodProperty(String periodProperty)
    {
        this.periodProperty = periodProperty;
    }

    public String getPeriodProperty()
    {
        return periodProperty;
    }
}
