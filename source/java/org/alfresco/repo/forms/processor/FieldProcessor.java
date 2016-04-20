
package org.alfresco.repo.forms.processor;

import org.alfresco.repo.forms.Field;

/**
 * Interface definition for a field processor.
 * 
 * @since 3.4
 * @author Nick Smith
 */
public interface FieldProcessor
{
    Field generateField(String fieldName, FormCreationData data);
}
