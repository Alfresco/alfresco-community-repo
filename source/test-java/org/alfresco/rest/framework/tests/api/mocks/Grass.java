
package org.alfresco.rest.framework.tests.api.mocks;

/**
 * Simple mock pojo for serialization for relationships
 * 
 * @author Gethin James
 */
public class Grass
{
    final private String id;
    private String color = "green";

    public Grass(String id)
    {
        super();
        this.id = id;
    }

    public String getId()
    {
        return this.id;
    }

    public String getColor()
    {
        return this.color;
    }

    public void setColor(String color)
    {
        this.color = color;
    }
}
