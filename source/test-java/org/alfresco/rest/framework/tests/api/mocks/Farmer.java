/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.rest.framework.tests.api.mocks;

import java.util.Date;

import org.alfresco.rest.framework.resource.EmbeddedEntityResource;
import org.alfresco.rest.framework.resource.UniqueId;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * Simple mock pojo for serialization
 *
 * @author Gethin James
 */
public class Farmer
{
    public enum size {LARGE,SMALL, MEDIUM}
    
    private String name = "Giles";
    private Date created = new Date();
    private int age = 54;
    private String id;
    private size farm = size.LARGE;
    private String sheepId;
    private String goatId;
    
    public Farmer()
    {
        super();
    }

    public Farmer(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Date getCreated()
    {
        return this.created;
    }

    public void setCreated(Date created)
    {
        this.created = created;
    }

    public int getAge()
    {
        return this.age;
    }

    public void setAge(int age)
    {
        this.age = age;
    }

    @UniqueId
    public String getId()
    {
        return this.id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public size getFarm()
    {
        return this.farm;
    }

    public void setFarm(size farm)
    {
        this.farm = farm;
    }

    @EmbeddedEntityResource(propertyName = "sheep", entityResource=SheepEntityResource.class)
    @JsonIgnore
    public String getSheepId()
    {
        return this.sheepId;
    }

    public void setSheepId(String sheepId)
    {
        this.sheepId = sheepId;
    }

    @EmbeddedEntityResource(propertyName = "goat", entityResource=GoatEntityResource.class)
    @JsonIgnore
    public String getGoatId()
    {
        return this.goatId;
    }

    public void setGoatId(String goatId)
    {
        this.goatId = goatId;
    }
    
    
}
