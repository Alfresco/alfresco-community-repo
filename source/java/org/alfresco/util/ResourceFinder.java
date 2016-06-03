package org.alfresco.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.context.support.ServletContextResourcePatternResolver;

/**
 * Can be used in Spring configuration to search for all resources matching an array of patterns.
 * 
 * @author dward
 */
public class ResourceFinder extends ServletContextResourcePatternResolver
{
    public ResourceFinder()
    {
        super(new DefaultResourceLoader());
    }

    /**
     * The Constructor.
     * 
     * @param resourceLoader
     *            the resource loader
     */
    public ResourceFinder(ResourceLoader resourceLoader)
    {
        super(resourceLoader);
    }

    /**
     * Gets an array of resources matching the given location patterns.
     * 
     * @param locationPatterns
     *            the location patterns
     * @return the matching resources, ordered by locationPattern index and location in the classpath
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public Resource[] getResources(String... locationPatterns) throws IOException
    {
        List<Resource> resources = new LinkedList<Resource>();
        for (String locationPattern : locationPatterns)
        {
            resources.addAll(Arrays.asList(getResources(locationPattern)));
        }
        Resource[] resourceArray = new Resource[resources.size()];
        resources.toArray(resourceArray);
        return resourceArray;
    }
}
