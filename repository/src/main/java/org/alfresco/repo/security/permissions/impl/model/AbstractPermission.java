/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.security.permissions.impl.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.dom4j.Attribute;
import org.dom4j.Element;

import org.alfresco.repo.security.permissions.impl.AbstractPermissionReference;
import org.alfresco.repo.security.permissions.impl.RequiredPermission;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;

/**
 * Support to read and store common properties for permissions
 * 
 * @author andyh
 */
public abstract class AbstractPermission extends AbstractPermissionReference implements XMLModelInitialisable
{
    /* XML Constants */

    private static final String NAME = "name";

    private static final String REQUIRED_PERMISSION = "requiredPermission";

    private static final String RP_NAME = "name";

    private static final String RP_TYPE = "type";

    private static final String RP_ON = "on";

    private static final String RP_IMPLIES = "implies";

    private static final String NODE_ENTRY = "node";

    private static final String PARENT_ENTRY = "parent";

    private static final String CHILDREN_ENTRY = "children";

    /* Instance variables */

    private String name;

    private QName typeQName;

    private Set<RequiredPermission> requiredPermissions = new HashSet<RequiredPermission>();

    protected AbstractPermission(QName typeQName)
    {
        super();
        this.typeQName = typeQName;
    }

    public void initialise(Element element, NamespacePrefixResolver nspr, PermissionModel permissionModel)
    {
        name = element.attributeValue(NAME);

        for (Iterator rpit = element.elementIterator(REQUIRED_PERMISSION); rpit.hasNext(); /**/)
        {
            QName qName;
            Element requiredPermissionElement = (Element) rpit.next();
            Attribute typeAttribute = requiredPermissionElement.attribute(RP_TYPE);
            if (typeAttribute != null)
            {
                qName = QName.createQName(typeAttribute.getStringValue(), nspr);
            }
            else
            {
                qName = typeQName;
            }

            String requiredName = requiredPermissionElement.attributeValue(RP_NAME);

            RequiredPermission.On on;
            String onString = requiredPermissionElement.attributeValue(RP_ON);
            if (onString.equalsIgnoreCase(NODE_ENTRY))
            {
                on = RequiredPermission.On.NODE;
            }
            else if (onString.equalsIgnoreCase(PARENT_ENTRY))
            {
                on = RequiredPermission.On.PARENT;
            }
            else if (onString.equalsIgnoreCase(CHILDREN_ENTRY))
            {
                on = RequiredPermission.On.CHILDREN;
            }
            else
            {
                throw new PermissionModelException("Required permission must specify parent or node for the on attribute.");
            }

            boolean implies = false;
            Attribute impliesAttribute = requiredPermissionElement.attribute(RP_IMPLIES);
            if (impliesAttribute != null)
            {
                implies = Boolean.parseBoolean(impliesAttribute.getStringValue());
            }

            RequiredPermission rq = new RequiredPermission(qName, requiredName, on, implies);

            requiredPermissions.add(rq);
        }
    }

    public final String getName()
    {
        return name;
    }

    /**
     * Get the required permissions
     * 
     * @return the required permissions
     */
    public final Set<RequiredPermission> getRequiredPermissions()
    {
        return Collections.unmodifiableSet(requiredPermissions);
    }

    /**
     * Get the type for this permission
     * 
     * @return the type
     */
    public final QName getTypeQName()
    {
        return typeQName;
    }

    public final QName getQName()
    {
        return getTypeQName();
    }
}
