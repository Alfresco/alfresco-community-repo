package org.alfresco.service.cmr.repository;

import org.alfresco.processor.ProcessorExtension;

/**
 * Interface to represent a server side template extension implementation
 * 
 * @author Kevin Roast
 */
public interface TemplateProcessorExtension extends ProcessorExtension
{   
    /**
     * Set the template image resolver for this extension
     * 
     * @param resolver      TemplateImageResolver
     */
    void setTemplateImageResolver(TemplateImageResolver resolver);
    
    /**
     * Get the template image resolver for this extension
     * 
     * @return TemplateImageResolver
     */
    TemplateImageResolver getTemplateImageResolver();
}
