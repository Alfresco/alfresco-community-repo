/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.extensions.config.Config;
import org.springframework.extensions.config.ConfigElement;
import org.springframework.extensions.config.ConfigImpl;

/**
 * Extension of {@link ConfigImpl} that protects all internal data when locked
 * 
 * @author Derek Hulley
 * @since 4.1.5
 */
public class ImmutableConfig extends ConfigImpl
{
    private final Map<String, ConfigElement> configElements;
    
    /**
     * Make a read-only copy of the given configuration
     * 
     * @param config            the configuration to copy
     */
    public ImmutableConfig(Config config)
    {
        if (config.getConfigElements() == null)
        {
            this.configElements = Collections.emptyMap();
        }
        else
        {
            Map<String, ConfigElement> configElements = new HashMap<String, ConfigElement>(config.getConfigElements());
            this.configElements = Collections.unmodifiableMap(configElements);
        }
    }
    
    @Override
    public ConfigElement getConfigElement(String name)
    {
        return configElements.get(name);
    }

    @Override
    public String getConfigElementValue(String name)
    {
        ConfigElement ce = configElements.get(name);
        return ce != null ? ce.getValue() : null; 
    }

    @Override
    public boolean hasConfigElement(String name)
    {
        return configElements.containsKey(name);    }

    @Override
    public Map<String, ConfigElement> getConfigElements()
    {
        return configElements;
    }

    @Override
    public void putConfigElement(ConfigElement configElement)
    {
        throw new UnsupportedOperationException("Configuration is immutable.");
    }
}
