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
package org.alfresco.service.cmr.repository;

import java.io.Writer;

/**
 * Template Service.
 * <p>
 * Provides an interface to services for executing template engine against a template file
 * and data model.
 * <p>
 * The service provides a configured list of available template engines. The template file
 * can either be in the repository (passed as NodeRef string) or on the classpath. The data
 * model is specified to the template engine. The FreeMarker template engine is used by default.
 * 
 * @author Kevin Roast
 */
public interface TemplateService
{
    /**
     * Process a template against the supplied data model and write to the out.
     * 
     * @param engine       Name of the template engine to use
     * @param template     Template (qualified classpath name or noderef)
     * @param model        Object model to process template against
     * 
     * @return output of the template process as a String
     */
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
    public TemplateProcessor getTemplateProcessor(String engine);
}
