package org.alfresco.module.org_alfresco_module_rm.capability.declarative.condition;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.capability.declarative.AbstractCapabilityCondition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Fileable capability condition.  Indicates whether a node is 'fileable', namely if it extends cm:content
 * or extends rma:nonElectronicDocument
 * 
 * @author Roy Wetherall
 */
public class FileableCapabilityCondition extends AbstractCapabilityCondition
{   
    /** Dictionary service */
    private DictionaryService dictionaryService;
    
    /**
     * @param dictionaryService     dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.declarative.CapabilityCondition#evaluate(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public boolean evaluateImpl(NodeRef nodeRef)
    {
        QName type = nodeService.getType(nodeRef);        
        // TODO and not already a record?
        return (dictionaryService.isSubClass(type, ContentModel.TYPE_CONTENT) ||
                dictionaryService.isSubClass(type, TYPE_NON_ELECTRONIC_DOCUMENT));
    }

}
