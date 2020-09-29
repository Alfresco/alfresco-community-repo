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
package org.alfresco.repo.search.impl.solr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.search.impl.AbstractJSONAPIResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The results of executing a SOLR REPORT action
 *
 * @author aborroy
 * @since 6.2
 */
public class SolrActionReportResult extends AbstractJSONAPIResult
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SolrActionReportResult.class);
    
    /**
     * Parses the JSON to set this Java Object values
     * @param json JSONObject returned by SOLR API
     */
    public SolrActionReportResult(JSONObject json)
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

        cores = new ArrayList<>();
        coresInfo = new HashMap<>();
        
        if (json.has("report")) 
        {
        
            JSONObject coreList = json.getJSONObject("report");
            JSONArray coreNameList = coreList.names();
            for(int i = 0; i < coreNameList.length(); i++)
            {
                
                String coreName = String.valueOf(coreNameList.get(i));
                cores.add(coreName);
                
                Map<String, Object> coreInfo = new HashMap<>();
                JSONObject coreProperties = coreList.getJSONObject(coreName);
                JSONArray propertyNameList = coreProperties.names();
                for (int j = 0; j < propertyNameList.length(); j++)
                {
                    String propertyName = String.valueOf(propertyNameList.get(j));
                    coreInfo.put(propertyName, coreProperties.get(propertyName));
                }
                
                coresInfo.put(coreName, coreInfo);

            }

        }
        
    }
    
}

