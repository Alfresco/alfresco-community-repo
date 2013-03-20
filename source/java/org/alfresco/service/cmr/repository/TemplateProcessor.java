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
package org.alfresco.service.cmr.repository;

import java.io.Writer;
import java.util.Locale;

import org.alfresco.processor.Processor;

/**
 * Interface to be implemented by template engine wrapper classes. The developer is responsible
 * for interfacing to an appropriate template engine, using the supplied data model as input to
 * the template and directing the output to the Writer stream. 
 * 
 * @author Kevin Roast
 */
public interface TemplateProcessor extends Processor
{   
    /**
     * Process a template against the supplied data model and write to the out.
     * 
     * @param template       Template name/path
     * @param model          Object model to process template against
     * @param out            Writer object to send output too
     */
    public void process(String template, Object model, Writer out);
    
    /**
     * Process a template in the given locale against the supplied data model and write to the out.
     * 
     * @param template       Template name/path
     * @param model          Object model to process template against
     * @param out            Writer object to send output too
     * @param locale		 The Locale to process the template in
     */
    public void process(String template, Object model, Writer out, Locale locale);

    /**
     * Process a string template against the supplied data model and write to the out.
     * 
     * @param template       Template string
     * @param model          Object model to process template against
     * @param out            Writer object to send output too
     */
    public void processString(String template, Object model, Writer out);
}
