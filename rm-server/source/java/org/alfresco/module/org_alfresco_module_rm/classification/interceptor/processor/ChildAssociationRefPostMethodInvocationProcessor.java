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

import java.util.Collection;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.collections.CollectionUtils;
import org.alfresco.util.collections.Filter;

/**
 * ChildAssociationRef Post Method Invocation Processor
 *
 * @author Tuna Aksoy
 * @since 3.0
 */
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
        ChildAssociationRef childAssociationRef = ((ChildAssociationRef) object);

        NodeRef childRef = childAssociationRef.getChildRef();
        NodeRef filteredChildRef = filter(childRef);

        NodeRef parentRef = childAssociationRef.getParentRef();
        NodeRef filteredParentRef = filter(parentRef);

        return (filteredChildRef == null || filteredParentRef == null) ? null : object;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.classification.interceptor.processor.AbstractPostMethodInvocationProcessor#processCollection(java.util.Collection)
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected Collection processCollection(Collection collection)
    {
        return CollectionUtils.filter(collection, new Filter<ChildAssociationRef>()
        {
            @Override
            public Boolean apply(ChildAssociationRef childAssociationRef)
            {
                return processSingleElement(childAssociationRef) != null;
            }
        });
    }
}
