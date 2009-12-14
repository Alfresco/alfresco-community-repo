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
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.repo.audit.extractor;

import org.springframework.extensions.surf.util.PropertyCheck;
import org.alfresco.util.registry.NamedObjectRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;

/**
 * Abstract implementation to provide support.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public abstract class AbstractDataExtractor implements DataExtractor, InitializingBean, BeanNameAware
{
    /** Logger that can be used by subclasses */
    protected final Log logger = LogFactory.getLog(getClass());
    
    private String name;
    private NamedObjectRegistry<DataExtractor> registry;

    /**
     * Set the name with which to {@link #setRegistry(NamedObjectRegistry) register}
     * @param name          the name of the bean
     */
    public void setBeanName(String name)
    {
        this.name = name;
    }

    /**
     * Set the registry with which to register
     * @param registry
     */
    public void setRegistry(NamedObjectRegistry<DataExtractor> registry)
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
     * This implementation assumes all extractors are stateless i.e. if the class matches
     * then the instances are equal.
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
