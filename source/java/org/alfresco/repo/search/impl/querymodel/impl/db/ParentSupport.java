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
import org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

public class ParentSupport implements DBQueryBuilderComponent
{
    Long dbid;

    Long[] dbids;
    
    String alias;
    
    DBQueryBuilderPredicatePartCommandType commandType;

    private boolean leftOuter;

    /**
     * @param dbid
     *            the dbid to set
     */
    public void setDbid(Long dbid)
    {
        this.dbid = dbid;
    }
    
    /**
     * @param dbids the dbids to set
     */
    public void setDbids(Long[] dbids)
    {
        this.dbids = dbids;
    }

    /**
     * @param commandType
     *            the commandType to set
     */
    public void setCommandType(DBQueryBuilderPredicatePartCommandType commandType)
    {
        this.commandType = commandType;
    }
    

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#isSupported()
     */
    @Override
    public boolean isSupported()
    {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#prepare(org.alfresco.service.namespace
     * .NamespaceService, org.alfresco.service.cmr.dictionary.DictionaryService,
     * org.alfresco.repo.domain.qname.QNameDAO, org.alfresco.repo.domain.node.NodeDAO, java.util.Set, java.util.Map,
     * org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext)
     */
    @Override
    public void prepare(NamespaceService namespaceService, DictionaryService dictionaryService, QNameDAO qnameDAO, NodeDAO nodeDAO, TenantService tenantService, Set<String> selectors,
            Map<String, Argument> functionArgs, FunctionEvaluationContext functionContext, boolean supportBooleanFloatAndDouble)
    {
      
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#buildJoins(java.util.Map,
     * java.util.List)
     */
    @Override
    public void buildJoins(Map<QName, DBQueryBuilderJoinCommand> singleJoins, List<DBQueryBuilderJoinCommand> multiJoins)
    {
        alias = "PARENT";
        if(commandType == DBQueryBuilderPredicatePartCommandType.ORDER)
        {
            DBQueryBuilderJoinCommand join = new DBQueryBuilderJoinCommand();
            alias = "PARENT_" + multiJoins.size();
            join.setAlias(alias);
            join.setOuter(leftOuter);
            join.setType(DBQueryBuilderJoinCommandType.PARENT);
            multiJoins.add(join);
        }

    }

    /*
     * (non-Javadoc)
     * @see
     * org.alfresco.repo.search.impl.querymodel.impl.db.DBQueryBuilderComponent#buildPredicateCommands(java.util
     * .List)
     */
    @Override
    public void buildPredicateCommands(List<DBQueryBuilderPredicatePartCommand> predicatePartCommands)
    {
        DBQueryBuilderPredicatePartCommand command = new DBQueryBuilderPredicatePartCommand();
        command.setJoinCommandType(DBQueryBuilderJoinCommandType.PARENT);
        command.setAlias(alias);
        command.setType(commandType);
        command.setValue(dbid);
        command.setValues(dbids);
        command.setFieldName("parent_node_id");
        predicatePartCommands.add(command);

    }

    /**
     * @param leftOuter boolean
     */
    public void setLeftOuter(boolean leftOuter)
    {
        this.leftOuter = leftOuter;
    }

}