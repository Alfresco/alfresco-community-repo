/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.repo.search.impl.querymodel.impl.db;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.search.impl.querymodel.Argument;
import org.alfresco.repo.search.impl.querymodel.Column;
import org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext;
import org.alfresco.repo.search.impl.querymodel.Order;
import org.alfresco.repo.search.impl.querymodel.PropertyArgument;
import org.alfresco.repo.search.impl.querymodel.QueryModelException;
import org.alfresco.repo.search.impl.querymodel.impl.BaseOrdering;
import org.alfresco.repo.search.impl.querymodel.impl.functions.PropertyAccessor;
import org.alfresco.repo.search.impl.querymodel.impl.functions.Score;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.chemistry.opencmis.commons.PropertyIds;


/**
 * @author Andy
 *
 */
public class DBOrdering extends BaseOrdering implements DBQueryBuilderComponent
{
    DBQueryBuilderComponent builderSupport;

    /**
     * @param column Column
     * @param order Order
     */
    public DBOrdering(Column column, Order order)
    {
        super(column, order);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#isSupported()
     */
    @Override
    public boolean isSupported()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#prepare(org.alfresco.service.namespace.NamespaceService, org.alfresco.service.cmr.dictionary.DictionaryService, org.alfresco.repo.domain.qname.QNameDAO, org.alfresco.repo.domain.node.NodeDAO, java.util.Set, java.util.Map, org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext)
     */
    @Override
    public void prepare(NamespaceService namespaceService, DictionaryService dictionaryService, QNameDAO qnameDAO, NodeDAO nodeDAO, TenantService tenantService, Set<String> selectors,
            Map<String, Argument> functionArgs, FunctionEvaluationContext functionContext, boolean supportBooleanFloatAndDouble)
    {
        if (getColumn().getFunction().getName().equals(PropertyAccessor.NAME))
        {
            PropertyArgument property = (PropertyArgument) getColumn().getFunctionArguments().get(PropertyAccessor.ARG_PROPERTY);
            if (property == null)
            {
                throw new QueryModelException("No property to order ");
            }

            if (property.getPropertyName().equals(PropertyIds.PARENT_ID))
            {
                throw new QueryModelException("Ordering is not supported for "+PropertyIds.PARENT_ID);
            }
            else if (property.getPropertyName().equals(PropertyIds.OBJECT_ID))
            {
                throw new QueryModelException("Ordering is not supported for "+PropertyIds.OBJECT_ID);
            }
            else if (property.getPropertyName().equals(PropertyIds.OBJECT_TYPE_ID))
            {
                throw new QueryModelException("Ordering is not supported for "+PropertyIds.OBJECT_TYPE_ID);
            }
            else if (property.getPropertyName().equals(PropertyIds.BASE_TYPE_ID))
            {
                throw new QueryModelException("Ordering is not supported for "+PropertyIds.BASE_TYPE_ID);
            }
            else if (property.getPropertyName().equals(PropertyIds.CONTENT_STREAM_MIME_TYPE))
            {
                PropertySupport propertySupport = new PropertySupport();
                if(getOrder() == Order.ASCENDING)
                {
                    propertySupport.setValue("ASC");
                }
                else  if(getOrder() == Order.DESCENDING)
                {
                    propertySupport.setValue("DESC");
                }
                
                QName basePropertyQName = QName.createQName(DBQuery.expandQName(functionContext.getAlfrescoPropertyName(property.getPropertyName()), namespaceService));
                propertySupport.setPropertyQName(basePropertyQName);
                propertySupport.setPair(qnameDAO.getQName(basePropertyQName));
                propertySupport.setJoinCommandType(DBQueryBuilderJoinCommandType.CONTENT_MIMETYPE);
                propertySupport.setFieldName("mimetype_str");
                propertySupport.setCommandType(DBQueryBuilderPredicatePartCommandType.ORDER);
                propertySupport.setLeftOuter(true);
                builderSupport = propertySupport;
            }
            else if (property.getPropertyName().equals(PropertyIds.CONTENT_STREAM_LENGTH))
            {
                PropertySupport propertySupport = new PropertySupport();
                if(getOrder() == Order.ASCENDING)
                {
                    propertySupport.setValue("ASC");
                }
                else  if(getOrder() == Order.DESCENDING)
                {
                    propertySupport.setValue("DESC");
                }
                
                QName basePropertyQName = QName.createQName(DBQuery.expandQName(functionContext.getAlfrescoPropertyName(property.getPropertyName()), namespaceService));
                propertySupport.setPropertyQName(basePropertyQName);
                propertySupport.setPair(qnameDAO.getQName(basePropertyQName));
                propertySupport.setJoinCommandType(DBQueryBuilderJoinCommandType.CONTENT_URL);
                propertySupport.setFieldName("content_size");
                propertySupport.setCommandType(DBQueryBuilderPredicatePartCommandType.ORDER);
                propertySupport.setLeftOuter(true);
                builderSupport = propertySupport;
            }
            else
            {
                PropertySupport propertySupport = new PropertySupport();
                if(getOrder() == Order.ASCENDING)
                {
                    propertySupport.setValue("ASC");
                }
                else  if(getOrder() == Order.DESCENDING)
                {
                    propertySupport.setValue("DESC");
                }
                QName propertyQName = QName.createQName(DBQuery.expandQName(functionContext.getAlfrescoPropertyName(property.getPropertyName()), namespaceService));
                propertySupport.setPropertyQName(propertyQName);
                propertySupport.setPair(qnameDAO.getQName(propertyQName));
                propertySupport.setJoinCommandType(DBQuery.getJoinCommandType(propertyQName));
                propertySupport.setFieldName(DBQuery.getFieldName(dictionaryService, propertyQName, supportBooleanFloatAndDouble));
                propertySupport.setCommandType(DBQueryBuilderPredicatePartCommandType.ORDER);
                propertySupport.setLeftOuter(true);
                builderSupport = propertySupport;
            }
        }
        else if (getColumn().getFunction().getName().equals(Score.NAME))
        {
            throw new QueryModelException("Ordering on score() is not supported");
        }
        else
        {
            throw new QueryModelException("Ordering not supported "+getColumn().getFunction().getName());
        }


    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#buildJoins(java.util.Map, java.util.List)
     */
    @Override
    public void buildJoins(Map<QName, DBQueryBuilderJoinCommand> singleJoins, List<DBQueryBuilderJoinCommand> multiJoins)
    {
        builderSupport.buildJoins(singleJoins, multiJoins);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#buildPredicateCommands(java.util.List)
     */
    @Override
    public void buildPredicateCommands(List<DBQueryBuilderPredicatePartCommand> predicatePartCommands)
    {
        builderSupport.buildPredicateCommands(predicatePartCommands);
    }

}
