/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
            PermissionReference permissionReference = new PermissionReferenceImpl(qName, refName);
            includedPermissionGroups.add(permissionReference);
        }
    }

    public Set<PermissionReference> getIncludedPermissionGroups()
    {
        return Collections.unmodifiableSet(includedPermissionGroups);
    }

    public String getName()
    {
        return name;
    }

    public boolean isAllowFullControl()
    {
        return allowFullControl;
    }

    public QName getQName()
    {
        return container;
    }

    public boolean isExtends()
    {
        return extendz;
    }

    public QName getTypeQName()
    {
        return type;
    }

    public boolean isExposed()
    {
        return isExposed;
    }
    
    
    public boolean isTypeRequired()
    {
        return requiresType;
    }
}
