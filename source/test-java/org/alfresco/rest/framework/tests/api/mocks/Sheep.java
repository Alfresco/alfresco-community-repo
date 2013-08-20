package org.alfresco.rest.framework.tests.api.mocks;

import org.alfresco.rest.framework.resource.UniqueId;

/**
 * Simple mock pojo for serialization for relationships
 *
 * @author Gethin James
 */
public class Sheep implements Animal
{
    private String id;
    private String name = "Dolly";
    private int age = 3;
    
    public Sheep(String id)
    {
        super();
        this.id = id;
    }

    @UniqueId(name="sheepGuid")
    public String getId()
    {
        return this.id;
    }

    public void setId(String id)
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

    public int getAge()
    {
        return this.age;
    }

    public void setAge(int age)
    {
        this.age = age;
    }
}
