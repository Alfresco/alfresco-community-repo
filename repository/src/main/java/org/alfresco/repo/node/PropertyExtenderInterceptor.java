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
package org.alfresco.repo.node;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.alfresco.repo.node.PropertyExtender.CalculationContext;
import org.alfresco.repo.node.PropertyExtender.CalculationResult;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

public class PropertyExtenderInterceptor implements MethodInterceptor
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyExtenderInterceptor.class);

    private final NodeService nodeService;
    private final PropertyExtendersHolder extendersHolder;

    public PropertyExtenderInterceptor(NodeService nodeService, PropertyExtendersHolder extendersHolder)
    {
        this.nodeService = nodeService;
        this.extendersHolder = extendersHolder;
    }

    @Override
    public @Nullable Object invoke(MethodInvocation invocation) throws Throwable
    {
        var methodName = invocation.getMethod().getName();
        var args = invocation.getArguments();

        Object ret = null;

        if (methodName.equals("addProperties"))
        {
            var nodeRef = (NodeRef) args[0];
            var newProperties = (Map<QName, Serializable>) args[1];

            var extendedProps = calculateProperties(newProperties);
            nodeService.addProperties(nodeRef, extendedProps);
        }
        else if (methodName.equals("createNode") && args.length == 5)
        {
            var parentNodeRef = (NodeRef) args[0];
            var assocTypeQName = (QName) args[1];
            var assocQName = (QName) args[2];
            var nodeTypeQName = (QName) args[3];
            var newProperties = (Map<QName, Serializable>) args[4];

            var extendedProps = calculateProperties(newProperties);
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
        var extendedProps = new HashMap<>(newProperties);
        extenders.stream()
                .map(ext -> ext.calculate(new CalculationContext(newProperties)))
                .map(CalculationResult::calculatedProperties)
                .forEach(extendedProps::putAll);
        return extendedProps;
    }
}
