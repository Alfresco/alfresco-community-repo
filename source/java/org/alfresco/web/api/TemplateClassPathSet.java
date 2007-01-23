/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
