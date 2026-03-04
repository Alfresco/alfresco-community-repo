/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2026 Alfresco Software Limited
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
package org.alfresco.repo.node.propertyextender;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.jspecify.annotations.Nullable;

import org.alfresco.repo.node.propertyextender.PropertyExtender.CalculationContext;
import org.alfresco.repo.node.propertyextender.PropertyExtender.CalculationResult;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * This interceptor is used to extend the properties that are being added on a node. It invokes the registered property extenders to calculate the additional properties and add them together to the node.
 */
public class PropertyExtenderInterceptor implements MethodInterceptor
{
    private final NodeService nodeService;
    private final PropertyExtendersHolder extendersHolder;

    public PropertyExtenderInterceptor(NodeService nodeService, PropertyExtendersHolder extendersHolder)
    {
        this.nodeService = nodeService;
        this.extendersHolder = extendersHolder;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public @Nullable Object invoke(MethodInvocation invocation) throws Throwable
    {
        var methodName = invocation.getMethod().getName();
        var args = invocation.getArguments();

        Object ret = null;

        if (methodName.equals("addProperties") && args.length == 2 &&
                args[0] instanceof NodeRef nodeRef &&
                args[1] instanceof Map newProperties)
        {
            var extendedProps = calculateProperties((Map<QName, Serializable>) newProperties);
            nodeService.addProperties(nodeRef, extendedProps);
        }
        else if (methodName.equals("createNode") && args.length == 5 &&
                args[0] instanceof NodeRef parentNodeRef &&
                args[1] instanceof QName assocTypeQName &&
                args[2] instanceof QName assocQName &&
                args[3] instanceof QName nodeTypeQName &&
                args[4] instanceof Map newProperties)
        {
            var extendedProps = calculateProperties((Map<QName, Serializable>) newProperties);
            ret = nodeService.createNode(parentNodeRef, assocTypeQName, assocQName, nodeTypeQName, extendedProps);
        }
        else
        {
            ret = invocation.proceed();
        }
        return ret;
    }

    private Map<QName, Serializable> calculateProperties(Map<QName, Serializable> newProperties)
    {
        var extenders = extendersHolder.getExtenders();
        var calculatedProps = extenders.stream()
                .map(ext -> ext.calculate(new CalculationContext(newProperties)))
                .map(CalculationResult::calculatedProperties)
                .filter(Predicate.not(Map::isEmpty))
                .toList();

        if (calculatedProps.isEmpty())
        {
            return newProperties;
        }

        var extendedProps = new HashMap<>(newProperties);
        calculatedProps.forEach(extendedProps::putAll);
        return extendedProps;
    }
}
