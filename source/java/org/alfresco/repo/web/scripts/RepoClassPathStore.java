/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.web.scripts;

import java.io.IOException;

import org.springframework.extensions.webscripts.ClassPathStore;

import freemarker.cache.TemplateLoader;

/**
 * Extension of the SpringSurf ClassPathStore to ensure that the examination of
 * last modified dates on classpath bound resources does not cause a performance
 * degredation in REST heavy client applications.
 * <p>
 * In the repository, due to the possibility of Repository bound resources, all
 * WebScript search path lists have the "delay" set to either zero seconds (no delay)
 * or something close to that. This means that the FreeMarker template cache is
 * always or often requesting the last modified date of a classpath resource - and
 * the resources do not change. Note that the /extension classpath store still uses
 * the original ClassPathStore. Otherwise all stores can be refreshed as usual via
 * the Refresh WebScripts command.
 * 
 * @author Kevin Roast
 */
public class RepoClassPathStore extends ClassPathStore
{
    @Override
    public TemplateLoader getTemplateLoader()
    {
        return new ClassPathTemplateLoader();
    }
    
    @Override
    public long lastModified(String documentPath)
        throws IOException
    {
        return -1L;
    }
    
    /**
     * Class Path Store implementation of a Template Loader
     * <p>
     * Retrieves templates either from classes in the class path or classes inside of JAR files
     * within the class path
     * <p>
     * This implementation always returns a fixed last modified date of -1.
     */
    private class ClassPathTemplateLoader extends ClassPathStore.ClassPathTemplateLoader
    {
        /**
         * @see freemarker.cache.TemplateLoader#getLastModified(java.lang.Object)
         */
        public long getLastModified(Object templateSource)
        {
            return -1L;
        }
    }
}
