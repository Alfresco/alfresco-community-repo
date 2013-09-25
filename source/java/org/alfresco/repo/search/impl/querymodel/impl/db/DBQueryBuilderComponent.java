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
import org.alfresco.repo.search.impl.querymodel.FunctionEvaluationContext;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Build the commands required to generate the dynamic SQL
 * This is independent of the data base schema. 
 * 
 * @author Andy
 *
 */
public interface DBQueryBuilderComponent
{
    /**
     * Is this component supported in a DB query?
     * @return
     */
    public boolean isSupported();
    
    /**
     * Use the dictionary to expand any terms, deal with multi-valued properties, etc
     * Use the QNameDAO to look up any ids
     * @param functionArgs 
     */
    public void prepare(NamespaceService namespaceService, DictionaryService dictionaryService, QNameDAO qnameDAO, NodeDAO nodeDAO, TenantService tenantService, Set<String> selectors, Map<String, Argument> functionArgs,  FunctionEvaluationContext functionContext);
    
    /**
     * Build the Set of required joins 
     * Assign join aliases and link them up to each component where required
     */
    public void buildJoins(Map<QName, DBQueryBuilderJoinCommand> singleJoins, List<DBQueryBuilderJoinCommand> multiJoins);
    
    /**
     * Add to the list of commands used to build the SQL predicate
     */
    public void buildPredicateCommands(List<DBQueryBuilderPredicatePartCommand> predicatePartCommands);
}
