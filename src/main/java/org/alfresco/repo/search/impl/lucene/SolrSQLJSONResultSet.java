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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.search.SimpleResultSetMetaData;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.BasicSearchParameters;
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
 * @author Michael Suzuki
 */
public class SolrSQLJSONResultSet implements ResultSet, JSONResult
{
    private static Log logger = LogFactory.getLog(SolrSQLJSONResultSet.class);
    private Long status;
    private Long queryTime;
    private SimpleResultSetMetaData resultSetMetaData;
    private String solrRes;
    private int length;
    private String errorMsg; 
    ResultSet wrapped;
    public SolrSQLJSONResultSet(JSONObject json, BasicSearchParameters searchParameters)
    {
        try
        {
            solrRes = ((JSONObject) json).toString();
            JSONObject res = (JSONObject) json.get("result-set");
            JSONArray docs = (JSONArray) res.get("docs");
            try
            {
                JSONObject obj1 = docs.getJSONObject(0);
                if(obj1.has("EXCEPTION")) 
                {
                    throw new RuntimeException("Unable to execute the query, error caused by: " + obj1.toString());
                }
            }
            catch (JSONException e)
            {
                //Ignore as its a good thing if it does find above line.
            }
            //Check if it has an error
            this.length = docs.length();
            JSONObject time = (JSONObject) docs.get(length -1);
            queryTime = new Long((Integer) time.get("RESPONSE_TIME"));
            // We'll say we were unlimited if we got a number less than the limit
            this.resultSetMetaData = new SimpleResultSetMetaData(LimitBy.FINAL_SIZE, 
                    PermissionEvaluationMode.EAGER, (SearchParameters)searchParameters);
        } 
        catch (JSONException e)
        {
            logger.info(e.getMessage());
        }
    }

    @Override
    public int length()
    {
        return length;
    }

    @Override
    public long getNumberFound()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public NodeRef getNodeRef(int n)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public float getScore(int n)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void close()
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public ResultSetRow getRow(int i)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<NodeRef> getNodeRefs()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ChildAssociationRef> getChildAssocRefs()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ChildAssociationRef getChildAssocRef(int n)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResultSetMetaData getResultSetMetaData()
    {
        return resultSetMetaData;
    }

    @Override
    public int getStart()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean hasMore()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean setBulkFetch(boolean bulkFetch)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean getBulkFetch()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int setBulkFetchSize(int bulkFetchSize)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getBulkFetchSize()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public List<Pair<String, Integer>> getFieldFacet(String field)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, Integer> getFacetQueries()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<NodeRef, List<Pair<String, List<String>>>> getHighlighting()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SpellCheckResult getSpellCheckResult()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterator<ResultSetRow> iterator()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long getQueryTime()
    {
        return queryTime;
    }

    public String getSolrRes()
    {
        return solrRes;
    }
    

}
