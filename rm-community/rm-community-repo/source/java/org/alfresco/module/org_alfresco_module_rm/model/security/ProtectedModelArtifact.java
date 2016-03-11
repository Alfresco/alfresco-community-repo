package org.alfresco.module.org_alfresco_module_rm.model.security;

import java.util.HashSet;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Protected model artifact class.
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public abstract class ProtectedModelArtifact
{
    /** Model security service */
    private ModelSecurityService modelSecurityService;    
    
    /** Namespace service */
    private NamespaceService namespaceService;
    
    /** Qualified name of the model artifact */
    private QName name;
    
    /** Set of capabilities */
    private Set<Capability> capabilities;
    
    /** Capability names */
    private Set<String> capabilityNames;
    
    /**
     * @param namespaceService  namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    /**
     * @param modelSecurityService  model security service
     */
    public void setModelSecurityService(ModelSecurityService modelSecurityService)
    {
        this.modelSecurityService = modelSecurityService;
    }
    
    /**
     * Init method
     */
    public void init()
    {
        modelSecurityService.register(this);
    }
    
    /**
     * @param name  artifact name (in cm:content form)
     */
    public void setName(String name)
    {
        QName qname = QName.createQName(name, namespaceService);
        this.name = qname;
    }
    
    /**
     * @return  artifact QName
     */
    public QName getQName()
    {
        return name;
    }
    
    /**
     * @param capabilities  capabilities
     */
    public void setCapabilities(Set<Capability> capabilities)
    {
        this.capabilities = capabilities;
    }
    
    /**
     * @return  capabilities
     */
    public Set<Capability> getCapabilities()
    {
        return capabilities;
    }
    
    /**
     * @return  capability names
     */
    public Set<String> getCapilityNames()
    {
        if (capabilityNames == null && capabilities != null)
        {
            capabilityNames = new HashSet<String>(capabilities.size());
            for (Capability capability : capabilities)
            {
                capabilityNames.add(capability.getName());
            }            
        }
        
        return capabilityNames;
    }
}
