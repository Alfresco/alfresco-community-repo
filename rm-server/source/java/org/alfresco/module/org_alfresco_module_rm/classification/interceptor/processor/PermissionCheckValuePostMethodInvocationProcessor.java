/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.classification.interceptor.processor;

import org.alfresco.repo.security.permissions.PermissionCheckValue;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Permission Check Value Post Method Invocation Processor
 *
 * @author Tuna Aksoy
 * @since 3.0
 */
public class PermissionCheckValuePostMethodInvocationProcessor extends AbstractPostMethodInvocationProcessor
{
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.classification.interceptor.processor.BasePostMethodInvocationProcessor#getClassName()
     */
    @Override
    protected Class<PermissionCheckValue> getClassName()
    {
        return PermissionCheckValue.class;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.classification.interceptor.processor.AbstractPostMethodInvocationProcessor#processSingleElement(java.lang.Object)
     */
    @Override
    protected <T> T processSingleElement(T object)
    {
        PermissionCheckValue permissionCheckValue = getClassName().cast(object);
        NodeRef nodeRef = permissionCheckValue.getNodeRef();
        return filter(nodeRef) == null ? null : object;
    }
}
