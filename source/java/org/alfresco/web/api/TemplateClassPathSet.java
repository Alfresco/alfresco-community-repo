/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.web.api;

import java.io.File;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import freemarker.cache.FileTemplateLoader;


/**
 * A set of template class paths
 *  
 * @author davidc
 */
public class TemplateClassPathSet implements InitializingBean
{
    // Logger
    private static final Log logger = LogFactory.getLog(TemplateClassPathSet.class);

    private Set<String> classPaths;
    private APITemplateProcessor templateProcessor;
    private ResourceLoader resourceLoader;

    /**
     * Construct
     */
    public TemplateClassPathSet()
    {
        resourceLoader = new DefaultResourceLoader();
    }

    /**
     * Sets the Template Processor
     * 
     * @param templateProcessor
     */
    public void setTemplateProcessor(APITemplateProcessor templateProcessor)
    {
        this.templateProcessor = templateProcessor;
    }
    
    /**
     * Sets the paths
     * 
     * @param classPaths
     */
    public void setPaths(Set<String> classPaths)
    {
        this.classPaths = classPaths;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception
    {
        // Add class paths to template processor
        for (String classPath : classPaths)
        {
            Resource resource = resourceLoader.getResource(classPath);
            if (resource.exists())
            {
                File file = resource.getFile();
                templateProcessor.addTemplateLoader(new FileTemplateLoader(file));

                if (logger.isDebugEnabled())
                    logger.debug("Registered template classpath '" + classPath);
            }
            else if (logger.isWarnEnabled())
            {
                logger.warn("Template classpath '" + classPath + "' does not exist");
            }
        }
    }
    
}
