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
package org.alfresco.repo.audit.generator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.registry.NamedObjectRegistry;

/**
 * Abstract implementation to provide support.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
@AlfrescoPublicApi
public abstract class AbstractDataGenerator implements DataGenerator, InitializingBean, BeanNameAware
{
    /** Logger that can be used by subclasses */
    protected final Log logger = LogFactory.getLog(getClass());

    private String name;
    private NamedObjectRegistry<DataGenerator> registry;

    /**
     * Set the name with which to {@link #setRegistry(NamedObjectRegistry) register}
     * 
     * @param name
     *            the name of the bean
     */
    public void setBeanName(String name)
    {
        this.name = name;
    }

    /**
     * Set the registry with which to register
     */
    public void setRegistry(NamedObjectRegistry<DataGenerator> registry)
    {
        this.registry = registry;
    }

    /**
     * Registers the instance
     */
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "name", name);
        PropertyCheck.mandatory(this, "registry", registry);

        registry.register(name, this);
    }

    /**
     * This implementation assumes all generators are stateless i.e. if the class matches then the instances are equal.
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj != null && obj.getClass().equals(this.getClass()))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
