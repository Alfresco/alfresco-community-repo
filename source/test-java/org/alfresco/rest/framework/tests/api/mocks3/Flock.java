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
