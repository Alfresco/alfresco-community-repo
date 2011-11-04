/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.search.impl.lucene;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.search.SimpleResultSetMetaData;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.PermissionEvaluationMode;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetMetaData;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.util.Pair;
import org.apache.solr.client.solrj.response.FacetField;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Andy
 */
public class SolrJSONResultSet implements ResultSet
{
    private NodeDAO nodeDAO;
    
    private NodeService nodeService;
    
    private ArrayList<Pair<Long, Float>> page;
    
    private ResultSetMetaData rsmd;
    
    private Long status;
    
    private Long queryTime;
    
    private Long numberFound;
    
    private Long start;
    
    private Float maxScore;

    private SimpleResultSetMetaData resultSetMetaData;
    
    private HashMap<String, List<Pair<String, Integer>>> fieldFacets = new HashMap<String, List<Pair<String, Integer>>>(1);
    
    /**
     * Detached result set based on that provided
     * @param resultSet
     */
    public SolrJSONResultSet(JSONObject json, NodeDAO nodeDAO, SearchParameters searchParameters, NodeService nodeService)
    {
        // Note all properties are returned as multi-valued from the WildcardField "*" definition in the SOLR schema.xml
        this.nodeDAO = nodeDAO;
        this.nodeService = nodeService;
        this.resultSetMetaData = new SimpleResultSetMetaData(LimitBy.UNLIMITED, PermissionEvaluationMode.EAGER, searchParameters);
        try
        {
            JSONObject responseHeader = json.getJSONObject("responseHeader");
            status = responseHeader.getLong("status");
            queryTime = responseHeader.getLong("QTime");
            
            JSONObject response = json.getJSONObject("response");
            numberFound = response.getLong("numFound");
            start = response.getLong("start");
            maxScore = Float.valueOf(response.getString("maxScore"));
            
            JSONArray docs = response.getJSONArray("docs");
            
            int numDocs = docs.length();
            page = new ArrayList<Pair<Long, Float>>(numDocs);
            for(int i = 0; i < numDocs; i++)
            {
                JSONObject doc = docs.getJSONObject(i);
                JSONArray dbids = doc.getJSONArray("DBID");
                Long dbid = dbids.getLong(0);
                Float score = Float.valueOf(doc.getString("score"));
                page.add(new Pair<Long, Float>(dbid, score));
                
                for(Iterator it = doc.keys(); it.hasNext(); /* */)
                {
                    String key = (String)it.next();
                }
            }
            
            if(json.has("facet_counts"))
            {
                JSONObject facet_counts = json.getJSONObject("facet_counts");
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
            }
            
        }
        catch (JSONException e)
        {
           
        }
        
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
        Pair<Long, ChildAssociationRef> primaryParentAssoc = nodeDAO.getPrimaryParentAssoc(page.get(n).getFirst());
        if(primaryParentAssoc != null)
        {
            return primaryParentAssoc.getSecond();
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
        // TODO: lost nodes?
        Pair<Long, NodeRef> nodePair = nodeDAO.getNodePair(page.get(n).getFirst());
        if(nodePair != null)
        {
            return nodePair.getSecond();
        }
        else
        {
            return new NodeRef(new StoreRef("missing", "missing"), "missing");
        }
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.search.ResultSetSPI#getNodeRefs()
     */
    @Override
    public List<NodeRef> getNodeRefs()
    {
        ArrayList<NodeRef> refs = new ArrayList<NodeRef>(page.size());
        for(int i = 0; i < page.size(); i++ )
        {
            refs.add( getNodeRef(i));
        }
        return refs;
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
       return numberFound.longValue() > (start.longValue() + page.size() + 1); 
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
    public Long getNumberFound()
    {
        return numberFound;
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
}
