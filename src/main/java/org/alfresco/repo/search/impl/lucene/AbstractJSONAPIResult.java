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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSON returned from SOLR API Parser
 * This class defines common properties and performs response header parsing.
 * An abstract method is provided for implementers to parse Core Information.
 *
 * @author aborroy
 * @since 6.2
 */
public abstract class AbstractJSONAPIResult implements JSONAPIResult
{
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractJSONAPIResult.class);
    
    protected Long status; 
    protected Long queryTime;
    protected List<String> cores;
    protected Map<String, Map<String, Object>> coresInfo;
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.lucene.JSONActionResult#getQueryTime()
     */
    public Long getQueryTime()
    {
        return queryTime;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.lucene.JSONActionResult#getStatus()
     */
    public Long getStatus()
    {
        return status;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.lucene.JSONAPIResult#getCores()
     */
    @Override
    public List<String> getCores()
    {
        return (cores == null ? null : Collections.unmodifiableList(cores));
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.lucene.JSONAPIResult#getCoresInfo()
     */
    @Override
    public Map<String, Map<String, Object>> getCoresInfo() 
    {
        return (coresInfo == null ? null : Collections.unmodifiableMap(coresInfo));
    }
    
    /**
     * Parses the JSON to set this Java Object values
     * @param json JSONObject returned by SOLR API
     * @throws JSONException
     */
    protected void processJson(JSONObject json) throws JSONException
    {
        
        LOGGER.debug("JSON response: {}", json);
        
        JSONObject responseHeader = json.getJSONObject("responseHeader");
        status = responseHeader.getLong("status");
        queryTime = responseHeader.getLong("QTime");
        
        processCoresInfoJson(json);

    }
    
    /**
     * Creates a property-value Map from a JSON Object containing properties and values
     * This method provides the right input for MBeans to expose the SOLR response values from the Response
     * @param json Simple JSON Object containing only properties
     * @return Property-value Map
     * @throws JSONException
     */
    protected Map<String, Object> getPropertyValueMap(JSONObject json) throws JSONException
    {
        Map<String, Object> propertyValueMap = new HashMap<>();
        JSONArray nodesPropertyNameList = json.names();
        for (int j = 0; j < nodesPropertyNameList.length(); j++)
        {
            String propertyName = String.valueOf(nodesPropertyNameList.get(j));
            Object propertyValue = json.get(propertyName);
            if (propertyValue != JSONObject.NULL)
            {
                // MBeans Objects are defined as Long types, so we need casting to provide the expected type
                if (propertyValue instanceof Integer)
                {
                    propertyValue = Long.valueOf((Integer) propertyValue);
                }
                propertyValueMap.put(propertyName, propertyValue);
            }
        }
        return propertyValueMap;
    }
    
    /**
     * Parses the JSON to set this Java Object values related to Core Information
     * @param json JSONObject returned by SOLR API
     * @throws JSONException
     */
    protected abstract void processCoresInfoJson(JSONObject json) throws JSONException;

}
