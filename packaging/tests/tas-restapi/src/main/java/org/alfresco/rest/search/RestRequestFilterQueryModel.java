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
 * Hacked together by 'gethin' on '2017-03-23 10:59' from 'Alfresco Search REST API' swagger file
 * Base Path {@linkplain /alfresco/api/-default-/public/search/versions/1}
 */
public class RestRequestFilterQueryModel extends TestModel implements IRestModel<RestRequestFilterQueryModel>
{
    @JsonProperty(value = "entry")
    RestRequestFilterQueryModel model;

    @Override
    public RestRequestFilterQueryModel onModel()
    {
        return model;
    }

    public RestRequestFilterQueryModel()
    {
        super();
    }

    public RestRequestFilterQueryModel(String query)
    {
        super();
        this.query = query;
    }

    public RestRequestFilterQueryModel(String query, List<String> tags)
    {
        super();
        this.query = query;
        this.tags = tags;
    }

    /**
    The filter query
    */	        

    private String query;	    
    private List<String> tags;

    public String getQuery()
    {
        return this.query;
    }

    public void setQuery(String query)
    {
        this.query = query;
    }				

    public List<String> getTags()
    {
        return this.tags;
    }

    public void setTags(List<String> tags)
    {
        this.tags = tags;
    }				
}
 
