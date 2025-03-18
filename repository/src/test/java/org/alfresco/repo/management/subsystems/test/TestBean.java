/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.management.subsystems.test;

import org.springframework.beans.factory.BeanNameAware;

import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;

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

    /* (non-Javadoc)
     * 
     * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String) */
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
