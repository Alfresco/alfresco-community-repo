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
package org.alfresco.repo.template;

import java.io.IOException;
import java.io.Writer;

import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TemplateException;
import org.alfresco.service.cmr.repository.TemplateProcessor;
import org.alfresco.util.ISO9075;
import org.apache.log4j.Logger;

import freemarker.cache.MruCacheStorage;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

/**
 * FreeMarker implementation the template processor interface
 * 
 * @author Kevin Roast
 */
public class FreeMarkerProcessor implements TemplateProcessor
{
    private final static String MSG_ERROR_NO_TEMPLATE   = "error_no_template";
    private final static String MSG_ERROR_TEMPLATE_FAIL = "error_template_fail";
    private final static String MSG_ERROR_TEMPLATE_IO   = "error_template_io";
    
    private static Logger logger = Logger.getLogger(FreeMarkerProcessor.class);
    
    /** FreeMarker processor configuration */
    private Configuration config = null;
    
    /** The permission-safe node service */
    private NodeService nodeService;
    
    /** The Content Service to use */
    private ContentService contentService;
    
    /**
     * Set the node service
     * 
     * @param nodeService       The permission-safe node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * Set the content service
     * 
     * @param contentService    The ContentService to use
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }
    
    /**
     * @return The FreeMarker config instance for this processor
     */
    private Configuration getConfig()
    {
        if (this.config == null)
        {
            Configuration config = new Configuration();
            
            // setup template cache
            config.setCacheStorage(new MruCacheStorage(20, 0));
            
            // use our custom loader to find templates on the ClassPath
            config.setTemplateLoader(new ClassPathRepoTemplateLoader(nodeService, contentService));
            
            // use our custom object wrapper that can deal with QNameMap objects directly
            config.setObjectWrapper(new QNameAwareObjectWrapper());
            
            // rethrow any exception so we can deal with them
            config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            
            this.config = config;
        }
        return this.config;
    }
    
    private Configuration getStringConfig(String path, String template)
    {
        
            Configuration config = new Configuration();
            
            // setup template cache
            config.setCacheStorage(new MruCacheStorage(20, 0));
            
            // use our custom loader to find templates on the ClassPath
            StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
            stringTemplateLoader.putTemplate(path, template);
            config.setTemplateLoader(stringTemplateLoader);
            
            // use our custom object wrapper that can deal with QNameMap objects directly
            config.setObjectWrapper(new QNameAwareObjectWrapper());
            
            // rethrow any exception so we can deal with them
            config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            
            return config;
    }
    
    /**
     * @see org.alfresco.service.cmr.repository.TemplateProcessor#process(java.lang.String, java.lang.Object, java.io.Writer)
     */
    public void process(String template, Object model, Writer out)
    {
        if (template == null || template.length() == 0)
        {
            throw new IllegalArgumentException("Template name is mandatory.");
        }
        if (model == null)
        {
            throw new IllegalArgumentException("Model is mandatory.");
        }
        if (out == null)
        {
            throw new IllegalArgumentException("Output Writer is mandatory.");
        }
        
        try
        {
            if (logger.isDebugEnabled())
                logger.debug("Executing template: " + template + " on model: " + model);
            
            Template t = getConfig().getTemplate(template);
            if (t != null)
            {
                try
                {
                    // perform the template processing against supplied data model
                    t.process(model, out);
                }
                catch (Throwable err)
                {
                    throw new TemplateException(MSG_ERROR_TEMPLATE_FAIL, new Object[] {err.getMessage()}, err);
                }
            }
            else
            {
                throw new TemplateException(MSG_ERROR_NO_TEMPLATE, new Object[] {template});
            }
        }
        catch (IOException ioerr)
        {
            throw new TemplateException(MSG_ERROR_TEMPLATE_IO, new Object[] {template}, ioerr);
        }
    }
    
    private static final String PATH = "string://fixed";
    
    public void processString(String template, Object model, Writer out)
    {
        if (template == null || template.length() == 0)
        {
            throw new IllegalArgumentException("Template is mandatory.");
        }
        if (model == null)
        {
            throw new IllegalArgumentException("Model is mandatory.");
        }
        if (out == null)
        {
            throw new IllegalArgumentException("Output Writer is mandatory.");
        }
        
        try
        {
            if (logger.isDebugEnabled())
                logger.debug("Executing template: " + template + " on model: " + model);
            
            Template t = getStringConfig(PATH, template).getTemplate(PATH);
            if (t != null)
            {
                try
                {
                    // perform the template processing against supplied data model
                    t.process(model, out);
                }
                catch (Throwable err)
                {
                    throw new TemplateException(MSG_ERROR_TEMPLATE_FAIL, new Object[] {err.getMessage()}, err);
                }
            }
            else
            {
                throw new TemplateException(MSG_ERROR_NO_TEMPLATE, new Object[] {template});
            }
        }
        catch (IOException ioerr)
        {
            throw new TemplateException(MSG_ERROR_TEMPLATE_IO, new Object[] {template}, ioerr);
        }
    }
}
