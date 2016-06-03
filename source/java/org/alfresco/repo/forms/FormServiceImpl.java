package org.alfresco.repo.forms;

import java.util.List;
import java.util.Map;

import org.alfresco.repo.forms.processor.FormProcessor;
import org.alfresco.repo.forms.processor.FormProcessorRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Form Service Implementation.
 * 
 * @author Gavin Cornwell
 */
public class FormServiceImpl implements FormService
{
    /** Logger */
    private static Log logger = LogFactory.getLog(FormServiceImpl.class);
    
    /** Services */
    private FormProcessorRegistry processorRegistry;
    
    /**
     * Sets the FormProcessorRegistry
     * 
     * @param registry The FormProcessorRegistry instance to use
     */
    public void setProcessorRegistry(FormProcessorRegistry registry)
    {
        this.processorRegistry = registry;
    }

    /*
     * @see org.alfresco.repo.forms.FormService#getForm(org.alfresco.repo.forms.Item)
     */
    public Form getForm(Item item)
    {
        return getForm(item, null, null, null);
    }
    
    /*
     * @see org.alfresco.repo.forms.FormService#getForm(org.alfresco.repo.forms.Item, java.util.Map)
     */
    public Form getForm(Item item, Map<String, Object> context)
    {
        return getForm(item, null, null, context);
    }

    /*
     * @see org.alfresco.repo.forms.FormService#getForm(org.alfresco.repo.forms.Item, java.util.List)
     */
    public Form getForm(Item item, List<String> fields)
    {
        return getForm(item, fields, null, null);
    }
    
    /*
     * @see org.alfresco.repo.forms.FormService#getForm(org.alfresco.repo.forms.Item, java.util.List, java.util.Map)
     */
    public Form getForm(Item item, List<String> fields, Map<String, Object> context)
    {
        return getForm(item, fields, null, context);
    }

    /*
     * @see org.alfresco.repo.forms.FormService#getForm(org.alfresco.repo.forms.Item, java.util.List, java.util.List)
     */
    public Form getForm(Item item, List<String> fields, List<String> forcedFields)
    {
        return getForm(item, fields, forcedFields, null);
    }
    
    /*
     * @see org.alfresco.repo.forms.FormService#getForm(org.alfresco.repo.forms.Item, java.util.List, java.util.List, java.util.Map)
     */
    public Form getForm(Item item, List<String> fields, List<String> forcedFields, Map<String, Object> context)
    {
        if (this.processorRegistry == null)
        {
            throw new FormException("Property 'processorRegistry' has not been set.");
        }
        
        if (logger.isDebugEnabled())
            logger.debug("Retrieving form for item: " + item);
        
        FormProcessor processor = this.processorRegistry.getApplicableFormProcessor(item);
        
        if (processor == null)
        {
            throw new FormException("Failed to find appropriate FormProcessor to generate Form for item: " + item);
        }
        else
        {
            return processor.generate(item, fields, forcedFields, context);
        }
    }
    
    /*
     * @see org.alfresco.repo.forms.FormService#saveForm(org.alfresco.repo.forms.Item, org.alfresco.repo.forms.FormData)
     */
    public Object saveForm(Item item, FormData data)
    {
        if (this.processorRegistry == null)
        {
            throw new FormException("FormProcessorRegistry has not been setup");
        }
        
        if (logger.isDebugEnabled())
            logger.debug("Saving form for item '" + item + "': " + data);
        
        FormProcessor processor = this.processorRegistry.getApplicableFormProcessor(item);
        
        if (processor == null)
        {
            throw new FormException("Failed to find appropriate FormProcessor to persist Form for item: " + item);
        }
        else
        {
            return processor.persist(item, data);
        }
    }
}
