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
