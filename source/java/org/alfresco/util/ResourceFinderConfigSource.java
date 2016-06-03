package org.alfresco.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.extensions.config.ConfigDeployment;
import org.springframework.extensions.config.ConfigSource;
import org.springframework.extensions.config.source.UrlConfigSource;

/**
 * A Spring {@link ConfigSource} that is powered by a {@link ResourceFinder}.
 * This allows for the loading of resources with wildcards, which 
 *  {@link UrlConfigSource} does not.
 * 
 * @author Nick Burch
 * @since 4.0.1
 */
public class ResourceFinderConfigSource implements ConfigSource
{
    private static Log logger = LogFactory.getLog(ResourceFinderConfigSource.class);
    
    /** The ResourceFinder to look up with */
    private ResourceFinder resourceFinder;
    /** The Resource Paths to search */
    private List<String> locations;
    
    public void setResourceFinder(ResourceFinder resourceFinder)
    {
        this.resourceFinder = resourceFinder;
    }
    
    /**
     * Sets the locations, of the form classpath*:/some/path.xml
     */
    public void setLocations(List<String> locations)
    {
        this.locations = locations;
    }

    @Override
    public List<ConfigDeployment> getConfigDeployments()
    {
        List<ConfigDeployment> configs = new ArrayList<ConfigDeployment>();
        
        String[] locs = locations.toArray(new String[locations.size()]);
        
        Resource[] resources;
        try
        {
            resources = resourceFinder.getResources(locs);
        }
        catch(IOException e)
        {
            throw new AlfrescoRuntimeException("Unable to find resources", e);
        }
        
        for (Resource resource : resources)
        {
            try
            {
                configs.add(new ConfigDeployment(resource.getDescription(), resource.getInputStream()));
                
                if (logger.isDebugEnabled())
                {
                    logger.debug("Loaded resource " + resource);
                }
            }
            catch(IOException e)
            {
                logger.warn("Skipping unreadable resource " + resource, e);
            }
        }
        
        return configs;
    }
}
