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
package org.alfresco.rest.model.body;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.utility.model.TestModel;

public class RestNodeLockBodyModel extends TestModel implements IRestModel<RestNodeLockBodyModel>
{
    
    @JsonProperty(value = "entry")
    RestNodeLockBodyModel model;

    @Override
    public RestNodeLockBodyModel onModel()
    {
        return model;
    }

    @JsonProperty
    private int timeToExpire;

    @JsonProperty
    private String type;
    
    @JsonProperty
    private String lifetime;
    
    public String getLifetime()
    {
        return lifetime;
    }

    public void setLifetime(String lifetime)
    {
        this.lifetime = lifetime;
    }

    public int getTimeToExpire()
    {
        return timeToExpire;
    }

    /*
     * Set in seconds lock time
     * if lock time = 0 or not set, the lock never expires
     */
    public void setTimeToExpire(int timeToExpire)
    {
        this.timeToExpire = timeToExpire;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }



}
