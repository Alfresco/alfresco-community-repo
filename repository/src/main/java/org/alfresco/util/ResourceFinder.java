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
