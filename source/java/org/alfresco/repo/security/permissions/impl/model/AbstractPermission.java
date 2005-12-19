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

import org.alfresco.repo.security.permissions.impl.AbstractPermissionReference;
import org.alfresco.repo.security.permissions.impl.RequiredPermission;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.dom4j.Attribute;
import org.dom4j.Element;

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
    
    public AbstractPermission(QName typeQName)
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

    public final Set<RequiredPermission> getRequiredPermissions()
    {
        return Collections.unmodifiableSet(requiredPermissions);
    }

    public final QName getTypeQName()
    {
        return typeQName;
    }

    public final QName getQName()
    {
        return getTypeQName();
    }
}
