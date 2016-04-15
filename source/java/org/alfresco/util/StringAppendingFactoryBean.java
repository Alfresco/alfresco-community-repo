package org.alfresco.util;

import org.springframework.beans.factory.FactoryBean;

/**
 * A simple factory for glueing together multiple arguments as a string
 * 
 * @author dward
 */
public class StringAppendingFactoryBean implements FactoryBean
{

    /** The items. */
    private Object[] items;

    /**
     * Sets the items to be appended together.
     * 
     * @param items
     *            the items
     */
    public void setItems(Object[] items)
    {
        this.items = items;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.FactoryBean#getObject()
     */
    public Object getObject() throws Exception
    {
        if (this.items == null)
        {
            return "";
        }
        StringBuilder buff = new StringBuilder(1024);
        for (Object item : this.items)
        {
            buff.append(item);
        }
        return buff.toString();
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.FactoryBean#getObjectType()
     */
    public Class<?> getObjectType()
    {
        return String.class;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.FactoryBean#isSingleton()
     */
    public boolean isSingleton()
    {
        return true;
    }

}
