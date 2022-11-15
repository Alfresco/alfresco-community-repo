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
import org.alfresco.utility.data.RandomData;
import org.alfresco.utility.model.TestModel;

public class RestProcessVariableModel extends TestModel implements IRestModel<RestProcessVariableModel>
{  
    private String name;
    
    private String value;
    
    private String type;
    
    @JsonProperty(value = "entry")
    RestProcessVariableModel model;

    public RestProcessVariableModel()
    {
    }
    
    public RestProcessVariableModel(String name, String value, String type)
    {
        this.name = name;
        this.value = value;
        this.type = type;
    }

    @Override
    public RestProcessVariableModel onModel()
    {
        return model;
    }
    
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public static RestProcessVariableModel getRandomProcessVariableModel(String variableType){
        return new RestProcessVariableModel(RandomData.getRandomName("name"), RandomData.getRandomName("value"), variableType);    
    }
}
