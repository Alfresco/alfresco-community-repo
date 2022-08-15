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
package org.alfresco.rest.search;

import java.util.List;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.utility.model.TestModel;

/**
 * Represents a facet interval.
 */

public class FacetInterval extends TestModel implements IRestModel<FacetInterval>
{
    private String field;
    private String label;
    private List<RestRequestFacetSetModel> sets;

    public FacetInterval()
    {
    }

    public FacetInterval(String field, String label, List<RestRequestFacetSetModel> sets)
    {
        this.field = field;
        this.label = label;
        this.sets = sets;
    }

    public String getField()
    {
        return field;
    }

    public String getLabel()
    {
        return label;
    }

    public List<RestRequestFacetSetModel> getSets()
    {
        return sets;
    }

    public void setField(String field)
    {
        this.field = field;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public void setSets(List<RestRequestFacetSetModel> sets)
    {
        this.sets = sets;
    }

    @Override
    public FacetInterval onModel()
    {
        return null;
    }
}
