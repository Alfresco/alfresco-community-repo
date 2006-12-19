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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TemplateException;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.service.cmr.repository.TemplateNode;
import org.alfresco.service.cmr.repository.TemplateProcessor;
import org.apache.log4j.Logger;

import freemarker.cache.MruCacheStorage;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

/**
 * FreeMarker implementation of the template processor interface.
 * <p>
 * Service to process FreeMarker template files loaded from various sources including
 * the classpath, repository and directly from a String.
 * <p>
 * The template is processed against a data model generally consisting of a map of
 * named objects. FreeMarker can natively handle any POJO objects using standard bean
 * notation syntax. It has support for walking List objects. A 'standard' data model
 * helper is provided to help generate an object model containing well known objects
 * such as the Company Home, User Home and current User nodes. It also provides helpful
 * util classes to process Date objects and repository specific custom methods. 
 * 
 * @author Kevin Roast
 */
public class FreeMarkerProcessor implements TemplateProcessor
{
    private final static String MSG_ERROR_NO_TEMPLATE   = "error_no_template";
    private final static String MSG_ERROR_TEMPLATE_FAIL = "error_template_fail";
    private final static String MSG_ERROR_TEMPLATE_IO   = "error_template_io";
    
    private static final Logger logger = Logger.getLogger(FreeMarkerProcessor.class);
    
    /** Pseudo path to String based template */
    private static final String PATH = "string://fixed";
    
    /** The permission-safe node service */
    private NodeService nodeService;
    
    /** The Content Service to use */
    private ContentService contentService;
    
    /** Template encoding */
    private String defaultEncoding;
    
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
     * Set the default template encoding
     * 
     * @param defaultEncoding  the default encoding 
     */
    public void setDefaultEncoding(String defaultEncoding)
    {
        this.defaultEncoding = defaultEncoding;
    }
    
    /**
     * Get the FreeMarker configuration for this instance
     * 
     * @return FreeMarker configuration
     */
    private Configuration getConfig()
    {
        Configuration config = new Configuration();
        
        // setup template cache
        config.setCacheStorage(new MruCacheStorage(2, 0));
        
        // use our custom loader to find templates on the ClassPath
        config.setTemplateLoader(new ClassPathRepoTemplateLoader(nodeService, contentService));
        
        // use our custom object wrapper that can deal with QNameMap objects directly
        config.setObjectWrapper(new QNameAwareObjectWrapper());
        
        // rethrow any exception so we can deal with them
        config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        
        // set default template encoding
        if (defaultEncoding != null)
        {
            config.setDefaultEncoding(defaultEncoding);
        }
        
        return config;
    }
    
    /**
     * FreeMarker configuration for loading the specified template directly from a String
     * 
     * @param path      Pseudo Path to the template
     * @param template  Template content
     * 
     * @return FreeMarker configuration
     */
    private Configuration getStringConfig(String path, String template)
    {
        Configuration config = new Configuration();
        
        // setup template cache
        config.setCacheStorage(new MruCacheStorage(2, 0));
        
        // use our custom loader to load a template directly from a String
        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
        stringTemplateLoader.putTemplate(path, template);
        config.setTemplateLoader(stringTemplateLoader);
        
        // use our custom object wrapper that can deal with QNameMap objects directly
        config.setObjectWrapper(new QNameAwareObjectWrapper());
        
        // rethrow any exception so we can deal with them
        config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        
        // set default template encoding
        if (defaultEncoding != null)
        {
            config.setDefaultEncoding(defaultEncoding);
        }
        
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
            long startTime = 0;
            if (logger.isDebugEnabled())
            {
                logger.debug("Executing template: " + template);// + " on model: " + model);
                startTime = System.currentTimeMillis();
            }
            
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
            
            if (logger.isDebugEnabled())
            {
                long endTime = System.currentTimeMillis();
                logger.debug("Time to execute template: " + (endTime - startTime) + "ms");
            }
        }
        catch (IOException ioerr)
        {
            throw new TemplateException(MSG_ERROR_TEMPLATE_IO, new Object[] {template}, ioerr);
        }
    }
    
    /**
     * @see org.alfresco.service.cmr.repository.TemplateProcessor#processString(java.lang.String, java.lang.Object, java.io.Writer)
     */
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
            long startTime = 0;
            if (logger.isDebugEnabled())
            {
                logger.debug("Executing template: " + template);// + " on model: " + model);
                startTime = System.currentTimeMillis();
            }
            
            Template t = getStringConfig(PATH, template).getTemplate(PATH);
            if (t != null)
            {
                try
                {
                    // perform the template processing against supplied data model
                    t.process(model, out);
                    
                    if (logger.isDebugEnabled())
                    {
                        long endTime = System.currentTimeMillis();
                        logger.debug("Time to execute template: " + (endTime - startTime) + "ms");
                    }
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
    
    /**
     * Create the default data-model available to templates as global objects.
     * <p>
     * 'companyhome' - the Company Home node<br>
     * 'userhome' - the current user home space node<br>
     * 'person' - the node representing the current user Person<br>
     * 'template' - the node representing the template itself (may not be available)
     * <p>
     * Also adds various helper util objects and methods.
     * 
     * @param services      ServiceRegistry
     * @param person        The current user Person Node
     * @param companyHome   The CompanyHome ref
     * @param userHome      The User home space ref
     * @param template      Optional ref to the template itself
     * @param resolver      Image resolver to resolve icon images etc.
     * 
     * @return A Map of Templatable Node objects and util objects.
     */
    public static Map<String, Object> buildDefaultModel(
            ServiceRegistry services,
            NodeRef person, NodeRef companyHome, NodeRef userHome, NodeRef template,
            TemplateImageResolver imageResolver)
    {
        Map<String, Object> model = new HashMap<String, Object>(16, 1.0f);
        
        // supply the Company Home space as "companyhome"
        model.put("companyhome", new TemplateNode(companyHome, services));
        
        // supply the users Home Space as "userhome"
        model.put("userhome", new TemplateNode(userHome, services));
        
        // supply the current user Node as "person"
        model.put("person", new TemplateNode(person, services));
        
        // add the template itself as "template" if it comes from content on a node
        if (template != null)
        {
            model.put("template", new TemplateNode(template, services));
        }
        
        // current date/time is useful to have and isn't supplied by FreeMarker by default
        model.put("date", new Date());
        
        // Session support
        model.put("session", new Session(services, imageResolver));
        
        // Classification support
        
        model.put("classification", new Classification(companyHome.getStoreRef(), services, imageResolver));
        
        // add custom method objects
        model.put("hasAspect", new HasAspectMethod());
        model.put("message", new I18NMessageMethod());
        model.put("dateCompare", new DateCompareMethod());
        model.put("incrementDate", new DateIncrementMethod());
        
        return model;
    }
}
