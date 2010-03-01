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
import org.alfresco.repo.security.permissions.impl.AbstractPermissionReference;
import org.alfresco.repo.security.permissions.impl.PermissionReferenceImpl;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.dom4j.Attribute;
import org.dom4j.Element;

/**
 * Support to read and store the defintion of permission groups.
 * 
 * @author andyh
 */
public final class PermissionGroup extends AbstractPermissionReference implements XMLModelInitialisable
{
    // XML Constants
    
    /**
     * 
     */
    private static final long serialVersionUID = 7879839657714155737L;

    private static final String NAME = "name";
    
    private static final String EXTENDS = "extends";

    private static final String ALLOW_FULL_CONTOL = "allowFullControl";

    private static final String INCLUDE_PERMISSION_GROUP = "includePermissionGroup";

    private static final String PERMISSION_GROUP = "permissionGroup";

    private static final String TYPE = "type";
    
    private static final String EXPOSE = "expose";
    
    private static final String REQUIRES_TYPE = "requiresType";

    private String name;
    
    private QName type;
    
    private boolean extendz;

    private boolean isExposed;
    
    private boolean allowFullControl;

    private QName container;

    private Set<PermissionReference> includedPermissionGroups = new HashSet<PermissionReference>(4, 1.0f);

    private boolean requiresType;

    /**
     * Permission group for the given type or aspect.
     * @param container
     */
    public PermissionGroup(QName container)
    {
        super();
        this.container = container;
    }

    public void initialise(Element element, NamespacePrefixResolver nspr, PermissionModel permissionModel)
    {
        // Name
        name = element.attributeValue(NAME);
        // Allow full control
        Attribute att = element.attribute(ALLOW_FULL_CONTOL);
        if (att != null)
        {
            allowFullControl = Boolean.parseBoolean(att.getStringValue());
        }
        else
        {
            allowFullControl = false;
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
        
        att = element.attribute(EXTENDS);
        if (att != null)
        {
            extendz = Boolean.parseBoolean(att.getStringValue());
        }
        else
        {
            extendz = false;
        }
        
        att = element.attribute(EXPOSE);
        if (att != null)
        {
            isExposed = Boolean.parseBoolean(att.getStringValue());
        }
        else
        {
            isExposed = true;
        }
        
        att = element.attribute(TYPE);
        if (att != null)
        {
            type = QName.createQName(att.getStringValue(),nspr);
        }
        else
        {
            type = null;
        }
        
        // Include permissions defined for other permission groups

        for (Iterator ipgit = element.elementIterator(INCLUDE_PERMISSION_GROUP); ipgit.hasNext(); /**/)
        {
            QName qName;
            Element includePermissionGroupElement = (Element) ipgit.next();
            Attribute typeAttribute = includePermissionGroupElement.attribute(TYPE);
            if (typeAttribute != null)
            {
                qName = QName.createQName(typeAttribute.getStringValue(), nspr);
            }
            else
            {
                qName = container;
            }
            String refName = includePermissionGroupElement.attributeValue(PERMISSION_GROUP);
            PermissionReference permissionReference = PermissionReferenceImpl.getPermissionReference(qName, refName);
            includedPermissionGroups.add(permissionReference);
        }
    }

    /**
     * What permission groups are included in this one (for ease of definition)
     * @return - the set of included permission from teh definitio
     */
    public Set<PermissionReference> getIncludedPermissionGroups()
    {
        return Collections.unmodifiableSet(includedPermissionGroups);
    }

    public String getName()
    {
        return name;
    }

    /**
     * Does this permission group allow full control?
     * @return true if this definition allows full control
     */
    public boolean isAllowFullControl()
    {
        return allowFullControl;
    }

    public QName getQName()
    {
        return container;
    }

    /**
     * Does this definition extend another (from a base type as defined in the DD)
     * @return true if the permission is extended from another type
     */
    public boolean isExtends()
    {
        return extendz;
    }

    /**
     * Get the  class
     * @return - the class
     */
    public QName getTypeQName()
    {
        return type;
    }

    /**
     * Expose in the UI?
     * @return exposed -> true
     */
    public boolean isExposed()
    {
        return isExposed;
    }
    
    /**
     * Does a node have to have a the type for the permission to apply?
     * @return true if a node has to have the type for the permission to apply.
     */
    public boolean isTypeRequired()
    {
        return requiresType;
    }
}
