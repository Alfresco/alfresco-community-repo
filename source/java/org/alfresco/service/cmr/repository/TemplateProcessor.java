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
