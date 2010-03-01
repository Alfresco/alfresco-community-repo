/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.service.cmr.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.search.MLAnalysisMode;
import org.alfresco.repo.search.impl.querymodel.QueryOptions;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;

/**
 * This class provides parameters to define a search. TODO - paging of results page number and page size - paging
 * isolation - REPEATABLE READ, READ COMMITTED, may SEE ONCE tracking node refs in previous result sets - how long
 * repeatable read may be held - limit by the number of permission evaluations
 * 
 * @author Andy Hind
 */
public class SearchParameters
{
    /*
     * The default limit if someone asks for a limited result set but does not say how to limit....
     */
    private static int DEFAULT_LIMIT = 500;

    private static int DEFAULT_BULK_FETCH_SIZE = 1000;

    /*
     * Standard sort definitions for sorting in document and score order.
     */
    /**
     * Sort in the order docs were added to the index - oldest docs first
     */
    public static final SortDefinition SORT_IN_DOCUMENT_ORDER_ASCENDING = new SortDefinition(SortDefinition.SortType.DOCUMENT, null, true);

    /**
     * Sort in the reverse order docs were added to the index - new/updateed docs first
     */
    public static final SortDefinition SORT_IN_DOCUMENT_ORDER_DESCENDING = new SortDefinition(SortDefinition.SortType.DOCUMENT, null, false);

    /**
     * Sort in ascending score
     */
    public static final SortDefinition SORT_IN_SCORE_ORDER_ASCENDING = new SortDefinition(SortDefinition.SortType.SCORE, null, false);

    /**
     * Sort in descending score order
     */
    public static final SortDefinition SORT_IN_SCORE_ORDER_DESCENDING = new SortDefinition(SortDefinition.SortType.SCORE, null, true);

    /**
     * An emum defining if the default action is to "and" or "or" unspecified components in the query register. Not all
     * search implementations will support this.
     */
    public enum Operator
    {
        /**
         * OR
         */
        OR,
        /**
         * AND
         */
        AND
    }

    /*
     * Expose as constants
     */
    /**
     * OR
     */
    public static final Operator OR = Operator.OR;

    /**
     * AND
     */
    public static final Operator AND = Operator.AND;

    /*
     * The parameters that can be set
     */
    private String language;

    private String query;

    private ArrayList<StoreRef> stores = new ArrayList<StoreRef>(1);

    private ArrayList<QueryParameterDefinition> queryParameterDefinitions = new ArrayList<QueryParameterDefinition>(1);

    private boolean excludeDataInTheCurrentTransaction = false;

    private ArrayList<SortDefinition> sortDefinitions = new ArrayList<SortDefinition>(1);

    private ArrayList<Locale> locales = new ArrayList<Locale>();

    private MLAnalysisMode mlAnalaysisMode = null; // Pick up from config if null

    private LimitBy limitBy = LimitBy.UNLIMITED;

    private PermissionEvaluationMode permissionEvaluation = PermissionEvaluationMode.EAGER;

    private int limit = DEFAULT_LIMIT;

    private HashSet<String> allAttributes = new HashSet<String>();

    private HashSet<String> textAttributes = new HashSet<String>();

    private boolean bulkFetch = true;

    private int bulkFetchSize = DEFAULT_BULK_FETCH_SIZE;

    private int maxItems = -1;

    private int skipCount = 0;

    private Operator defaultFTSOperator = Operator.OR;

    private Operator defaultFTSFieldOperator = Operator.OR;

    private Map<String, String> queryTemplates = new HashMap<String, String>();

    private String namespace = NamespaceService.CONTENT_MODEL_1_0_URI;

    // By default uses the central config
    private int maxPermissionChecks = -1;

    // By default uses the central config
    private long maxPermissionCheckTimeMillis = -1;

    private String defaultFieldName = "TEXT";

    /**
     * Default constructor
     */
    public SearchParameters()
    {
        super();
    }

    /**
     * Construct from Query Options
     * 
     * @param options
     */
    public SearchParameters(QueryOptions options)
    {
        setBulkFetch(options.getFetchSize() > 0);
        setBulkFetchSize(options.getFetchSize());
        setSkipCount(options.getSkipCount());
        setMaxPermissionChecks(options.getMaxPermissionChecks());
        setMaxPermissionCheckTimeMillis(options.getMaxPermissionCheckTimeMillis());
        if (options.getMaxItems() >= 0)
        {
            setLimitBy(LimitBy.FINAL_SIZE);
            setLimit(options.getMaxItems());
            setMaxItems(options.getMaxItems());
        }
        else
        {
            setLimitBy(LimitBy.UNLIMITED);
        }
    }
    
    /**
     * Get the search language
     * 
     * @return - string id of search language
     */
    public String getLanguage()
    {
        return language;
    }

    /**
     * Get the query.
     * 
     * @return - the query string
     */
    public String getQuery()
    {
        return query;
    }

    /**
     * Set the query language.
     * 
     * @param language -
     *            the query language.
     */
    public void setLanguage(String language)
    {
        this.language = language;
    }

    /**
     * Set the query string.
     * 
     * @param query -
     *            the query string.
     */
    public void setQuery(String query)
    {
        this.query = query;
    }

    /**
     * Set the stores to be supported - currently there can be only one. Searching across multiple stores is on the todo
     * list.
     * 
     * @param store
     */
    public void addStore(StoreRef store)
    {
        if (stores.size() != 0)
        {
            throw new IllegalStateException("At the moment, there can only be one store set for the search");
        }
        stores.add(store);
    }

    /**
     * Add parameter definitions for the query - used to parameterise the query string
     * 
     * @param queryParameterDefinition
     */
    public void addQueryParameterDefinition(QueryParameterDefinition queryParameterDefinition)
    {
        queryParameterDefinitions.add(queryParameterDefinition);
    }

    /**
     * If true, any data in the current transaction will be ignored in the search. You will not see anything you have
     * added in the current transaction. By default you will see data in the current transaction. This effectively gives
     * read committed isolation. There is a performance overhead for this, at least when using lucene. This flag may be
     * set to avoid that performance hit if you know you do not want to find results that are yet to be committed (this
     * includes creations, deletions and updates)
     * 
     * @param excludeDataInTheCurrentTransaction
     */
    public void excludeDataInTheCurrentTransaction(boolean excludeDataInTheCurrentTransaction)
    {
        this.excludeDataInTheCurrentTransaction = excludeDataInTheCurrentTransaction;
    }

    /**
     * Add a sort to the query (for those query languages that do not support it directly) The first sort added is
     * treated as primary, the second as secondary etc. A helper method to create SortDefinitions.
     * 
     * @param field -
     *            this is initially a direct attribute on a node not an attribute on the parent etc TODO: It could be a
     *            relative path at some time.
     * @param ascending -
     *            true to sort ascending, false for descending.
     */
    public void addSort(String field, boolean ascending)
    {
        addSort(new SortDefinition(SortDefinition.SortType.FIELD, field, ascending));
    }

    /**
     * Add a sort definition.
     * 
     * @param sortDefinition -
     *            the sort definition to add. Use the static member variables for sorting in score and index order.
     */
    public void addSort(SortDefinition sortDefinition)
    {
        sortDefinitions.add(sortDefinition);
    }

    /**
     * Is data in the current transaction excluded from the search.
     * 
     * @return - true if data in the current transaction is ignored
     */
    public boolean excludeDataInTheCurrentTransaction()
    {
        return excludeDataInTheCurrentTransaction;
    }

    /**
     * Get the query parameters that apply to this query.
     * 
     * @return - the parameter
     */
    public ArrayList<QueryParameterDefinition> getQueryParameterDefinitions()
    {
        return queryParameterDefinitions;
    }

    /**
     * Get the sort definitions that apply to this query.
     * 
     * @return - the sort definitions
     */
    public ArrayList<SortDefinition> getSortDefinitions()
    {
        return sortDefinitions;
    }

    /**
     * Get the stores in which this query should find results.
     * 
     * @return - the list of stores
     */
    public ArrayList<StoreRef> getStores()
    {
        return stores;
    }

    /**
     * Set the default operator for query elements when they are not explicit in the query.
     * 
     * @param defaultOperator
     */
    public void setDefaultOperator(Operator defaultOperator)
    {
        this.defaultFTSOperator = defaultOperator;
        this.defaultFTSFieldOperator = defaultOperator;
    }

    /**
     * Get the default operator for query elements when they are not explicit in the query.
     * 
     * @return the default operator
     */
    public Operator getDefaultOperator()
    {
        return getDefaultFTSOperator();
    }

    /**
     * Get how the result set should be limited
     * 
     * @return how the result set will be or was limited
     */
    public LimitBy getLimitBy()
    {
        return limitBy;
    }

    /**
     * Set how the result set should be limited.
     * 
     * @param limitBy
     */
    public void setLimitBy(LimitBy limitBy)
    {
        this.limitBy = limitBy;
    }

    /**
     * Get when permissions are evaluated.
     * 
     * @return - how permissions are evaluated
     */
    public PermissionEvaluationMode getPermissionEvaluation()
    {
        return permissionEvaluation;
    }

    /**
     * Set when permissions are evaluated.
     * 
     * @param permissionEvaluation
     */
    public void setPermissionEvaluation(PermissionEvaluationMode permissionEvaluation)
    {
        this.permissionEvaluation = permissionEvaluation;
    }

    /**
     * If limiting the result set in some way, get the limiting value used.
     * 
     * @return the limit
     */
    public int getLimit()
    {
        return limit;
    }

    /**
     * If limiting the result set in some way, set the limiting value used.
     * 
     * @param limit
     */
    public void setLimit(int limit)
    {
        this.limit = limit;
    }

    /**
     * The way in which multilingual fields are treated durig a search. By default, only the specified locale is used
     * and it must be an exact match.
     * 
     * @return - how locale related text is tokenised
     */
    public MLAnalysisMode getMlAnalaysisMode()
    {
        return mlAnalaysisMode;
    }

    /**
     * Set the way in which multilingual fields are treated durig a search. This controls in which locales an
     * multilingual fields will match.
     * 
     * @param mlAnalaysisMode
     */
    public void setMlAnalaysisMode(MLAnalysisMode mlAnalaysisMode)
    {
        this.mlAnalaysisMode = mlAnalaysisMode;
    }

    /**
     * Add a locale to include for multi-lingual text searches. If non are set, the default is to use the user's locale.
     * 
     * @param locale
     */
    public void addLocale(Locale locale)
    {
        locales.add(locale);
    }

    /**
     * Get the locales used for multi-lingual text searches.
     * 
     * @return - the locales
     */
    public List<Locale> getLocales()
    {
        return Collections.unmodifiableList(locales);
    }

    /**
     * Add a field for TEXT expansion
     * 
     * @param attribute -
     *            field/attribute in the index
     */
    public void addTextAttribute(String attribute)
    {
        textAttributes.add(attribute);
    }

    /**
     * Get the text attributes used for text expansion.
     * 
     * @return the text attributes used for text expansion
     */
    public Set<String> getTextAttributes()
    {
        return Collections.unmodifiableSet(textAttributes);
    }

    /**
     * Add a field for ALL expansion
     * 
     * @param attribute -
     *            field/attribute in the index
     */
    public void addAllAttribute(String attribute)
    {
        allAttributes.add(attribute);
    }

    /**
     * Get the text attributes used for ALL expansion.
     * 
     * @return the text attributes used for ALL expansion
     */
    public Set<String> getAllAttributes()
    {
        return Collections.unmodifiableSet(allAttributes);
    }

    /**
     * Bulk fetch results in the cache
     * 
     * @param bulkFetch
     */
    public void setBulkFetch(boolean bulkFetch)
    {
        this.bulkFetch = bulkFetch;
    }

    /**
     * Do we bulk fetch
     * 
     * @return - true if we do
     */
    public boolean getBulkFetch()
    {
        return bulkFetch;
    }

    /**
     * Set the bulk fetch size
     * 
     * @param bulkFetchSize
     */
    public void setBulkFetchSize(int bulkFetchSize)
    {
        this.bulkFetchSize = bulkFetchSize;
    }

    /**
     * Get the bulk fetch size.
     * 
     * @return the fetch size
     */
    public int getBulkFecthSize()
    {
        return bulkFetchSize;
    }

    /**
     * Get the max number of rows for the result set 0 or less is unlimited
     * 
     * @return the maxItems
     */
    public int getMaxItems()
    {
        return maxItems;
    }

    /**
     * Set the max number of rows for the result set 0 or less is unlimited
     * 
     * @param maxItems
     *            the maxItems to set
     */
    public void setMaxItems(int maxItems)
    {
        this.maxItems = maxItems;
    }

    /**
     * Get the skip count - the number of rows to skip at the start of the query.
     * 
     * @return the skipCount
     */
    public int getSkipCount()
    {
        return skipCount;
    }

    /**
     * Set the skip count - the number of rows to skip at the start of the query.
     * 
     * @param skipCount
     *            the skipCount to set
     */
    public void setSkipCount(int skipCount)
    {
        this.skipCount = skipCount;
    }

    /**
     * Get the default connective used when OR and AND are not specified for the FTS contains() function.
     * 
     * @return the defaultFTSConnective
     */
    public Operator getDefaultFTSOperator()
    {
        return defaultFTSOperator;
    }

    /**
     * Set the default connective used when OR and AND are not specified for the FTS contains() function.
     * 
     * @param defaultFTSOperator
     *            the defaultFTSOperator to set
     */
    public void setDefaultFTSOperator(Operator defaultFTSOperator)
    {
        this.defaultFTSOperator = defaultFTSOperator;
    }

    /**
     * As getDefaultFTSConnective() but for field groups
     * 
     * @return the defaultFTSFieldConnective
     */
    public Operator getDefaultFTSFieldOperator()
    {
        return defaultFTSFieldOperator;
    }

    /**
     * As setDefaultFTSConnective() but for field groups
     * 
     * @param defaultFTSFieldOperator
     *            the defaultFTSFieldOperator to set
     */
    public void setDefaultFTSFieldConnective(Operator defaultFTSFieldOperator)
    {
        this.defaultFTSFieldOperator = defaultFTSFieldOperator;
    }

    /**
     * Get the default namespace.
     * 
     * @return the default namspace uri or prefix.
     */
    public String getNamespace()
    {
        return namespace;
    }

    /**
     * Set the default namespace
     * 
     * @param namespace -
     *            the uri or prefix for the default namespace.
     */
    public void setNamespace(String namespace)
    {
        this.namespace = namespace;
    }

    /**
     * Get the query templates
     * 
     * @return - the query templates
     */
    public Map<String, String> getQueryTemplates()
    {
        return queryTemplates;
    }

    /**
     * Add/replace a query template Not all languages support query templates
     * 
     * @param name
     * @param template
     * @return any removed template or null
     */
    public String addQueryTemplate(String name, String template)
    {
        return queryTemplates.put(name, template);
    }

    public long getMaxPermissionCheckTimeMillis()
    {
        return maxPermissionCheckTimeMillis;
    }

    public void setMaxPermissionCheckTimeMillis(long maxPermissionCheckTimeMillis)
    {
        this.maxPermissionCheckTimeMillis = maxPermissionCheckTimeMillis;
    }

    public int getMaxPermissionChecks()
    {
        return maxPermissionChecks;
    }

    public void setMaxPermissionChecks(int maxPermissionChecks)
    {
        this.maxPermissionChecks = maxPermissionChecks;
    }

    public int getBulkFetchSize()
    {
        return bulkFetchSize;
    }

    /**
     * A helper class for sort definition. Encapsulated using the lucene sortType, field name and a flag for
     * ascending/descending.
     * 
     * @author Andy Hind
     */
    public static class SortDefinition
    {

        /**
         * What is used for the sort
         * 
         * @author andyh
         */
        public enum SortType
        {
            /**
             * A Field
             */
            FIELD,
            /**
             * Doc number
             */
            DOCUMENT,
            /**
             * Score
             */
            SCORE
        };

        SortType sortType;

        String field;

        boolean ascending;

        SortDefinition(SortType sortType, String field, boolean ascending)
        {
            this.sortType = sortType;
            this.field = field;
            this.ascending = ascending;
        }

        /**
         * Is ascending
         * 
         * @return true if ascending
         */
        public boolean isAscending()
        {
            return ascending;
        }

        /**
         * Field
         * 
         * @return - the field
         */
        public String getField()
        {
            return field;
        }

        /**
         * What is used for the sort
         * 
         * @return sort type
         */
        public SortType getSortType()
        {
            return sortType;
        }

    }

    /**
     * @return the default field name
     */
    public String getDefaultFieldName()
    {
        return defaultFieldName;
    }
    
    /**
     * @param defaultFieldName - the default field name to use
     */
    public void setDefaultFieldName(String defaultFieldName)
    {
       this.defaultFieldName = defaultFieldName;
    }

    @Override
    public String toString()
    {
        return "SearchParameters [allAttributes="
                + allAttributes + ", bulkFetch=" + bulkFetch + ", bulkFetchSize=" + bulkFetchSize + ", defaultFTSFieldOperator=" + defaultFTSFieldOperator
                + ", defaultFTSOperator=" + defaultFTSOperator + ", defaultFieldName=" + defaultFieldName + ", excludeDataInTheCurrentTransaction="
                + excludeDataInTheCurrentTransaction + ", language=" + language + ", limit=" + limit + ", limitBy=" + limitBy + ", locales=" + locales + ", maxItems=" + maxItems
                + ", maxPermissionCheckTimeMillis=" + maxPermissionCheckTimeMillis + ", maxPermissionChecks=" + maxPermissionChecks + ", mlAnalaysisMode=" + mlAnalaysisMode
                + ", namespace=" + namespace + ", permissionEvaluation=" + permissionEvaluation + ", query=" + query + ", queryParameterDefinitions=" + queryParameterDefinitions
                + ", queryTemplates=" + queryTemplates + ", skipCount=" + skipCount + ", sortDefinitions=" + sortDefinitions + ", stores=" + stores + ", textAttributes="
                + textAttributes + "]";
    }

}
