package org.alfresco.rest.framework.tests.api.mocks3;

/**
 * A goat has a herd
 *
 * @author Gethin James
 */
public class Herd
{
    String name;
    int quantity = 56;
    
    public Herd(String name)
    {
        super();
        this.name = name;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the quantity
     */
    public int getQuantity()
    {
        return this.quantity;
    }

    /**
     * @param quantity the quantity to set
     */
    public void setQuantity(int quantity)
    {
        this.quantity = quantity;
    }
    
}
