
package org.alfresco.repo.action.constraint;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.QName;

/**
 * Type parameter constraint
 * 
 * @author Roy Wetherall
 */
public class TypeParameterConstraint extends BaseParameterConstraint
{
    /** Name constant */
    public static final String NAME = "ac-types";
    
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
        Collection<QName> types = dictionaryService.getAllTypes();
        Map<String, String> result = new LinkedHashMap<String, String>(types.size());
        for (QName type : types)
        {
            TypeDefinition typeDef = dictionaryService.getType(type);
            if (typeDef != null && typeDef.getTitle(dictionaryService) != null)
            {
                result.put(type.toPrefixString(), typeDef.getTitle(dictionaryService));
            }
        }        
        return result;
    }    
    
    
}
