package org.alfresco.cmis;

import org.alfresco.utility.TasAisProperties;
import org.alfresco.utility.TasProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@PropertySource("classpath:default.properties")
@PropertySource(value = "classpath:${environment}.properties", ignoreResourceNotFound = true)
public class CmisProperties
{
    @Autowired
    private TasProperties properties;

    @Autowired
    private TasAisProperties aisProperties;

    public TasProperties envProperty()
    {
        return properties;
    }

    public TasAisProperties aisProperty()
    {
        return aisProperties;
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer()
    {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Value("${cmis.binding}")
    private String cmisBinding;

    @Value("${cmis.basePath}")
    private String basePath;

    public String getCmisBinding()
    {
        return cmisBinding;
    }

    public String getBasePath()
    {
        return basePath;
    }

    public void setBasePath(String basePath)
    {
        this.basePath = basePath;
    }

    public void setCmisBinding(String cmisBinding)
    {
        this.cmisBinding = cmisBinding;
    }
}
