package org.alfresco.repo.template;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.repo.processor.BaseProcessorExtension;
import org.alfresco.service.cmr.repository.TemplateProcessorExtension;
import org.alfresco.service.cmr.repository.TemplateImageResolver;

/**
 * Abstract base class for a template extension implementation
 * 
 * @author Kevin Roast
 */
@AlfrescoPublicApi
public abstract class BaseTemplateProcessorExtension extends BaseProcessorExtension implements TemplateProcessorExtension
{   
    /** The TemplateImageResolver for the current template execution thread */
    private ThreadLocal<TemplateImageResolver> resolver = new ThreadLocal<TemplateImageResolver>();
    
    /**
     * @see org.alfresco.service.cmr.repository.TemplateProcessorExtension#setTemplateImageResolver(org.alfresco.service.cmr.repository.TemplateImageResolver)
     */
    public void setTemplateImageResolver(TemplateImageResolver resolver)
    {
        this.resolver.set(resolver);
    }
    
    /**
     * @see org.alfresco.service.cmr.repository.TemplateProcessorExtension#getTemplateImageResolver()
     */
    public TemplateImageResolver getTemplateImageResolver()
    {
        return this.resolver.get();
    }
}
