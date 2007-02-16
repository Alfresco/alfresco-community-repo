/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.service.cmr.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.alfresco.repo.search.MLAnalysisMode;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;

/**
 * This class provides parameters to define a search. TODO - paging of results page number and page size - paging
 * isolation - REPEATABLE READ, READ COMMITTED, may SEE ONCE tracking node refs in previous result sets - how long
 * repeatable read may be held - limit by the number of permission evaluations
 * 
 * @author Andy Hind
 */
public class SearchParameters extends SearchStatement
{
    /*
     * The default limit if someone asks for a limited result set but does not say how to limit....
     */
    private static int DEFAULT_LIMIT = 500;

    /*
     * Standard sort definitions for sorting in document and score order.
     */
    public static final SortDefinition SORT_IN_DOCUMENT_ORDER_ASCENDING = new SortDefinition(
            SortDefinition.SortType.DOCUMENT, null, true);

    public static final SortDefinition SORT_IN_DOCUMENT_ORDER_DESCENDING = new SortDefinition(
            SortDefinition.SortType.DOCUMENT, null, false);

    public static final SortDefinition SORT_IN_SCORE_ORDER_ASCENDING = new SortDefinition(
            SortDefinition.SortType.SCORE, null, false);

    public static final SortDefinition SORT_IN_SCORE_ORDER_DESCENDING = new SortDefinition(
            SortDefinition.SortType.SCORE, null, true);

    /**
     * An emum defining if the default action is to "and" or "or" unspecified components in the query register. Not all
     * search implementations will support this.
     */
    public enum Operator
    {
        OR, AND
    }

    /*
     * Expose as constants
     */
    public static final Operator OR = Operator.OR;

    public static final Operator AND = Operator.AND;

    /*
     * The parameters that can be set
     */
    private ArrayList<StoreRef> stores = new ArrayList<StoreRef>(1);

    private ArrayList<Path> attributePaths = new ArrayList<Path>(1);

    private ArrayList<QueryParameterDefinition> queryParameterDefinitions = new ArrayList<QueryParameterDefinition>(1);

    private boolean excludeDataInTheCurrentTransaction = false;

    private ArrayList<SortDefinition> sortDefinitions = new ArrayList<SortDefinition>(1);

    private Operator defaultOperator = Operator.OR;

    private ArrayList<Locale> locales = new ArrayList<Locale>();

    private MLAnalysisMode mlAnalaysisMode = null; // Pick up from config if null

    private LimitBy limitBy = LimitBy.UNLIMITED;

    private PermissionEvaluationMode permissionEvaluation = PermissionEvaluationMode.EAGER;

    private int limit = DEFAULT_LIMIT;
    
    private HashSet<String> allAttributes = new HashSet<String>();
    
    private HashSet<String> textAttributes = new HashSet<String>();

    /**
     * Default constructor
     */
    public SearchParameters()
    {
        super();
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
     * Add paths for attributes in the result set. Generally this only makes sense for disconnected results sets. These
     * atttributes/paths state what must be present in the result set, akin to the selection of columns is sql.
     * 
     * @param attributePath
     */
    public void addAttrbutePath(Path attributePath)
    {
        attributePaths.add(attributePath);
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
     *            this is intially a direct attribute on a node not an attribute on the parent etc TODO: It could be a
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
     * Get the list of attribute paths that are guarenteed to be in the result set.
     * 
     * @return
     */
    public ArrayList<Path> getAttributePaths()
    {
        return attributePaths;
    }

    /**
     * Is data in the current transaction excluded from the search.
     * 
     * @return
     */
    public boolean excludeDataInTheCurrentTransaction()
    {
        return excludeDataInTheCurrentTransaction;
    }

    /**
     * Get the query parameters that apply to this query.
     * 
     * @return
     */
    public ArrayList<QueryParameterDefinition> getQueryParameterDefinitions()
    {
        return queryParameterDefinitions;
    }

    /**
     * Get the sort definitions that apply to this query.
     * 
     * @return
     */
    public ArrayList<SortDefinition> getSortDefinitions()
    {
        return sortDefinitions;
    }

    /**
     * Get the stores in which this query should find results.
     * 
     * @return
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
        this.defaultOperator = defaultOperator;
    }

    /**
     * Get the default operator for query elements when they are not explicit in the query.
     * 
     * @return
     */
    public Operator getDefaultOperator()
    {
        return defaultOperator;
    }

    /**
     * Get how the result set should be limited
     * 
     * @return
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
     * @return
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
     * @return
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
     * The way in which multilingual fields are treated durig a search.
     * By default, only the specified locale is used and it must be an exact match.
     * 
     * @return
     */
    public MLAnalysisMode getMlAnalaysisMode()
    {
        return mlAnalaysisMode;
    }

    /**
     * Set the way in which multilingual fields are treated durig a search.
     * This controls in which locales an multilingual fields will match.
     * 
     * @param mlAnalaysisMode
     */
    public void setMlAnalaysisMode(MLAnalysisMode mlAnalaysisMode)
    {
        this.mlAnalaysisMode = mlAnalaysisMode;
    }

    /**
     * Add a locale to include for multi-lingual text searches.
     * If non are set, the default is to use the user's locale.
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
     * @return
     */
    public List<Locale> getLocales()
    {
        return Collections.unmodifiableList(locales);
    }
    
    
    

    /**
     * Add a locale to include for multi-lingual text searches.
     * If non are set, the default is to use the user's locale.
     *  
     * @param locale
     */
    public void addTextAttribute(String attribute)
    {
        textAttributes.add(attribute);
    }

    /**
     * Get the locales used for multi-lingual text searches.
     * 
     * @return
     */
    public Set<String> getTextAttributes()
    {
        return Collections.unmodifiableSet(textAttributes);
    }
    
    /**
     * Add a locale to include for multi-lingual text searches.
     * If non are set, the default is to use the user's locale.
     *  
     * @param locale
     */
    public void addAllAttribute(String attribute)
    {
        allAttributes.add(attribute);
    }

    /**
     * Get the locales used for multi-lingual text searches.
     * 
     * @return
     */
    public Set<String> getAllAttributes()
    {
        return Collections.unmodifiableSet(allAttributes);
    }
    

    /**
     * A helper class for sort definition. Encapsulated using the lucene sortType, field name and a flag for
     * ascending/descending.
     * 
     * @author Andy Hind
     */
    public static class SortDefinition
    {

        public enum SortType
        {
            FIELD, DOCUMENT, SCORE
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

        public boolean isAscending()
        {
            return ascending;
        }

        public String getField()
        {
            return field;
        }

        public SortType getSortType()
        {
            return sortType;
        }

    }
}
