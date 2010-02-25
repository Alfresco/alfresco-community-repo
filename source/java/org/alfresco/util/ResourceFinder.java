/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
