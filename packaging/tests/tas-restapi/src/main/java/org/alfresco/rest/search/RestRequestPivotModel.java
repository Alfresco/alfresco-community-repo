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

import com.fasterxml.jackson.annotation.JsonProperty;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.utility.model.TestModel;

/**
 * Generated by 'gethin' on '2017-03-23 10:07' from 'Alfresco Search REST API' swagger file Generated from 'Alfresco Search REST API' swagger file Base Path {@linkplain /alfresco/api/-default-/public/search/versions/1}
 */
public class RestRequestPivotModel extends TestModel implements IRestModel<RestRequestPivotModel>
{
    @JsonProperty(value = "entry")
    RestRequestPivotModel model;

    @Override
    public RestRequestPivotModel onModel()
    {
        return model;
    }

    /**
     * A key corresponding to a matching field facet label.
     */

    private String key;

    private List<RestRequestPivotModel> pivots;

    public String getKey()
    {
        return this.key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public List<RestRequestPivotModel> getPivots()
    {
        return this.pivots;
    }

    public void setPivots(List<RestRequestPivotModel> pivots)
    {
        this.pivots = pivots;
    }
}
