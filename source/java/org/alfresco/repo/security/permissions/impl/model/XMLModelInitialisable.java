package org.alfresco.repo.security.permissions.impl.model;

import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.dom4j.Element;

/**
 * Interface to initialise a component of the permission mode from its XML representation.
 * 
 * @author andyh
 */
public interface XMLModelInitialisable
{
    public void initialise(Element element, NamespacePrefixResolver nspr, PermissionModel permissionModel);
}
