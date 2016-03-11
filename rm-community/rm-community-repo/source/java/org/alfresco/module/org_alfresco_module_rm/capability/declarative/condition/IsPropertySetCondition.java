package org.alfresco.module.org_alfresco_module_rm.capability.declarative.condition;

import org.alfresco.module.org_alfresco_module_rm.capability.declarative.AbstractCapabilityCondition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;

/**
 * Indicates whether a property is set or not.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public class IsPropertySetCondition extends AbstractCapabilityCondition
{
    /** property name (eg: rma:location) */
    private String propertyName;
    private QName propertyQName;
    
    /** namespace service */
    private NamespaceService namespaceService;
    
    /**
     * @param propertyName  property name (eg: rma:location)
     */
    public void setPropertyName(String propertyName)
    {
        this.propertyName = propertyName;
    }
    
    /**
     * @param namespaceService  namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    /**
     * @return QName    property qname
     */
    protected QName getPropertyQName()
    {
    	if (propertyQName == null)
    	{
    		propertyQName = QName.createQName(propertyName, namespaceService);
    	}
    	return propertyQName;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.declarative.CapabilityCondition#evaluate(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public boolean evaluateImpl(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);

        boolean result = false;
        
        if (nodeService.getProperty(nodeRef, getPropertyQName()) != null)
        {
            result = true;
        }
                
        return result;
    }

}
