/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.service.cmr.repository;

import java.io.Writer;
import java.util.Map;

import org.alfresco.service.Auditable;
import org.alfresco.service.PublicService;

/**
 * Template Service.
 * <p>
 * Provides an interface to services for executing template engine against a template file
 * and data model.
 * <p>
 * The service provides a configured list of available template engines. The template file
 * can either be in the repository (passed as NodeRef string) or on the classpath. Also a template
 * can be passed directly as a String using the processTemplateString() methods.
 * <p>
 * The data model is specified to the template engine. The FreeMarker template engine is used by default.
 * 
 * @author Kevin Roast
 */
public interface TemplateService
{
    /** Keys for default model values */
    public static final String KEY_IMAGE_RESOLVER = "imageresolver";
    public static final String KEY_COMPANY_HOME = "companyhome";
    public static final String KEY_USER_HOME = "userhome";
    public static final String KEY_PERSON = "person";
    public static final String KEY_TEMPLATE = "template";
    public static final String KEY_DATE = "date";
    public static final String KEY_SHARE_URL = "shareUrl";
    public static final String KEY_PRODUCT_NAME = "productName";
    
    /**
     * Process a template against the upplied data model and return the result as
     * a string.
     * 
     * The template engine used will be determined by the extension of the template.
     *      
     * @param template     Template (qualified classpath name or noderef)
     * @param model        Object model to process template against
     * 
     * @return output of the template process as a String
     * @throws TemplateException
     */
    @Auditable(parameters = {"template", "model"})
    public String processTemplate(String template, Object model)
        throws TemplateException;
    
    /**
     * Process a template against the supplied data model and write to the out.
     * 
     * The template engine used will be determined by the extension of the template.
     * 
     * @param template     Template (qualified classpath name or noderef)
     * @param model        Object model to process template against
     * @param out          Writer object to send output too
     */
    @Auditable(parameters = {"template", "model", "out"})
    public void processTemplate(String template, Object model, Writer out)
        throws TemplateException;
    
    /**
     * Process a template against the supplied data model and write to the out.
     * 
     * @param engine       Name of the template engine to use
     * @param template     Template (qualified classpath name or noderef)
     * @param model        Object model to process template against
     * 
     * @return output of the template process as a String
     */
    @Auditable(parameters = {"engine", "template", "model"})
    public String processTemplate(String engine, String template, Object model)
        throws TemplateException;
    
    /**
     * Process a template against the supplied data model and write to the out.
     * 
     * @param engine       Name of the template engine to use
     * @param template     Template (qualified classpath name or noderef)
     * @param model        Object model to process template against
     * @param out          Writer object to send output too
     */
    @Auditable(parameters = {"engine", "template", "model", "out"})
    public void processTemplate(String engine, String template, Object model, Writer out)
        throws TemplateException;
    
    /**
     * Process a given template, provided as a string, against the supplied data model and return the result as a String
     * 
     * @param engine       Name of the template engine to use
     * @param template     Template string
     * @param model        Object model to process template against
     * 
     * @return  output of the template process as a String
     *      
     * @throws TemplateException
     */
    @Auditable(parameters = {"engine", "template", "model"})
    public String processTemplateString(String engine, String template, Object model)
        throws TemplateException;
    
    /**
     * Process a given template, provided as a string, against the supplied data model and report the
     * result back in the provided writer.
     * 
     * @param engine       Name of the template engine to use
     * @param template     Template string
     * @param model        Object model to process template against
     * @param out          Writer object to send output too
     * 
     * @throws TemplateException
     */
    @Auditable(parameters = {"engine", "template", "model", "out"})
    public void processTemplateString(String engine, String template, Object model, Writer out)
        throws TemplateException;
        
    /**
     * Return a TemplateProcessor instance for the specified engine name.
     * Note that the processor instance is NOT thread safe!
     * 
     * @param engine       Name of the template engine to get or null for default
     * 
     * @return TemplateProcessor
     */
    @Auditable(parameters = {"engine"})
    public TemplateProcessor getTemplateProcessor(String engine);
    
    /**
     * Registers a new template processor with the template service
     * 
     * @param templateProcessor     the template processor to register
     */
    @Auditable(parameters = {"templateProcessor"})
    public void registerTemplateProcessor(TemplateProcessor templateProcessor);
    
    /**
     * Helper method to build a default model
     * 
     * @param person        the person node reference
     * @param companyHome   the company home node refereence
     * @param userHome      the user home node reference
     * @param template      the node ref for the template (optional)
     * @param imageResolver the image resolver (optional)
     * @return
     */
    @Auditable(parameters = {"person", "companyHome", "userHome", "template", "imageResolver"})
    public Map<String, Object> buildDefaultModel(
            NodeRef person, 
            NodeRef companyHome, 
            NodeRef userHome,
            NodeRef template,
            TemplateImageResolver imageResolver);
}
