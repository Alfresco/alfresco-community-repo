package org.alfresco.repo.search.impl.lucene;

import java.util.HashMap;
import java.util.List;

import org.alfresco.repo.search.impl.parsers.AlfrescoFunctionEvaluationContext;
import org.alfresco.repo.search.impl.parsers.FTSQueryParser;
import org.alfresco.repo.search.impl.querymodel.Constraint;
import org.alfresco.repo.search.impl.querymodel.QueryEngine;
import org.alfresco.repo.search.impl.querymodel.QueryEngineResults;
import org.alfresco.repo.search.impl.querymodel.QueryModelFactory;
import org.alfresco.repo.search.impl.querymodel.QueryOptions;
import org.alfresco.repo.search.impl.querymodel.QueryOptions.Connective;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;

/**
 * Alfresco FTS Query language support
 * @author andyh
 *
 */
public class LuceneAlfrescoFtsQueryLanguage implements LuceneQueryLanguageSPI
{
    QueryEngine queryEngine;

    /**
     * Set the query engine
     * @param queryEngine
     */
    public void setQueryEngine(QueryEngine queryEngine)
    {
        this.queryEngine = queryEngine;
    }

    public ResultSet executQuery(SearchParameters searchParameters, ADMLuceneSearcherImpl admLuceneSearcher)
    {
        String ftsExpression = searchParameters.getQuery();
        QueryModelFactory factory = queryEngine.getQueryModelFactory();
        AlfrescoFunctionEvaluationContext context = new AlfrescoFunctionEvaluationContext(admLuceneSearcher.getNamespacePrefixResolver(), admLuceneSearcher.getDictionaryService(), searchParameters.getNamespace());

        QueryOptions options = new QueryOptions(searchParameters.getQuery(), null);
        options.setFetchSize(searchParameters.getBulkFecthSize());
        options.setIncludeInTransactionData(!searchParameters.excludeDataInTheCurrentTransaction());
        options.setDefaultFTSConnective(searchParameters.getDefaultOperator() == SearchParameters.Operator.OR ? Connective.OR : Connective.AND);
        options.setDefaultFTSFieldConnective(searchParameters.getDefaultOperator() == SearchParameters.Operator.OR ? Connective.OR : Connective.AND);
        options.setSkipCount(searchParameters.getSkipCount());
        options.setMaxPermissionChecks(searchParameters.getMaxPermissionChecks());
        options.setMaxPermissionCheckTimeMillis(searchParameters.getMaxPermissionCheckTimeMillis());
        if (searchParameters.getLimitBy() == LimitBy.FINAL_SIZE)
        {
            options.setMaxItems(searchParameters.getLimit());
        }
        else
        {
            options.setMaxItems(searchParameters.getMaxItems());
        }
        options.setMlAnalaysisMode(searchParameters.getMlAnalaysisMode());
        options.setLocales(searchParameters.getLocales());
        options.setStores(searchParameters.getStores());

        Constraint constraint = FTSQueryParser.buildFTS(ftsExpression, factory, context, null, null, options.getDefaultFTSConnective(), options.getDefaultFTSFieldConnective(),
                searchParameters.getQueryTemplates());
        org.alfresco.repo.search.impl.querymodel.Query query = factory.createQuery(null, null, constraint, null);

        QueryEngineResults results = queryEngine.executeQuery(query, options, context);
        return results.getResults().values().iterator().next();
    }

    public String getName()
    {
      return SearchService.LANGUAGE_FTS_ALFRESCO;
    }

    public void setFactories(List<AbstractLuceneIndexerAndSearcherFactory> factories)
    {
        for (AbstractLuceneIndexerAndSearcherFactory factory : factories)
        {
            factory.registerQueryLanguage(this);
        }
    }

}
