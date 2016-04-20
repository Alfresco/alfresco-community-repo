
package org.alfresco.service.cmr.action;

import java.util.Map;

import org.alfresco.api.AlfrescoPublicApi;

/**
 * Parameter constraint.  Helps to constraint the list of allowable values for a action parameter.
 * 
 * @author Roy Wetherall
 */
@AlfrescoPublicApi
public interface ParameterConstraint
{
    /**
     * Gets the unique name of the constraint
     * 
     * @return String   constraint name
     */
    String getName();
    
    /**
     * Indicates whether the provided value satisfies the constraint.  True if it does, false otherwise.
     * 
     * @return  boolean  true if valid, false otherwise
     */
    boolean isValidValue(String value);
    
    /**
     * 
     * @param value String
     * @return String
     */
    String getValueDisplayLabel(String value);
    
    /**
     *  The implementers are expected to return allowed values in the insertion order.
     */
    Map<String, String> getAllowableValues();
}
