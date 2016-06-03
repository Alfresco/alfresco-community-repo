
package org.alfresco.repo.forms.processor.node;

import org.alfresco.repo.forms.processor.FieldProcessorRegistry;

/**
 * FieldProcessorRegistry that exclusively handles content model based field processors.
 * 
 * @since 3.4
 * @author Nick Smith
 */
public class ContentModelFieldProcessorRegistry extends FieldProcessorRegistry
{
    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.FieldProcessorRegistry#getKey(java.lang.String)
     */
    @Override
    protected String getKey(String fieldName)
    {
        String[] parts = fieldName.split(FormFieldConstants.FIELD_NAME_SEPARATOR);
        if (parts.length > 0)
        {
            return parts[0];
        }
        else 
        {
            return null;
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.forms.processor.FieldProcessorRegistry#useDefaultProcessor(java.lang.String)
     */
    @Override
    protected boolean useDefaultProcessor(String fieldName)
    {
        // Only use default if the fieldName follows the format
        // prefix:localname
        String[] parts = fieldName.split(FormFieldConstants.FIELD_NAME_SEPARATOR);
        return parts.length == 2;
    }
}
