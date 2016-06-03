
package org.alfresco.repo.action.constraint;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;

/**
 * Type parameter constraint
 * 
 * @author Roy Wetherall
 */
public class PropertyParameterConstraint extends BaseParameterConstraint
{
    /** Name constant */
    public static final String NAME = "ac-properties";
    
    private DictionaryService dictionaryService;
    
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
          
    /**
     * @see org.alfresco.service.cmr.action.ParameterConstraint#getAllowableValues()
     */
    protected Map<String, String> getAllowableValuesImpl()
    {   
        Collection<QName> properties = dictionaryService.getAllProperties(null);
        Map<String, String> result = new LinkedHashMap<String, String>(properties.size());
        for (QName property : properties)
        {
            PropertyDefinition propertyDef = dictionaryService.getProperty(property);
            if (propertyDef != null && propertyDef.getTitle(dictionaryService) != null)
            {
                result.put(property.toPrefixString(), propertyDef.getTitle(dictionaryService));
            }
        }        
        return result;
    }    
    
    
}
