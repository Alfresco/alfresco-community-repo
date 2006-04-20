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

import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.TemplateException;
import org.alfresco.service.cmr.repository.TemplateProcessor;
import org.alfresco.service.cmr.repository.TemplateService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author Kevin Roast
 */
public class TemplateServiceImpl implements TemplateService, ApplicationContextAware
{
    private static Log logger = LogFactory.getLog(TemplateService.class);
    
    /** Spring ApplicationContext for bean lookup by ID */
    private ApplicationContext applicationContext;
    
    /** Default Template processor engine to use */
    private String defaultTemplateEngine;
    
    /** Available template engine names to impl class names */
    private Map<String, String> templateEngines;
    
    /** Threadlocal instance for template processor cache */
    private static ThreadLocal<Map<String, TemplateProcessor>> processors = new ThreadLocal<Map<String, TemplateProcessor>>();
    
    /**
     * Set the application context
     * 
     * @param applicationContext    the application context
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;
    }
    
    /**
     * @param defaultTemplateEngine The default Template Engine name to set.
     */
    public void setDefaultTemplateEngine(String defaultTemplateEngine)
    {
        this.defaultTemplateEngine = defaultTemplateEngine;
    }

    /**
     * @param templateEngines       The Map of template engine name to impl class name to set.
     */
    public void setTemplateEngines(Map<String, String> templateEngines)
    {
        this.templateEngines = templateEngines;
    }

    /**
     * @see org.alfresco.service.cmr.repository.TemplateService#getTemplateProcessor(java.lang.String)
     */
    public TemplateProcessor getTemplateProcessor(String engine)
    {
        try
        {
            return getTemplateProcessorImpl(engine);
        }
        catch (Throwable err)
        {
            if (logger.isDebugEnabled())
                logger.debug("Unable to load template processor.", err);
            
            return null;
        }
    }

    /**
     * @see org.alfresco.service.cmr.repository.TemplateService#processTemplate(java.lang.String, java.lang.String, java.lang.Object, java.io.Writer)
     */
    public void processTemplate(String engine, String template, Object model, Writer out)
        throws TemplateException
    {
        try
        {
           // execute template processor
           TemplateProcessor processor = getTemplateProcessorImpl(engine);
           processor.process(template, model, out);
        }
        catch (TemplateException terr)
        {
           throw terr;
        }
        catch (Throwable err)
        {
           throw new TemplateException(err.getMessage(), err);
        }
    }

    /**
     * @see org.alfresco.service.cmr.repository.TemplateService#processTemplate(java.lang.String, java.lang.String, java.lang.Object)
     */
    public String processTemplate(String engine, String template, Object model)
        throws TemplateException
    {
        Writer out = new StringWriter(1024);
        processTemplate(engine, template, model, out);
        return out.toString();
    }

    public void processTemplateString(String engine, String template, Object model, Writer out)
    throws TemplateException
    {
        try
        {
            // execute template processor
            TemplateProcessor processor = getTemplateProcessorImpl(engine);
            processor.processString(template, model, out);
        }
        catch (TemplateException terr)
        {
            throw terr;
        }
        catch (Throwable err)
        {
            throw new TemplateException(err.getMessage(), err);
        }
    }
    
    
    public String processTemplateString(String engine, String template, Object model)
    throws TemplateException
    {
        Writer out = new StringWriter(1024);
        processTemplateString(engine, template, model, out);
        return out.toString();
    }

    
    
    /**
     * Return the TemplateProcessor implementation for the named template engine
     * 
     * @param name      Template Engine name
     * 
     * @return TemplateProcessor
     */
    private TemplateProcessor getTemplateProcessorImpl(String name)
    {
        // use the ThreadLocal map to find the processors instance
        // create the cache map for this thread if required 
        Map<String, TemplateProcessor> procMap = processors.get();
        if (procMap == null)
        {
            procMap = new HashMap<String, TemplateProcessor>(7, 1.0f);
            processors.set(procMap);
        }
        
        if (name == null)
        {
            name = defaultTemplateEngine;
        }
        
        // find the impl for the named processor
        TemplateProcessor processor = procMap.get(name);
        if (processor == null)
        {
            String className = templateEngines.get(name);
            if (className == null)
            {
                throw new AlfrescoRuntimeException("Unable to find configured ClassName for template engine: " + name);
            }
            try
            {
                Object obj;
                try
                {
                    obj = this.applicationContext.getBean(className);
                }
                catch (BeansException err)
                {
                    // instantiate the processor class directory if not a Spring bean
                    obj = Class.forName(className).newInstance();
                }
                
                if (obj instanceof TemplateProcessor)
                {
                    processor = (TemplateProcessor)obj;
                }
                else
                {
                    throw new AlfrescoRuntimeException("Supplied template processors does not implement TemplateProcessor: " + className);
                }
            }
            catch (ClassNotFoundException err1)
            {
                // if the bean is not a classname, then it may be a spring bean Id
                throw new AlfrescoRuntimeException("Unable to load class for supplied template processors: " + className, err1);
            }
            catch (IllegalAccessException err2)
            {
                throw new AlfrescoRuntimeException("Unable to load class for supplied template processors: " + className, err2);
            }
            catch (InstantiationException err3)
            {
                throw new AlfrescoRuntimeException("Unable to instantiate class for supplied template processors: " + className, err3);
            }
            
            // cache for later
            procMap.put(name, processor);
        }
        
        return processor;
    }
}
