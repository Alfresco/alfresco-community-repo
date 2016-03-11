package org.alfresco.module.org_alfresco_module_rm.capability.declarative.condition;

import org.alfresco.module.org_alfresco_module_rm.capability.declarative.AbstractCapabilityCondition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * @author Roy Wetherall
 */
public class HasAspectCapabilityCondition extends AbstractCapabilityCondition
{
    private String aspectName;
    
    private NamespaceService namespaceService;
    
    public void setAspectName(String aspectName)
    {
        this.aspectName = aspectName;
    }
    
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.declarative.CapabilityCondition#evaluate(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public boolean evaluateImpl(NodeRef nodeRef)
    {
        boolean result = false;
        if (aspectName != null)
        {
            QName aspect = QName.createQName(aspectName, namespaceService);
            result = nodeService.hasAspect(nodeRef, aspect);
        }
        
        return result;
    }

}
