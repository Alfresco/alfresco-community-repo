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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.search.SimpleResultSetMetaData;
import org.alfresco.repo.search.impl.solr.facet.facetsresponse.GenericBucket;
import org.alfresco.repo.search.impl.solr.facet.facetsresponse.GenericFacetResponse;
import org.alfresco.repo.search.impl.solr.facet.facetsresponse.GenericFacetResponse.FACET_TYPE;
import org.alfresco.repo.search.impl.solr.facet.facetsresponse.MetricCount;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.PermissionEvaluationMode;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetMetaData;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SpellCheckResult;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Andy
 */
public class SolrJSONResultSet implements ResultSet, JSONResult
{
    private static final Log logger = LogFactory.getLog(SolrJSONResultSet.class);
    
    private NodeService nodeService;
    
    private ArrayList<Pair<Long, Float>> page;
    
    private ArrayList<NodeRef> refs;
    
    private ResultSetMetaData rsmd;
    
    private Long status;
    
    private Long queryTime;
    
    private Long numberFound;
    
    private Long start;
    
    private Float maxScore;

    private SimpleResultSetMetaData resultSetMetaData;
    
    private HashMap<String, List<Pair<String, Integer>>> fieldFacets = new HashMap<String, List<Pair<String, Integer>>>(1);
    
    private Map<String, Integer> facetQueries = new HashMap<String, Integer>();

    private Map<NodeRef, List<Pair<String, List<String>>>> highlighting = new HashMap<>();

    private Map<String, List<Pair<String, Integer>>> facetIntervals = new HashMap<String, List<Pair<String, Integer>>>(1);

    private List<GenericFacetResponse> pivotFacets = new ArrayList<>();

    private NodeDAO nodeDao;
    
    private long lastIndexedTxId;
    
    private SpellCheckResult spellCheckResult;
    
    private boolean processedDenies;
    
    /**
     * Detached result set based on that provided
     * @param json JSONObject
     * @param searchParameters SearchParameters
     * @param nodeService NodeService
     * @param nodeDao NodeDAO
     * @param limitBy LimitBy
     * @param maxResults int
     */
    public SolrJSONResultSet(JSONObject json, SearchParameters searchParameters, NodeService nodeService, NodeDAO nodeDao, LimitBy limitBy, int maxResults)
    {
        // Note all properties are returned as multi-valued from the WildcardField "*" definition in the SOLR schema.xml
        this.nodeService = nodeService;
        this.nodeDao = nodeDao;
        try
        {
            JSONObject responseHeader = json.getJSONObject("responseHeader");
            status = responseHeader.getLong("status");
            queryTime = responseHeader.getLong("QTime");
            
            JSONObject response = json.getJSONObject("response");
            numberFound = response.getLong("numFound");
            start = response.getLong("start");
            maxScore = Float.valueOf(response.getString("maxScore"));
            if (json.has("lastIndexedTx"))
            {
                lastIndexedTxId = json.getLong("lastIndexedTx");
            }
            if (json.has("processedDenies"))
            {
                processedDenies = json.getBoolean("processedDenies");
            }
            JSONArray docs = response.getJSONArray("docs");
            
            int numDocs = docs.length();
            
            ArrayList<Long> rawDbids = new ArrayList<Long>(numDocs);
            ArrayList<Float> rawScores = new ArrayList<Float>(numDocs); 
            for(int i = 0; i < numDocs; i++)
            {
                JSONObject doc = docs.getJSONObject(i);
                JSONArray dbids = doc.optJSONArray("DBID");
                if(dbids != null)
                {
                    Long dbid = dbids.getLong(0);
                    Float score = Float.valueOf(doc.getString("score"));
                    rawDbids.add(dbid);
                    rawScores.add(score);
                }
                else
                {
                    Long dbid = doc.optLong("DBID");
                    if(dbid != null)
                    {
                        Float score = Float.valueOf(doc.getString("score"));
                        rawDbids.add(dbid);
                        rawScores.add(score);
                    }
                    else
                    {
                        // No DBID found 
                        throw new LuceneQueryParserException("No DBID found for doc ...");
                    }
                }
                
            }
            
            // bulk load
            if (searchParameters.isBulkFetchEnabled())
            {
                nodeDao.cacheNodesById(rawDbids);
            }

            // filter out rubbish
            
            page = new ArrayList<Pair<Long, Float>>(numDocs);
            refs = new ArrayList<NodeRef>(numDocs);
            Map<Long,NodeRef> dbIdNodeRefs = new HashMap<>(numDocs);

            for(int i = 0; i < numDocs; i++)
            {
                Long dbid = rawDbids.get(i);
                NodeRef nodeRef = nodeService.getNodeRef(dbid);

                if(nodeRef != null)
                {
                    page.add(new Pair<Long, Float>(dbid, rawScores.get(i)));
                    refs.add(nodeRef);
                    dbIdNodeRefs.put(dbid, nodeRef);
                }
            }

            Map<String, String> intervalMappings = new HashMap<>();
            if(json.has("_interval_mappings_"))
            {
                JSONObject interval_mappings = json.getJSONObject("_interval_mappings_");
                for(Iterator it = interval_mappings.keys(); it.hasNext(); /**/)
                {
                    String original = (String) it.next();
                    intervalMappings.put(interval_mappings.getString(original), original);
                }
            }

            //Process hightlight response
            if(json.has("highlighting"))
            {
                JSONObject highObj = (JSONObject) json.getJSONObject("highlighting");
                for(Iterator it = highObj.keys(); it.hasNext(); /**/)
                {
                    Long nodeKey = null;
                    String aKey = (String) it.next();
                    JSONObject high = highObj.getJSONObject(aKey);
                    List< Pair<String, List<String>> > highFields = new ArrayList<>(high.length());
                    for(Iterator hit = high.keys(); hit.hasNext(); /**/)
                    {
                        String highKey = (String) hit.next();
                        if ("DBID".equals(highKey))
                        {
                            nodeKey = high.getLong("DBID");
                        }
                        else
                        {
                            JSONArray highVal = high.getJSONArray(highKey);
                            List<String> highValues = new ArrayList<>(highVal.length());
                            for (int i = 0, length = highVal.length(); i < length; i++)
                            {
                                highValues.add(highVal.getString(i));
                            }
                            Pair<String, List<String>> highPair = new Pair<String, List<String>>(highKey, highValues);
                            highFields.add(highPair);
                        }
                    }
                    NodeRef nodefRef = dbIdNodeRefs.get(nodeKey);
                    if (nodefRef != null && !highFields.isEmpty())
                    {
                        highlighting.put(nodefRef, highFields);
                    }
                }
            }
            if(json.has("facet_counts"))
            {
                JSONObject facet_counts = json.getJSONObject("facet_counts");
                if(facet_counts.has("facet_queries"))
                {
                    JSONObject facet_queries = facet_counts.getJSONObject("facet_queries");
                    for(Iterator it = facet_queries.keys(); it.hasNext(); /**/)
                    {
                        String fq = (String) it.next();
                        Integer count =Integer.parseInt(facet_queries.getString(fq));
                        facetQueries.put(fq, count);
                    }
                }
                if(facet_counts.has("facet_fields"))
                {
                    JSONObject facet_fields = facet_counts.getJSONObject("facet_fields");
                    for(Iterator it = facet_fields.keys(); it.hasNext(); /**/)
                    {
                        String fieldName = (String)it.next();
                        JSONArray facets = facet_fields.getJSONArray(fieldName);
                        int facetArraySize = facets.length();
                        ArrayList<Pair<String, Integer>> facetValues = new ArrayList<Pair<String, Integer>>(facetArraySize/2);
                        for(int i = 0; i < facetArraySize; i+=2)
                        {
                            String facetEntryName = facets.getString(i);
                            Integer facetEntryCount = Integer.parseInt(facets.getString(i+1));
                            Pair<String, Integer> pair = new Pair<String, Integer>(facetEntryName, facetEntryCount);
                            facetValues.add(pair);
                        }
                        fieldFacets.put(fieldName, facetValues);
                    }
                }
                if(facet_counts.has("facet_intervals"))
                {
                    JSONObject facet_intervals = facet_counts.getJSONObject("facet_intervals");
                    for(Iterator it = facet_intervals.keys(); it.hasNext(); /**/)
                    {
                        String fieldName = (String)it.next();
                        JSONObject intervals = facet_intervals.getJSONObject(fieldName);
                        //TODO: Handle a label
                        String fieldkey = intervalMappings.containsKey(fieldName)?intervalMappings.get(fieldName):fieldName;

                        ArrayList<Pair<String, Integer>> intervalValues = new ArrayList<Pair<String, Integer>>(intervals.length());
                        for(Iterator itk = intervals.keys(); itk.hasNext(); /**/)
                        {
                            String key = (String) itk.next();
                            Integer count = Integer.parseInt(intervals.getString(key));
                            intervalValues.add(new Pair<String, Integer>(key, count));
                        }
                        facetIntervals.put(fieldkey,intervalValues);
                    }
                }
                if(facet_counts.has("facet_pivot"))
                {
                    JSONObject facet_pivot = facet_counts.getJSONObject("facet_pivot");
                    for(Iterator it = facet_pivot.keys(); it.hasNext(); /**/)
                    {
                        String pivotName = (String)it.next();
                        pivotFacets = buildPivot(facet_pivot, pivotName);
                    }
                }

            }
            // process Spell check 
            JSONObject spellCheckJson = (JSONObject) json.opt("spellcheck");
            if (spellCheckJson != null)
            {
                List<String> list = new ArrayList<>(3);
                String flag = "";
                boolean searchedFor = false;
                if (spellCheckJson.has("searchInsteadFor"))
                {
                    flag = "searchInsteadFor";
                    searchedFor = true;
                    list.add(spellCheckJson.getString(flag));

                }
                else if (spellCheckJson.has("didYouMean"))
                {
                    flag = "didYouMean";
                    JSONArray suggestions = spellCheckJson.getJSONArray(flag);
                    for (int i = 0, lenght = suggestions.length(); i < lenght; i++)
                    {
                        list.add(suggestions.getString(i));
                    }
                }

                spellCheckResult = new SpellCheckResult(flag, list, searchedFor);

            }
            else
            {
                spellCheckResult = new SpellCheckResult(null, null, false);
            }
        }
        catch (JSONException e)
        {
           logger.info(e.getMessage());
        }
        // We'll say we were unlimited if we got a number less than the limit
        this.resultSetMetaData = new SimpleResultSetMetaData(
                maxResults > 0 && numberFound < maxResults ? LimitBy.UNLIMITED : limitBy,
                PermissionEvaluationMode.EAGER, searchParameters);
    }

    protected List<GenericFacetResponse> buildPivot(JSONObject facet_pivot, String pivotName) throws JSONException
    {
        if (!facet_pivot.has(pivotName)) return null;

        JSONArray pivots = facet_pivot.getJSONArray(pivotName);
        Map<String,List<GenericBucket>> pivotBuckets = new HashMap<>(pivots.length());
        List<GenericFacetResponse> facetResponses = new ArrayList<>();
        for(int i = 0; i < pivots.length(); i++)
        {
            JSONObject piv = pivots.getJSONObject(i);
            String field = piv.getString("field");
            String value = piv.getString("value");
            Integer count = Integer.parseInt(piv.getString("count"));
            List<GenericFacetResponse> innerPivot = buildPivot(piv, "pivot");
            GenericBucket buck = new GenericBucket(piv.getString("value"), field+":"+value, null, Arrays.asList(new MetricCount(count)), innerPivot);
            List<GenericBucket> listBucks = pivotBuckets.containsKey(field)?pivotBuckets.get(field):new ArrayList<>();
            listBucks.add(buck);
            pivotBuckets.put(field, listBucks);
        }

        for (Map.Entry<String, List<GenericBucket>> entry : pivotBuckets.entrySet()) {
            facetResponses.add(new GenericFacetResponse(FACET_TYPE.pivot,entry.getKey(),entry.getValue()));
        }

        if (!facetResponses.isEmpty()) return facetResponses;

        return null;
    }

    public NodeService getNodeService()
    {
        return nodeService;
    }


    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetSPI#close()
     */
    @Override
    public void close()
    {
        // NO OP
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetSPI#getBulkFetch()
     */
    @Override
    public boolean getBulkFetch()
    {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetSPI#getBulkFetchSize()
     */
    @Override
    public int getBulkFetchSize()
    {
        return Integer.MAX_VALUE;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetSPI#getChildAssocRef(int)
     */
    @Override
    public ChildAssociationRef getChildAssocRef(int n)
    {
        ChildAssociationRef primaryParentAssoc = nodeService.getPrimaryParent(getNodeRef(n));
        if(primaryParentAssoc != null)
        {
            return primaryParentAssoc;
        }
        else
        {
            return null;
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetSPI#getChildAssocRefs()
     */
    @Override
    public List<ChildAssociationRef> getChildAssocRefs()
    {
        ArrayList<ChildAssociationRef> refs = new ArrayList<ChildAssociationRef>(page.size());
        for(int i = 0; i < page.size(); i++ )
        {
            refs.add( getChildAssocRef(i));
        }
        return refs;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetSPI#getNodeRef(int)
     */
    @Override
    public NodeRef getNodeRef(int n)
    {
        return refs.get(n);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetSPI#getNodeRefs()
     */
    @Override
    public List<NodeRef> getNodeRefs()
    {
        return Collections.unmodifiableList(refs);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetSPI#getResultSetMetaData()
     */
    @Override
    public ResultSetMetaData getResultSetMetaData()
    {
        return resultSetMetaData;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetSPI#getRow(int)
     */
    @Override
    public ResultSetRow getRow(int i)
    {
       return new SolrJSONResultSetRow(this, i);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetSPI#getScore(int)
     */
    @Override
    public float getScore(int n)
    {
        return page.get(n).getSecond();
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetSPI#getStart()
     */
    @Override
    public int getStart()
    {
        return start.intValue();
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetSPI#hasMore()
     */
    @Override
    public boolean hasMore()
    {
       return numberFound.longValue() > (start.longValue() + page.size());
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetSPI#length()
     */
    @Override
    public int length()
    {
       return page.size();
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetSPI#setBulkFetch(boolean)
     */
    @Override
    public boolean setBulkFetch(boolean bulkFetch)
    {
         return bulkFetch;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetSPI#setBulkFetchSize(int)
     */
    @Override
    public int setBulkFetchSize(int bulkFetchSize)
    {
        return bulkFetchSize;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<ResultSetRow> iterator()
    {
        return new SolrJSONResultSetRowIterator(this);
    }


    /**
     * @return the queryTime
     */
    public Long getQueryTime()
    {
        return queryTime;
    }


    /**
     * @return the numberFound
     */
    public long getNumberFound()
    {
        return numberFound.longValue();
    }

    @Override
    public List<Pair<String, Integer>> getFieldFacet(String field)
    {
        List<Pair<String, Integer>> answer = fieldFacets.get(field);
        if(answer != null)
        {
            return answer;
        }
        else
        {
            return Collections.<Pair<String, Integer>>emptyList();
        }
    }

    public Map<String, List<Pair<String, Integer>>> getFieldFacets()
    {
        return Collections.unmodifiableMap(fieldFacets);
    }

    public Map<String, List<Pair<String, Integer>>> getFacetIntervals()
    {
        return Collections.unmodifiableMap(facetIntervals);
    }

    public List<GenericFacetResponse> getPivotFacets()
    {
        return pivotFacets;
    }

    public long getLastIndexedTxId()
    {
        return lastIndexedTxId;
    }

    @Override
    public Map<String, Integer> getFacetQueries()
    {
        return Collections.unmodifiableMap(facetQueries);
    }

    @Override
    public Map<NodeRef, List<Pair<String, List<String>>>> getHighlighting()
    {
        return Collections.unmodifiableMap(highlighting);
    }

    @Override
    public SpellCheckResult getSpellCheckResult()
    {
        return this.spellCheckResult;
    }

    public boolean getProcessedDenies()
    {
        return processedDenies;
    }
}
