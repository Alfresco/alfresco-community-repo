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
package org.alfresco.rest.framework.tests.api.mocks3;

import java.util.List;

import org.alfresco.rest.framework.resource.UniqueId;
import org.alfresco.rest.framework.resource.content.BinaryProperty;
import org.alfresco.rest.framework.tests.api.mocks.Sheep;
/*
 * A Flock of Sheep
 */
public class Flock
{
    String name;
    int quantity = 25;
    List<Sheep> sheep;
    BinaryProperty photo;
 
    public Flock()
    {
        super();
    }
    
    public Flock(String name, int quantity, List<Sheep> sheep, BinaryProperty photo)
    {
        super();
        this.name = name;
        this.quantity = quantity;
        this.sheep = sheep;
        this.photo = photo;
    }
    
    @UniqueId
    public String getName()
    {
        return this.name;
    }
    public void setName(String name)
    {
        this.name = name;
    }
    public int getQuantity()
    {
        return this.quantity;
    }
    public void setQuantity(int quantity)
    {
        this.quantity = quantity;
    }
    public List<Sheep> getSheep()
    {
        return this.sheep;
    }
    public void setSheep(List<Sheep> sheep)
    {
        this.sheep = sheep;
    }
    public BinaryProperty getPhoto()
    {
        return this.photo;
    }
    public void setPhoto(BinaryProperty photo)
    {
        this.photo = photo;
    }
}
