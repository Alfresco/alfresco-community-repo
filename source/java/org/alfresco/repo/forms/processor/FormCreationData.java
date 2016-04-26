
package org.alfresco.repo.forms.processor;

import java.util.Map;

/**
 * Interface definition for a simple DTO containing various objects 
 * needed to generate Forms.
 * 
 * @since 3.4
 * @author Nick Smith
 */
public interface FormCreationData
{
    /**
     * @return the itemData
     */
    Object getItemData();
    
    /**
     * @return If the <code>fieldName</code> given is a forced field then
     *         returns <code>true</code>, otherwise returns <code>false</code>.
     */
    boolean isForcedField(String fieldName);
    
    /**
     * @return the context
     */
    Map<String, Object> getContext();
}
