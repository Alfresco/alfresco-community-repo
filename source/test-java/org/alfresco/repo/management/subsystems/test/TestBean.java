package org.alfresco.repo.management.subsystems.test;

import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.springframework.beans.factory.BeanNameAware;

/**
 * A bean to test out 'composite property' features.
 * 
 * @see ChildApplicationContextFactory
 */
public class TestBean implements BeanNameAware
{
    private String id;
    private long longProperty;
    private boolean boolProperty;
    private String anotherStringProperty;

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
     */
    public void setBeanName(String name)
    {
        this.id = name;
    }

    public String getId()
    {
        return this.id;
    }

    public long getLongProperty()
    {
        return this.longProperty;
    }

    public void setLongProperty(long longProperty)
    {
        this.longProperty = longProperty;
    }

    public boolean isBoolProperty()
    {
        return this.boolProperty;
    }

    public void setBoolProperty(boolean boolProperty)
    {
        this.boolProperty = boolProperty;
    }

    public String getAnotherStringProperty()
    {
        return this.anotherStringProperty;
    }

    public void setAnotherStringProperty(String anotherStringProperty)
    {
        this.anotherStringProperty = anotherStringProperty;
    }
}
