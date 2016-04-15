package org.alfresco.repo.web.scripts;

import org.springframework.extensions.webscripts.TemplateProcessor;
import org.springframework.extensions.webscripts.TemplateProcessorFactory;

/**
 * @author Kevin Roast
 */
public class RepositoryTemplateProcessorFactory implements TemplateProcessorFactory
{
    private TemplateProcessor templateProcessor;
    
    
    /**
     * @param templateProcessor     the TemplateProcessor to set
     */
    public void setTemplateProcessor(TemplateProcessor templateProcessor)
    {
        this.templateProcessor = templateProcessor;
    }

    /* (non-Javadoc)
     * @see org.springframework.extensions.webscripts.TemplateProcessorFactory#newInstance()
     */
    public TemplateProcessor newInstance()
    {
        return templateProcessor;
    }
}