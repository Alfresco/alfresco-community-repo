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
import org.alfresco.repo.search.impl.querymodel.Constraint;
import org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext;
import org.alfresco.repo.search.impl.querymodel.QueryModelException;
import org.alfresco.repo.search.impl.querymodel.impl.BaseConjunction;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * @author Andy
 *
 */
public class DBConjunction extends BaseConjunction implements DBQueryBuilderComponent
{

    /**
     */
    public DBConjunction(List<Constraint> constraints)
    {
        super(constraints);
    }

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
        for (Constraint constraint : getConstraints())
        {
            if (constraint instanceof DBQueryBuilderComponent)
            {
                if (constraint.getOccur() == Occur.OPTIONAL)
                {
                    throw new QueryModelException("Disjunctions are not suported");
                }
                DBQueryBuilderComponent dbQueryBuilderComponent = (DBQueryBuilderComponent) constraint;
                dbQueryBuilderComponent.prepare(namespaceService, dictionaryService, qnameDAO, nodeDAO, tenantService, selectors, functionArgs, functionContext, supportBooleanFloatAndDouble);
            }
            else
            {
                throw new UnsupportedOperationException();
            }
        }
    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#buildJoins(java.util.Map, java.util.List) */
    @Override
    public void buildJoins(Map<QName, DBQueryBuilderJoinCommand> singleJoins, List<DBQueryBuilderJoinCommand> multiJoins)
    {
        for (Constraint constraint : getConstraints())
        {
            if (constraint instanceof DBQueryBuilderComponent)
            {
                DBQueryBuilderComponent dbQueryBuilderComponent = (DBQueryBuilderComponent) constraint;
                dbQueryBuilderComponent.buildJoins(singleJoins, multiJoins);
            }
            else
            {
                throw new UnsupportedOperationException();
            }
        }

    }

    /* (non-Javadoc)
     * 
     * @see org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#buildPredicateCommands(java.util.List) */
    @Override
    public void buildPredicateCommands(List<DBQueryBuilderPredicatePartCommand> predicatePartCommands)
    {
        DBQueryBuilderPredicatePartCommand open = new DBQueryBuilderPredicatePartCommand();
        open.setType(DBQueryBuilderPredicatePartCommandType.OPEN);
        predicatePartCommands.add(open);

        boolean requiresAnd = false;

        for (Constraint constraint : getConstraints())
        {
            if (constraint instanceof DBQueryBuilderComponent)
            {
                if (requiresAnd)
                {
                    DBQueryBuilderPredicatePartCommand and = new DBQueryBuilderPredicatePartCommand();
                    and.setType(DBQueryBuilderPredicatePartCommandType.AND);
                    predicatePartCommands.add(and);
                }
                else
                {
                    requiresAnd = true;
                }
                if (constraint.getOccur() == Occur.EXCLUDE)
                {
                    DBQueryBuilderPredicatePartCommand not = new DBQueryBuilderPredicatePartCommand();
                    not.setType(DBQueryBuilderPredicatePartCommandType.NOT);
                    predicatePartCommands.add(not);
                }
                DBQueryBuilderComponent dbQueryBuilderComponent = (DBQueryBuilderComponent) constraint;
                dbQueryBuilderComponent.buildPredicateCommands(predicatePartCommands);
            }
            else
            {
                throw new UnsupportedOperationException();
            }
        }

        DBQueryBuilderPredicatePartCommand close = new DBQueryBuilderPredicatePartCommand();
        close.setType(DBQueryBuilderPredicatePartCommandType.CLOSE);
        predicatePartCommands.add(close);

    }

}
