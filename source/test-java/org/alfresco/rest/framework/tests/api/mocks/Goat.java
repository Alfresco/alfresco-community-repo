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
