
package org.alfresco.repo.forms.processor.workflow;

import org.alfresco.repo.forms.FormData.FieldData;

/**
 * Interface definition for a helper class that handles persisting form data.
 * 
 * @since 3.4
 * @author Nick Smith
 *
 * @param <T>
 */
public interface FormPersister<T>
{
    void addField(FieldData fieldData);
    
    T persist();
}
