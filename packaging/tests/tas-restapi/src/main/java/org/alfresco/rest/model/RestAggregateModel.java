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
package org.alfresco.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.utility.model.TestModel;

public class RestAggregateModel extends TestModel implements IRestModel<RestAggregateModel>
{
    @JsonProperty(value = "aggregate")
    RestAggregateModel model;
    
    @JsonProperty(required = true)
    private int numberOfRatings;
    private String average;

    @Override
    public RestAggregateModel onModel()
    {
        return model;
    }
    
    public int getNumberOfRatings()
    {
        return numberOfRatings;
    }

    public void setNumberOfRatings(int numberOfRatings)
    {
        this.numberOfRatings = numberOfRatings;
    }

    public String getAverage()
    {
        return average;
    }

    public void setAverage(String average)
    {
        this.average = average;
    }
}
