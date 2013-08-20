package org.alfresco.rest.framework.tests.api.mocks3;

import org.alfresco.rest.framework.resource.UniqueId;
import org.alfresco.rest.framework.tests.api.mocks.Animal;
import org.alfresco.rest.framework.tests.api.mocks.Goat;

/**
 * Simple mock pojo for serialization for relationships
 *
 * @author Gethin James
 */
public class SlimGoat implements Animal
{
    String name = "Betty";
    Goat.DEMEANOR mood = Goat.DEMEANOR.ANGRY;
    
    public SlimGoat()
    {
        super();
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
 
    public Goat.DEMEANOR getMood()
    {
        return this.mood;
    }
    public void setMood(Goat.DEMEANOR mood)
    {
        this.mood = mood;
    }

}
