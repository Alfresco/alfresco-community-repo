/*-
 * #%L
 * alfresco-tas-restapi
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
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
/*
 * Copyright (C) 2017 Alfresco Software Limited.
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
package org.alfresco.rest.search;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Object representation of a single sort clause.
 *
 * <pre>
 *  "sort": [
 *     {
 *       "type": "FIELD",
 *       "field": "string",
 *       "ascending": false
 *     }
 *   ]
 * </pre>
 */
public class SortClause
{

    private String type;
    private String field;
    private boolean ascending;

    public SortClause()
    {
    }

    @JsonCreator
    public SortClause(@JsonProperty("type") String type,
                   @JsonProperty("field")  String field,
                   @JsonProperty("ascending") boolean ascending)
    {
        this.type = type;
        this.field = field;
        this.ascending = ascending;
    }

    public String getType()
    {
        return type;
    }

    public String getField()
    {
        return field;
    }

    public boolean isAscending()
    {
        return ascending;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setField(String field) {
        this.field = field;
    }

    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }

    /**
     * Shortcut method for creating a sort clause.
     *
     * @param type the sort type (e.g. FIELD)
     * @param fieldname the field name.
     * @param ascending ascending (true) or descending (false).
     * @return a new {@link SortClause} instance.
     */
    public static SortClause from(String type, String fieldname, boolean ascending)
    {
        return new SortClause(type, fieldname, ascending);
    }
}
