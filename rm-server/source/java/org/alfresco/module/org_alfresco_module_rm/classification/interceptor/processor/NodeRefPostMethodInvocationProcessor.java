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

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.stereotype.Component;

/**
 * NodeRef Post Method Invocation Processor
 *
 * @author Tuna Aksoy
 * @since 3.0
 */
@Component
public class NodeRefPostMethodInvocationProcessor extends BasePostMethodInvocationProcessor
{
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.classification.interceptor.processor.BasePostMethodInvocationProcessor#getClassName()
     */
    @Override
    protected Class<NodeRef> getClassName()
    {
        return NodeRef.class;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.classification.interceptor.processor.BasePostMethodInvocationProcessor#process(java.lang.Object)
     */
    @Override
    public <T> T process(T object)
    {
        T result = object;

        if (result != null)
        {
            NodeRef nodeRef = getClassName().cast(result);
            if (filter(nodeRef) == null)
            {
                result = null;
            }
        }

        return result;
    }
}
