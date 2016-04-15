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
package org.alfresco.repo.search.impl.lucene;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.search.impl.parsers.AlfrescoFunctionEvaluationContext;
import org.alfresco.repo.search.impl.parsers.FTSParser;
import org.alfresco.repo.search.impl.parsers.FTSQueryParser;
import org.alfresco.repo.search.impl.querymodel.Argument;
import org.alfresco.repo.search.impl.querymodel.Column;
import org.alfresco.repo.search.impl.querymodel.Constraint;
import org.alfresco.repo.search.impl.querymodel.Function;
import org.alfresco.repo.search.impl.querymodel.Order;
import org.alfresco.repo.search.impl.querymodel.Ordering;
import org.alfresco.repo.search.impl.querymodel.QueryEngine;
import org.alfresco.repo.search.impl.querymodel.QueryEngineResults;
import org.alfresco.repo.search.impl.querymodel.QueryModelFactory;
import org.alfresco.repo.search.impl.querymodel.QueryOptions;
import org.alfresco.repo.search.impl.querymodel.QueryOptions.Connective;
import org.alfresco.repo.search.impl.querymodel.impl.functions.PropertyAccessor;
import org.alfresco.repo.search.impl.querymodel.impl.functions.Score;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchParameters.SortDefinition;
import org.alfresco.service.cmr.search.SearchParameters.SortDefinition.SortType;
import org.alfresco.service.namespace.NamespacePrefixResolver;

/**
 * @author Andy
 *
 */
public abstract class AbstractAlfrescoFtsQueryLanguage extends AbstractLuceneQueryLanguage
{

    QueryEngine queryEngine;
    
    /**
     * Set the query engine
     * 
     * @param queryEngine QueryEngine
     */
    public void setQueryEngine(QueryEngine queryEngine)
    {
        this.queryEngine = queryEngine;
    }
    
    protected NamespacePrefixResolver getNamespacePrefixResolver(ADMLuceneSearcherImpl admLuceneSearcher)
    {
        return admLuceneSearcher.getNamespacePrefixResolver();   
    }
    
    protected DictionaryService getDictionaryService(ADMLuceneSearcherImpl admLuceneSearcher)
    {
        return admLuceneSearcher.getDictionaryService();
    }
    
    public ResultSet executeQuery(SearchParameters searchParameters, ADMLuceneSearcherImpl admLuceneSearcher)
    {
        String ftsExpression = searchParameters.getQuery();
        QueryModelFactory factory = queryEngine.getQueryModelFactory();
        AlfrescoFunctionEvaluationContext context = new AlfrescoFunctionEvaluationContext(getNamespacePrefixResolver(admLuceneSearcher), getDictionaryService(admLuceneSearcher),
                searchParameters.getNamespace());

        QueryOptions options = QueryOptions.create(searchParameters);

        FTSParser.Mode mode;

        if(options.getDefaultFTSConnective() == Connective.AND)
        {
            mode = FTSParser.Mode.DEFAULT_CONJUNCTION;
        }
        else
        {
            mode = FTSParser.Mode.DEFAULT_DISJUNCTION;
        }
            
        Constraint constraint = FTSQueryParser.buildFTS(ftsExpression, factory, context, null, null, mode, options.getDefaultFTSFieldConnective(),
                searchParameters.getQueryTemplates(), options.getDefaultFieldName(), FTSQueryParser.RerankPhase.SINGLE_PASS);
        org.alfresco.repo.search.impl.querymodel.Query query = factory.createQuery(null, null, constraint, buildOrderings(factory, searchParameters));

        QueryEngineResults results = queryEngine.executeQuery(query, options, context);
        ResultSet resultSet = results.getResults().values().iterator().next();
        return resultSet;
    }

    public List<Ordering> buildOrderings(QueryModelFactory factory, SearchParameters searchParameters)
    {
        List<Ordering> orderings = new ArrayList<Ordering>(searchParameters.getSortDefinitions().size());
        for (SortDefinition sd : searchParameters.getSortDefinitions())
        {
            if (sd.getSortType() == SortType.FIELD)
            {
                Function function = factory.getFunction(PropertyAccessor.NAME);
                Argument arg = factory.createPropertyArgument(PropertyAccessor.ARG_PROPERTY, true, true, "", sd.getField());
                Map<String, Argument> functionArguments = new LinkedHashMap<String, Argument>();
                functionArguments.put(arg.getName(), arg);
                Column column = factory.createColumn(function, functionArguments, sd.getField());

                Order order = sd.isAscending() ? Order.ASCENDING : Order.DESCENDING;

                Ordering ordering = factory.createOrdering(column, order);
                
                orderings.add(ordering);
            }
            else  if (sd.getSortType() == SortType.SCORE)
            {
                Function function = factory.getFunction(Score.NAME);
                Map<String, Argument> functionArguments = new LinkedHashMap<String, Argument>();
                Column column = factory.createColumn(function, functionArguments, Score.NAME);
                Order order = sd.isAscending() ? Order.ASCENDING : Order.DESCENDING;

                Ordering ordering = factory.createOrdering(column, order);
                
                orderings.add(ordering);
            }
            else
            {
                throw new UnsupportedOperationException("Unsupported Ordering "+sd.getSortType());
            }
        }
        return orderings;
    }
}
