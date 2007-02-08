/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.security.permissions.impl.model;

import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.dom4j.Element;

/**
 * The definition of a required permission
 * 
 * @author andyh
 */
public class DynamicPermission extends AbstractPermission implements XMLModelInitialisable
{
    private static final String EVALUATOR = "evaluator";
    
    private String evaluatorFullyQualifiedClassName;
    
    public DynamicPermission(QName typeQName)
    {
        super(typeQName);
    }

    public void initialise(Element element, NamespacePrefixResolver nspr, PermissionModel permissionModel)
    {
        super.initialise(element, nspr, permissionModel);
        evaluatorFullyQualifiedClassName = element.attributeValue(EVALUATOR);   
    }

    public String getEvaluatorFullyQualifiedClassName()
    {
        return evaluatorFullyQualifiedClassName;
    }
}
