package org.alfresco.repo.content.transform;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.TransformationOptions;

/**
 * Transformation options for the runtime executable transformer.
 * <p>
 * Values set here are mapped to ${valueName} style strings in the tranformer
 * execution string. 
 * 
 * @author Roy Wetherall
 */
public class RuntimeExecutableContentTransformerOptions extends TransformationOptions
{
    /** Map of property values */
    private Map<String, String> propertyValues = new HashMap<String, String>(11);
    
    /**
     * Sets the map of property values that are used when executing the transformer
     * 
     * @param propertyValues    property value
     */
    public void setPropertyValues(Map<String, String> propertyValues)
    {
        this.propertyValues = propertyValues;
    }
    
    /**
     * Overrides the base class implementation to add all values set in {@link #setPropertyValues(Map)}
     */
    @Override
    public Map<String, Object> toMap()
    {
        Map<String, Object> baseProps = super.toMap();
        Map<String, Object> props = new HashMap<String, Object>(baseProps);
        props.putAll(propertyValues);
        return props;
    }
}
