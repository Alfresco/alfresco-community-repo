
package org.alfresco.repo.forms.processor;

import java.util.List;
import java.util.Map;

/**
 * Simple DTO containing various objects needed to generate Forms.
 * 
 * @since 3.4
 * @author Nick Smith
 */
public class FormCreationDataImpl implements FormCreationData
{
    private final Object itemData;
    private final List<String> forcedFields;
    private final Map<String, Object> context;

    public FormCreationDataImpl(Object itemData, List<String> forcedFields, Map<String, Object> context)
    {
        this.itemData = itemData;
        this.forcedFields = forcedFields;
        this.context = context;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.forms.processor.FormCreationData#getItemData()
     */
    public Object getItemData()
    {
        return itemData;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.alfresco.repo.forms.processor.FormCreationData#isForcedField(java
     * .lang.String)
     */
    public boolean isForcedField(String fieldName)
    {
        if (forcedFields == null)
        {
            return false;
        }
        
        return forcedFields.contains(fieldName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.repo.forms.processor.FormCreationData#getContext()
     */
    public Map<String, Object> getContext()
    {
        return context;
    }
}
