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

import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.search.impl.querymodel.Argument;
import org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext;
import org.alfresco.repo.search.impl.querymodel.QueryModelException;
import org.alfresco.repo.search.impl.querymodel.impl.db.DBQuery;
import org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent;
import org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderJoinCommand;
import org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderPredicatePartCommand;
import org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderPredicatePartCommandType;
import org.alfresco.repo.search.impl.querymodel.impl.db.ParentSupport;
import org.alfresco.repo.search.impl.querymodel.impl.functions.Child;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * @author Andy
 *
 */
public class DBChild extends Child implements DBQueryBuilderComponent
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
     * @see org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#prepare(org.alfresco.service.namespace.NamespaceService, org.alfresco.service.cmr.dictionary.DictionaryService, org.alfresco.repo.domain.qname.QNameDAO, org.alfresco.repo.domain.node.NodeDAO) */
    @Override
    public void prepare(NamespaceService namespaceService, DictionaryService dictionaryService, QNameDAO qnameDAO, NodeDAO nodeDAO, TenantService tenantService, Set<String> selectors, Map<String, Argument> functionArgs, FunctionEvaluationContext functionContext, boolean supportBooleanFloatAndDouble)
    {

        Argument argument = functionArgs.get(ARG_PARENT);
        String id = (String) argument.getValue(functionContext);
        argument = functionArgs.get(ARG_SELECTOR);
        if (argument != null)
        {
            String selector = (String) argument.getValue(functionContext);
            if (!selectors.contains(selector))
            {
                throw new QueryModelException("Unkown selector " + selector);
            }
        }
        else
        {
            if (selectors.size() > 1)
            {
                throw new QueryModelException("Selector must be specified for child constraint (IN_FOLDER) and join");
            }
        }
        ParentSupport parentSupport = new ParentSupport();
        parentSupport.setDbid(DBQuery.getDbid(id, nodeDAO, tenantService));
        parentSupport.setCommandType(DBQueryBuilderPredicatePartCommandType.EQUALS);
        builderSupport = parentSupport;
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
