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

import java.util.Arrays;
import java.util.List;

import org.alfresco.rest.framework.resource.EmbeddedEntityResource;
import org.alfresco.rest.framework.resource.UniqueId;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * Simple mock pojo for serialization for relationships
 *
 * @author Gethin James
 */
public class Goat implements Animal
{
    public enum DEMEANOR {ANGRY,CRAZY,MELLOW}
    private String name = "Billy";
    private int age = 2;
    private DEMEANOR mood = DEMEANOR.MELLOW;
    private String grassId = "3";
    private List<String> favourites = Arrays.asList("front","back","over the road");
    
    public Goat()
    {
        super();
    }
    
    public Goat(String grassId)
    {
        super();
        this.grassId = grassId;
    }
    
    @UniqueId(name="goatId")
    public String getName()
    {
        return this.name;
    }
    public void setName(String name)
    {
        this.name = name;
    }
    public int getAge()
    {
        return this.age;
    }
    public void setAge(int age)
    {
        this.age = age;
    }
    public DEMEANOR getMood()
    {
        return this.mood;
    }
    public void setMood(DEMEANOR mood)
    {
        this.mood = mood;
    }

    @EmbeddedEntityResource(propertyName = "grass", entityResource=GrassEntityResource.class)
    @JsonIgnore
    public String getGrassId()
    {
        return this.grassId;
    }

    public void setGrassId(String grassId)
    {
        this.grassId = grassId;
    }

    public List<String> getFavourites()
    {
        return this.favourites;
    }

    public void setFavourites(List<String> favourites)
    {
        this.favourites = favourites;
    }
}
