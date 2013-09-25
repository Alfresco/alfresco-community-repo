/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.search.impl.querymodel.impl.db;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.search.impl.querymodel.Argument;
import org.alfresco.repo.search.impl.querymodel.Function;
import org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext;
import org.alfresco.repo.search.impl.querymodel.impl.BaseFunctionalConstraint;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * @author Andy
 *
 */
public class DBFunctionalConstraint extends BaseFunctionalConstraint implements DBQueryBuilderComponent
{

    /**
     * @param function
     * @param arguments
     */
    public DBFunctionalConstraint(Function function, Map<String, Argument> arguments)
    {
        super(function, arguments);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#isSupported()
     */
    @Override
    public boolean isSupported()
    {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#prepare(org.alfresco.service.namespace.NamespaceService, org.alfresco.service.cmr.dictionary.DictionaryService, org.alfresco.repo.domain.qname.QNameDAO, org.alfresco.repo.domain.node.NodeDAO)
     */
    @Override
    public void prepare(NamespaceService namespaceService, DictionaryService dictionaryService, QNameDAO qnameDAO, NodeDAO nodeDAO, TenantService tenantService, Set<String> selectors, Map<String, Argument> functionArgs,  FunctionEvaluationContext functionContext)
    {
        Function function = getFunction();
        if(function != null)
        {
            if(function instanceof DBQueryBuilderComponent)
            {
                DBQueryBuilderComponent dbQueryBuilderComponent = (DBQueryBuilderComponent)function;
                dbQueryBuilderComponent.prepare(namespaceService, dictionaryService, qnameDAO, nodeDAO, tenantService, selectors,  getFunctionArguments(), functionContext);
            }
            else
            {
                throw new UnsupportedOperationException();
            }
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#buildJoins(java.util.Map, java.util.List)
     */
    @Override
    public void buildJoins(Map<QName, DBQueryBuilderJoinCommand> singleJoins, List<DBQueryBuilderJoinCommand> multiJoins)
    {
        Function function = getFunction();
        if(function != null)
        {
            if(function instanceof DBQueryBuilderComponent)
            {
                DBQueryBuilderComponent dbQueryBuilderComponent = (DBQueryBuilderComponent)function;
                dbQueryBuilderComponent.buildJoins(singleJoins, multiJoins);
            }
            else
            {
                throw new UnsupportedOperationException();
            }
        }
        
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#buildPredicateCommands(java.util.List)
     */
    @Override
    public void buildPredicateCommands(List<DBQueryBuilderPredicatePartCommand> predicatePartCommands)
    {
        Function function = getFunction();
        if(function != null)
        {
            if(function instanceof DBQueryBuilderComponent)
            {
                DBQueryBuilderComponent dbQueryBuilderComponent = (DBQueryBuilderComponent)function;
                dbQueryBuilderComponent.buildPredicateCommands(predicatePartCommands);
            }
            else
            {
                throw new UnsupportedOperationException();
            }
        }
    }

}
