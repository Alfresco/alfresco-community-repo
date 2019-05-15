package org.alfresco.rest.core;

import org.alfresco.utility.TasProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:default.properties")
@PropertySource(value = "classpath:${environment}.properties", ignoreResourceNotFound = true)
public class RestProperties
{  
    @Autowired
    private TasProperties properties;

    public TasProperties envProperty()
    {
        return properties;
    }  
}