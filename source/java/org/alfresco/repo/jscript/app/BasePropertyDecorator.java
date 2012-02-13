/**
 * 
 */
package org.alfresco.repo.jscript.app;

import java.util.HashSet;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * @author Roy Wetherall
 */
public abstract class BasePropertyDecorator implements PropertyDecorator
{
    protected Set<QName> propertyNames;
    
    protected NodeService nodeService;
    
    protected NamespaceService namespaceService;
    
    protected PermissionService permissionService;
    
    protected JSONConversionComponent jsonConversionComponent;
    
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    public void setJsonConversionComponent(JSONConversionComponent jsonConversionComponent)
    {
        this.jsonConversionComponent = jsonConversionComponent;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }
    
    public void init()
    {
        jsonConversionComponent.registerPropertyDecorator(this);
    }
    
    @Override
    public Set<QName> getPropertyNames()
    {
        return propertyNames;
    }
    
    public void setPropertyName(String propertyName)
    {
        propertyNames = new HashSet<QName>(1);        
        propertyNames.add(QName.createQName(propertyName, namespaceService));
    }
    
    public void setPropertyNames(Set<String> propertyNames)
    {
        this.propertyNames = new HashSet<QName>(propertyNames.size());
        for (String propertyName : propertyNames)
        {
            this.propertyNames.add(QName.createQName(propertyName, namespaceService));
        }
    }

}
