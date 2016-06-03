
package org.alfresco.repo.action.constraint;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.QName;

/**
 * Type parameter constraint
 * 
 * @author Roy Wetherall
 */
public class AspectParameterConstraint extends BaseParameterConstraint
{
    /** Name constant */
    public static final String NAME = "ac-aspects";
    
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
        Collection<QName> aspects = dictionaryService.getAllAspects();
        Map<String, String> result = new LinkedHashMap<String, String>(aspects.size());
        for (QName aspect : aspects)
        {
            AspectDefinition aspectDef = dictionaryService.getAspect(aspect);
            if (aspectDef != null && aspectDef.getTitle(dictionaryService) != null)
            {
                result.put(aspect.toPrefixString(), aspectDef.getTitle(dictionaryService));
            }
        }        
        return result;
    }    
    
    
}
