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
    /**
     * 
     */
    private static final long serialVersionUID = 8060533686472973313L;

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
