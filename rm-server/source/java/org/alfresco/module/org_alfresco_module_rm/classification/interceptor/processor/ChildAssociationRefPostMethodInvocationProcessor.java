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

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.stereotype.Component;

/**
 * ChildAssociationRef Post Method Invocation Processor
 *
 * @author Tuna Aksoy
 * @since 3.0
 */
@Component
public class ChildAssociationRefPostMethodInvocationProcessor extends AbstractPostMethodInvocationProcessor
{
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.classification.interceptor.processor.BasePostMethodInvocationProcessor#getClassName()
     */
    @Override
    protected Class<ChildAssociationRef> getClassName()
    {
        return ChildAssociationRef.class;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.classification.interceptor.processor.AbstractPostMethodInvocationProcessor#processSingleElement(java.lang.Object)
     */
    @Override
    protected <T> T processSingleElement(T object)
    {
        T result;

        ChildAssociationRef childAssociationRef = getClassName().cast(object);

        NodeRef childRef = childAssociationRef.getChildRef();
        NodeRef filteredChildRef = filter(childRef);

        NodeRef parentRef = childAssociationRef.getParentRef();
        NodeRef filteredParentRef;
        if (parentRef == null)
        {
            result = filteredChildRef == null ? null : object;
        }
        else
        {
            filteredParentRef = filter(parentRef);
            result = (filteredChildRef == null || filteredParentRef == null) ? null : object;
        }

        return result;
    }
}
