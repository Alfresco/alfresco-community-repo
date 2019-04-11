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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The results of executing a SOLR TX action
 *
 * @author aborroy
 * @since 6.2
 */
public class SolrActionTxReportResult extends AbstractJSONAPIResult
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SolrActionTxReportResult.class);
    
    /**
     * Parses the JSON to set this Java Object values
     * @param json JSONObject returned by SOLR API
     */
    public SolrActionTxReportResult(JSONObject json)
    {
        try 
        {
            processJson(json);
        }
        catch (NullPointerException | JSONException e)
        {
           LOGGER.info(e.getMessage());
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.lucene.AbstractSolrActionAPIResult#processCoresInfoJson(org.json.JSONObject)
     */
    @Override
    protected void processCoresInfoJson(JSONObject json) throws JSONException
    {

        List<String> cores = new ArrayList<>();
        Map<String, Map<String, Object>> coresInfo = new HashMap<>();
        
        if (json.has("report")) 
        {
        
            JSONObject coreList = json.getJSONObject("report");
            JSONArray coreNameList = coreList.names();
            for(int i = 0; i < coreNameList.length(); i++)
            {
                
                String coreName = String.valueOf(coreNameList.get(i));
                JSONObject core = coreList.getJSONObject(coreName);
                cores.add(coreName);
                
                Map<String, Object> coreInfo = new HashMap<>();
                
                JSONObject transaction = core.getJSONObject("transaction");
                coreInfo.put("transaction", getPropertyValueMap(transaction));

                JSONObject nodes = core.getJSONObject("nodes");
                Map<String, Object> nodesInfo = new HashMap<>();
                JSONArray nodesPropertyNameList = nodes.names();
                for (int j = 0; j < nodesPropertyNameList.length(); j++)
                {
                    String nodeName = String.valueOf(nodesPropertyNameList.get(j));
                    JSONObject node = nodes.getJSONObject(nodeName);
                    coresInfo.put(coreName, getPropertyValueMap(node));
                
                }
                coreInfo.put("nodes", nodesInfo);
                
                coresInfo.put(coreName, coreInfo);
                
            }

        }
        
        this.cores = cores;
        this.coresInfo = coresInfo;
        
    }
    
}
