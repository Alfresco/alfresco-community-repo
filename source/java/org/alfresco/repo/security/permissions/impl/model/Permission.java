/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.security.permissions.impl.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.repo.security.permissions.impl.PermissionReferenceImpl;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.dom4j.Attribute;
import org.dom4j.Element;

/**
 * Support to read and store the definition of a permission.
 * 
 * @author andyh
 */
public class Permission extends AbstractPermission implements XMLModelInitialisable
{
    // XML Constants
    
    /**
     * 
     */
    private static final long serialVersionUID = -4560426591597681329L;

    private static final String GRANTED_TO_GROUP = "grantedToGroup";
    
    private static final String GTG_NAME = "permissionGroup";

    private static final String GTG_TYPE = "type";

    private Set<PermissionReference> grantedToGroups = new HashSet<PermissionReference>();
    
    private static final String DENY = "deny";
    
    private static final String ALLOW = "allow";
    
    private static final String DEFAULT_PERMISSION = "defaultPermission";

    private static final String EXPOSE = "expose";
    
    private static final String REQUIRES_TYPE = "requiresType";

    private AccessStatus defaultPermission;

    private boolean isExposed;
    
    private boolean requiresType;

    /**
     * A permission for the given type
     * 
     * @param typeQName
     */
    public Permission(QName typeQName)
    {
        super(typeQName);
    }

    public void initialise(Element element, NamespacePrefixResolver nspr, PermissionModel permissionModel)
    {
        super.initialise(element, nspr, permissionModel);
        
        Attribute att = element.attribute(EXPOSE);
        if (att != null)
        {
            isExposed = Boolean.parseBoolean(att.getStringValue());
        }
        else
        {
            isExposed = true;
        }
        
        att = element.attribute(REQUIRES_TYPE);
        if (att != null)
        {
            requiresType = Boolean.parseBoolean(att.getStringValue());
        }
        else
        {
            requiresType = true;
        }
        
        Attribute defaultPermissionAttribute = element.attribute(DEFAULT_PERMISSION);
        if(defaultPermissionAttribute != null)
        {
            if(defaultPermissionAttribute.getStringValue().equalsIgnoreCase(ALLOW))
            {
                defaultPermission = AccessStatus.ALLOWED;  
            }
            else if(defaultPermissionAttribute.getStringValue().equalsIgnoreCase(DENY))
            {
                defaultPermission = AccessStatus.DENIED;  
            }
            else
            {
                throw new PermissionModelException("The default permission must be deny or allow");
            }
        }
        else
        {
            defaultPermission = AccessStatus.DENIED;
        }
        
        for (Iterator gtgit = element.elementIterator(GRANTED_TO_GROUP); gtgit.hasNext(); /**/)
        {
            QName qName;
            Element grantedToGroupsElement = (Element) gtgit.next();
            Attribute typeAttribute = grantedToGroupsElement.attribute(GTG_TYPE);
            if (typeAttribute != null)
            {
                qName = QName.createQName(typeAttribute.getStringValue(), nspr);
            }
            else
            {
                qName = getTypeQName();
            }

            String grantedName = grantedToGroupsElement.attributeValue(GTG_NAME);
            
            grantedToGroups.add(PermissionReferenceImpl.getPermissionReference(qName, grantedName));
        }
    }

    /**
     * Default deny/allow for this permission
     * @return the access status
     */
    public AccessStatus getDefaultPermission()
    {
        return defaultPermission;
    }

    /**
     * Get the groups for which this permission is granted (by definition - filled in by the model API)
     * @return the specifed groups
     */
    public Set<PermissionReference> getGrantedToGroups()
    {
        return Collections.unmodifiableSet(grantedToGroups);
    }

    /**
     * Should this permission be shown to the UI?
     * @return return true if the permission be shown in the UI.
     */
    public boolean isExposed()
    {
        return isExposed;
    }
    
    /**
     * Does a node have to have the type/aspect for the permission to apply?
     * @return true if a node must  have the type/aspect for the permission to apply.
     */
    public boolean isTypeRequired()
    {
        return requiresType;
    }
}
