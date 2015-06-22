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

import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

import org.alfresco.repo.search.SimpleResultSetMetaData;
import org.alfresco.repo.security.permissions.impl.acegi.FilteringResultSet;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.PermissionEvaluationMode;
import org.alfresco.service.cmr.search.ResultSet;

/**
 * ResultSet Post Method Invocation Processor
 *
 * @author Tuna Aksoy
 * @since 3.0
 */
public class ResultSetPostMethodInvocationProcessor extends BasePostMethodInvocationProcessor
{
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.classification.interceptor.processor.BasePostMethodInvocationProcessor#getClassName()
     */
    @Override
    protected Class<ResultSet> getClassName()
    {
        return ResultSet.class;
    }

    // FIXME: Change implementation
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.classification.interceptor.processor.CollectionPostMethodInvocationProcessor#process(java.lang.Object)
     */
    @SuppressWarnings({ "unchecked" })
    @Override
    public <T> T process(T object)
    {
        T result = object;
        ResultSet resultSet = getClassName().cast(result);
        BitSet inclusionMask = new BitSet(resultSet.length());
        FilteringResultSet filteringResultSet = new FilteringResultSet(resultSet, inclusionMask);

        filteringResultSet.setResultSetMetaData(
                new SimpleResultSetMetaData(
                        resultSet.getResultSetMetaData().getLimitedBy(),
                        PermissionEvaluationMode.EAGER,
                        resultSet.getResultSetMetaData().getSearchParameters()));

        List<NodeRef> nodeRefs = resultSet.getNodeRefs();
        if (!nodeRefs.isEmpty())
        {
            Iterator<NodeRef> iterator = nodeRefs.iterator();
            BasePostMethodInvocationProcessor processor = getPostMethodInvocationProcessor().getProcessor(iterator.next());

            for (int i = 0; i < nodeRefs.size(); i++)
            {
                if (processor.process(nodeRefs.get(i)) == null)
                {
                    inclusionMask.set(i, false);
                }
            }
        }

        List<ChildAssociationRef> childAssocRefs = getClassName().cast(filteringResultSet).getChildAssocRefs();
        if (!childAssocRefs.isEmpty())
        {
            Iterator<ChildAssociationRef> iterator = childAssocRefs.iterator();
            BasePostMethodInvocationProcessor processor = getPostMethodInvocationProcessor().getProcessor(iterator.next());

            for (int i = 0; i < childAssocRefs.size(); i++)
            {
                if (processor.process(nodeRefs.get(i)) == null)
                {
                    inclusionMask.set(i, false);
                }
            }
        }

        return (T) filteringResultSet;
    }
}
