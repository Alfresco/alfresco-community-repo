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

import static org.alfresco.service.cmr.search.PermissionEvaluationMode.EAGER;

import java.util.BitSet;

import org.alfresco.repo.search.SimpleResultSetMetaData;
import org.alfresco.repo.security.permissions.impl.acegi.FilteringResultSet;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetMetaData;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.springframework.stereotype.Component;

/**
 * ResultSet Post Method Invocation Processor
 *
 * @author Tuna Aksoy
 * @since 3.0
 */
@Component
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

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.classification.interceptor.processor.CollectionPostMethodInvocationProcessor#process(java.lang.Object)
     */
    @SuppressWarnings({ "unchecked" })
    @Override
    public <T> T process(T object)
    {
        T result = object;

        if (result != null)
        {
            ResultSet returnedObject = getClassName().cast(result);

            BitSet inclusionMask = new BitSet(returnedObject.length());
            FilteringResultSet filteringResultSet = new FilteringResultSet(returnedObject, inclusionMask);

            ResultSetMetaData resultSetMetaData = returnedObject.getResultSetMetaData();
            SearchParameters searchParameters = resultSetMetaData.getSearchParameters();

            BasePostMethodInvocationProcessor nodeRefProcessor = null;
            BasePostMethodInvocationProcessor childAssociationRefProcessor = null;

            for (int i = 0; i < returnedObject.length(); i++)
            {
                ResultSetRow row = returnedObject.getRow(i);
                NodeRef nodeRef = row.getNodeRef();

                if (nodeRefProcessor == null)
                {
                    nodeRefProcessor = getPostMethodInvocationProcessor().getProcessor(nodeRef);
                }

                NodeRef processedNodeRef = nodeRefProcessor.process(nodeRef);
                if (processedNodeRef == null)
                {
                    inclusionMask.set(i, false);
                }
                else
                {
                    ChildAssociationRef childAssocRef = row.getChildAssocRef();

                    if (childAssociationRefProcessor == null)
                    {
                        childAssociationRefProcessor = getPostMethodInvocationProcessor().getProcessor(childAssocRef);
                    }

                    ChildAssociationRef childAssociationRef = childAssociationRefProcessor.process(childAssocRef);
                    if (childAssociationRef == null)
                    {
                        inclusionMask.set(i, false);
                    }
                    else
                    {
                        inclusionMask.set(i, true);
                    }
                }
            }

            SimpleResultSetMetaData simpleResultSetMetaData = new SimpleResultSetMetaData(resultSetMetaData.getLimitedBy(), EAGER, searchParameters);
            filteringResultSet.setResultSetMetaData(simpleResultSetMetaData);
            result = (T) filteringResultSet;
        }

        return result;
    }
}
