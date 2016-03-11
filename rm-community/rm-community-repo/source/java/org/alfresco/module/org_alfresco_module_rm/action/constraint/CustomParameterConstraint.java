package org.alfresco.module.org_alfresco_module_rm.action.constraint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.action.constraint.BaseParameterConstraint;

/**
 * A parameter constraint that reads in a list of allowable values from Spring configuration
 * 
 * @author Craig Tan
 * @since 2.1
 */
public class CustomParameterConstraint extends BaseParameterConstraint
{
    
    private List<String> parameterValues;

    /**
     * @param parameterValues
     */
    public void setParameterValues(List<String> parameterValues)
    {
        this.parameterValues = parameterValues;
    }
    
    /**
     * @see org.alfresco.service.cmr.action.ParameterConstraint#getAllowableValues()
     */
    protected Map<String, String> getAllowableValuesImpl()
    {                  
        Map<String, String> allowableValues = new HashMap<String, String>(parameterValues.size());
        
        for (Object parameterValue : parameterValues)
        {
            // Look up the I18N value
            String displayLabel = getI18NLabel(parameterValue.toString());
            
            // Add to the map of allowed values
            allowableValues.put(parameterValue.toString(), displayLabel);
        } 
        
        return allowableValues;
    }
}
