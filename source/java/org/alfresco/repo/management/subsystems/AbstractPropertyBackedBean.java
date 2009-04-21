/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.management.subsystems;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * A base class for {@link PropertyBackedBean}s. Gets its category from its Spring bean name and automatically destroys
 * itself on server shutdown. Communicates its creation and destruction to a {@link PropertyBackedBeanRegistry}.
 * 
 * @author dward
 */
public abstract class AbstractPropertyBackedBean implements PropertyBackedBean, InitializingBean, DisposableBean,
        BeanNameAware
{

    /** The default ID. */
    protected static final String DEFAULT_ID = "default";

    /** The registry. */
    private PropertyBackedBeanRegistry registry;

    /** The id. */
    private String id = DEFAULT_ID;

    /** The category. */
    private String category;

    /**
     * Sets the registry.
     * 
     * @param registry
     *            the registry to set
     */
    public void setRegistry(PropertyBackedBeanRegistry registry)
    {
        this.registry = registry;
    }

    /**
     * Gets the registry.
     * 
     * @return the registry
     */
    public PropertyBackedBeanRegistry getRegistry()
    {
        return registry;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
     */
    public void setBeanName(String name)
    {
        this.category = name;
    }

    /**
     * Sets the id.
     * 
     * @param id
     *            the id to set
     */
    public void setId(String id)
    {
        this.id = id;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception
    {
        this.registry.register(this);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.management.SelfDescribingBean#getId()
     */
    public String getId()
    {
        return this.id;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.management.subsystems.PropertyBackedBean#getCategory()
     */
    public String getCategory()
    {
        return this.category;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.DisposableBean#destroy()
     */
    public void destroy()
    {
        destroy(false);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.management.subsystems.PropertyBackedBean#destroy(boolean)
     */
    public void destroy(boolean isPermanent)
    {
        stop();
        this.registry.deregister(this, isPermanent);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.management.subsystems.PropertyBackedBean#isUpdateable(java.lang.String)
     */
    public boolean isUpdateable(String name)
    {
        return true;
    }
}
