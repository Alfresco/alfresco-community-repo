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
package org.alfresco.repo.search.impl.querymodel.impl.db.functions;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.PropertyIds;

import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.search.impl.querymodel.Argument;
import org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext;
import org.alfresco.repo.search.impl.querymodel.PropertyArgument;
import org.alfresco.repo.search.impl.querymodel.impl.db.DBQuery;
import org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent;
import org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderJoinCommand;
import org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderJoinCommandType;
import org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderPredicatePartCommand;
import org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderPredicatePartCommandType;
import org.alfresco.repo.search.impl.querymodel.impl.db.PropertySupport;
import org.alfresco.repo.search.impl.querymodel.impl.functions.Like;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * @author Andy
 *
 */
public class DBLike extends Like implements DBQueryBuilderComponent
{
    DBQueryBuilderComponent builderSupport;

    /* (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#isSupported() */
    @Override
    public boolean isSupported()
    {
        return true;
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#prepare(org.alfresco.service.namespace.NamespaceService, org.alfresco.service.cmr.dictionary.DictionaryService, org.alfresco.repo.domain.qname.QNameDAO, org.alfresco.repo.domain.node.NodeDAO, java.util.Set, java.util.Map, org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext) */
    @Override
    public void prepare(NamespaceService namespaceService, DictionaryService dictionaryService, QNameDAO qnameDAO, NodeDAO nodeDAO, TenantService tenantService, Set<String> selectors,
            Map<String, Argument> functionArgs, FunctionEvaluationContext functionContext, boolean supportBooleanFloatAndDouble)
    {
        PropertyArgument propertyArgument = (PropertyArgument) functionArgs.get(ARG_PROPERTY);
        Argument inverseArgument = functionArgs.get(ARG_NOT);
        Boolean not = DefaultTypeConverter.INSTANCE.convert(Boolean.class, inverseArgument.getValue(functionContext));
        Argument expressionArgument = functionArgs.get(ARG_EXP);
        String expression = expressionArgument.getValue(functionContext).toString();

        if (propertyArgument.getPropertyName().equals(PropertyIds.CONTENT_STREAM_MIME_TYPE))
        {
            PropertySupport propertySupport = new PropertySupport();
            propertySupport.setValue(expression);

            QName basePropertyQName = QName.createQName(DBQuery.expandQName(functionContext.getAlfrescoPropertyName(propertyArgument.getPropertyName()), namespaceService));
            propertySupport.setPropertyQName(basePropertyQName);
            propertySupport.setPropertyDataType(DBQuery.getDataTypeDefinition(dictionaryService, basePropertyQName));
            propertySupport.setPair(qnameDAO.getQName(basePropertyQName));
            propertySupport.setJoinCommandType(DBQueryBuilderJoinCommandType.CONTENT_MIMETYPE);
            propertySupport.setFieldName("mimetype_str");
            if ((not != null) && (not.equals(Boolean.TRUE)))
            {
                propertySupport.setCommandType(DBQueryBuilderPredicatePartCommandType.NOTLIKE);
            }
            else
            {
                propertySupport.setCommandType(DBQueryBuilderPredicatePartCommandType.LIKE);
            }
            builderSupport = propertySupport;
        }
        else
        {
            PropertySupport propertySupport = new PropertySupport();
            propertySupport.setValue(expression);

            QName propertyQName = QName.createQName(DBQuery.expandQName(functionContext.getAlfrescoPropertyName(propertyArgument.getPropertyName()), namespaceService));
            propertySupport.setPropertyQName(propertyQName);
            propertySupport.setPropertyDataType(DBQuery.getDataTypeDefinition(dictionaryService, propertyQName));
            propertySupport.setPair(qnameDAO.getQName(propertyQName));
            propertySupport.setJoinCommandType(DBQuery.getJoinCommandType(propertyQName));
            propertySupport.setFieldName(DBQuery.getFieldName(dictionaryService, propertyQName, supportBooleanFloatAndDouble));
            if ((not != null) && (not.equals(Boolean.TRUE)))
            {
                propertySupport.setCommandType(DBQueryBuilderPredicatePartCommandType.NOTLIKE);
            }
            else
            {
                propertySupport.setCommandType(DBQueryBuilderPredicatePartCommandType.LIKE);
            }
            builderSupport = propertySupport;
        }
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#buildJoins(java.util.Map, java.util.List) */
    @Override
    public void buildJoins(Map<QName, DBQueryBuilderJoinCommand> singleJoins, List<DBQueryBuilderJoinCommand> multiJoins)
    {
        builderSupport.buildJoins(singleJoins, multiJoins);
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#buildPredicateCommands(java.util.List) */
    @Override
    public void buildPredicateCommands(List<DBQueryBuilderPredicatePartCommand> predicatePartCommands)
    {
        builderSupport.buildPredicateCommands(predicatePartCommands);
    }

}
