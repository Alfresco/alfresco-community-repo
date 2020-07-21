/*
 * #%L
 * Alfresco Data model classes
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
package org.alfresco.service.cmr.search;

import java.util.List;
import java.util.Map;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;

/**
 * This is the common interface for both row (Alfresco node) and column (CMIS style property or function) based results.
 * The meta-data for the results sets contains the detailed info on what columns are available. For row based result
 * sets there is no selector - all the nodes returned do not have to have a specific type or aspect. For example, an FTS
 * search on properties of type d:content has no type constraint implied or otherwise. Searches against properties have
 * an implied type, but as there can be more than one property -> more than one type or aspect implied (eg via OR in FTS
 * or lucene) they are ignored An iterable result set from a searcher query.<b/> Implementations must implement the
 * indexes for row lookup as zero-based.<b/>
 * 
 * @author andyh
 * @param <ROW> 
 * @param <MD> 
 */
@AlfrescoPublicApi
public interface ResultSetSPI<ROW extends ResultSetRow, MD extends ResultSetMetaData> extends Iterable<ROW> // Specific iterator over ResultSetRows
{
    /**
     * Get the number of rows in this result set. This will be less than or equal to the maximum number of rows
     * requested or the full length of the results set if no restriction on length are specified. If a skip count is
     * given, the length represents the number of results after the skip count and does not include the items skipped.
     * 
     * @return the number of results. -1 means unknown and can be returned for lazy evaluations of permissions when the
     *         actual size is not known and evaluated upon request.
     */
    public int length();
   
    /**
     * Attempt to get the number of rows that matched the query. This result set may only contain a section of the results.
     * If a skip count is given the number found may or may not include the items skipped.
     * This is best effort and only done if is cheap.
     * For SOLR it is cheap; for the DB it may be expensive as permissions are done post query.
     * If you want to know if there are more results to fetch use hasMore()
     * @return long
     */
    public long getNumberFound();

    /**
     * Get the id of the node at the given index (if there is only one selector or no selector)
     * 
     * @param n
     *            zero-based index
     * @return return the the node ref for the row if there is only one selector
     */
    public NodeRef getNodeRef(int n);

    /**
     * Get the score for the node at the given position (if there is only one selector or no selector)
     * 
     * @param n
     *            zero-based index
     * @return return the score for the row if there is only one selector
     */
    public float getScore(int n);

    /**
     * Close the result set and release any resources held/ The result set is bound to the transaction and will auto
     * close at the end of the transaction.
     */
    public void close();

    /**
     * Get a row from the result set by row index, starting at 0.
     * 
     * @param i
     *            zero-based index
     * @return return the row
     */
    public ROW getRow(int i);

    /**
     * Get a list of all the node refs in the result set (if there is only one selector or no selector)
     * 
     * @return the node refs if there is only one selector or no selector *
     */
    public List<NodeRef> getNodeRefs();

    /**
     * Get a list of all the child associations in the results set. (if there is only one selectoror no selector)
     * 
     * @return the child assoc refs if there is only one selector or no selector *
     */
    public List<ChildAssociationRef> getChildAssocRefs();

    /**
     * Get the child assoc ref for a particular row. (if there is only one selectoror no selector)
     * 
     * @param n
     *            zero-based index
     * @return the child assoc ref for the row if there is only one selector or no selector
     */
    public ChildAssociationRef getChildAssocRef(int n);

    /**
     * Get the meta data for the results set.
     * 
     * @return the metadata
     */
    public MD getResultSetMetaData();

    /**
     * Get the start point for this results set in the overall set of rows that match the query - this will be equal to
     * the skip count set when executing the query, and zero if this is not set.
     * 
     * @return the position of the first result in the overall result set
     */
    public int getStart();

    /**
     * Was this result set curtailed - are there more pages to the result set?
     * 
     * @return true if there are more pages in the result set
     */
    public boolean hasMore();

    /**
     * Bulk fetch results in the cache
     * 
     * @param bulkFetch boolean
     */
    public boolean setBulkFetch(boolean bulkFetch);

    /**
     * Do we bulk fetch
     * 
     * @return - true if we do
     */
    public boolean getBulkFetch();

    /**
     * Set the bulk fetch size
     * 
     * @param bulkFetchSize int
     */
    public int setBulkFetchSize(int bulkFetchSize);

    /**
     * Get the bulk fetch size.
     * 
     * @return the fetch size
     */
    public int getBulkFetchSize();
    
    /**
     * @param field String
     * @return List
     */
    public List<Pair<String, Integer>> getFieldFacet(String field);

    /**
     * Gets the facet query results
     * 
     * @return Map of {@literal <requested facet query, count>}
     */
    public Map<String, Integer> getFacetQueries();

    /**
     * Gets the highlighting results.  Returns a Map keyed by noderef.
     * Each value is a pair of "fieldname" and a String array of highlight snippets
     * @return the Map
     */
    public Map<NodeRef, List<Pair<String, List<String>>>> getHighlighting();
    /**
     * Gets the spell check result
     * 
     * @return SpellCheckResult
     */
    public SpellCheckResult getSpellCheckResult();
}
