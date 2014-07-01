/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
import java.util.List;

import org.alfresco.service.cmr.search.StatsResultSet;
import org.alfresco.service.cmr.search.StatsResultStat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.StringUtils;

/**
 * The results of executing a solr stats query 
 *
 * @author Gethin James
 * @since 5.0
 */
public class SolrStatsResult implements JSONResult, StatsResultSet
{
    private static final Log logger = LogFactory.getLog(SolrStatsResult.class);
    
    private Long status; 
    private Long queryTime;
    private Long numberFound;
    
    //Summary stats
    private Long sum;
    private Long max;
    private Long mean;
    
    private List<StatsResultStat> stats;
    private boolean nameIsADate;
    
    public SolrStatsResult(JSONObject json, boolean nameIsADate)
    {
        try 
        {
            this.nameIsADate = nameIsADate;
            stats = new ArrayList<>();
            processJson(json);
        }
        catch (NullPointerException | JSONException e)
        {
           logger.info(e.getMessage());
        }
    }
    
    /**
     * Parses the json
     * @param json
     * @throws JSONException
     */
    protected void processJson(JSONObject json) throws JSONException
    {
        JSONObject responseHeader = json.getJSONObject("responseHeader");
        status = responseHeader.getLong("status");
        queryTime = responseHeader.getLong("QTime");
        
        JSONObject response = json.getJSONObject("response");
        numberFound = response.getLong("numFound");
        
        if (logger.isDebugEnabled())
        {
            logger.debug("JSON response: "+json);
        }
        
        if(json.has("stats"))
        {
            JSONObject statsObj = json.getJSONObject("stats");
            if(statsObj.has("stats_fields"))
            {
                JSONObject statsFields = statsObj.getJSONObject("stats_fields");
                JSONObject contentsize = statsFields.getJSONObject("contentsize");
                
                sum = contentsize.getLong("sum");
                max = contentsize.getLong("max");
                mean = contentsize.getLong("mean");
                
                if(contentsize.has("facets"))
                {
                    JSONObject facets = contentsize.getJSONObject("facets");
                    JSONArray facetNames = facets.names();
                    for(int i = 0; i < facetNames.length(); i++)
                    {
                        JSONObject facetType = facets.getJSONObject(String.valueOf(facetNames.get(i)));
                        if (facetType!=null && facetType.names() != null)
                        {
                            JSONArray facetValues = facetType.names();
                            for(int j = 0; j < facetValues.length(); j++)
                            {
                                String name = String.valueOf(facetValues.get(j));
                                JSONObject facetVal = facetType.getJSONObject(name);
                                stats.add(processStat(name, facetVal));                          
                            }                        
                        }


                    }
                }
            }
        }
    }
    
    /**
     * Proccesses an individual stat entry
     * @param name
     * @param facetVal
     * @return Stat
     * @throws JSONException
     */
    private StatsResultStat processStat(String name, JSONObject facetVal) throws JSONException
    {
        return new StatsResultStat(nameIsADate?formatAsDate(name):name,
                    facetVal.getLong("sum"),
                    facetVal.getLong("count"),
                    facetVal.getLong("min"),
                    facetVal.getLong("max"),
                    facetVal.getLong("mean"));
    }

    public static String formatAsDate(String name)
    {
        if (StringUtils.hasText(name))
        {
            try
            {
                //LocalDate d = LocalDate.parse(name);
                //return d.toString();
                return name.substring(0,10);
            }
            catch (IllegalArgumentException iae)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Can't parse reponse: "+iae.getMessage());
                }
            }
        }

        //Default
        return "";
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("SolrStatsResult [status=").append(this.status).append(", queryTime=")
                    .append(this.queryTime).append(", numberFound=").append(this.numberFound)
                    .append(", sum=").append(this.sum).append(", max=").append(this.max)
                    .append(", mean=").append(this.mean).append(", stats=").append(this.stats)
                    .append("]");
        return builder.toString();
    }
    
    public Long getStatus()
    {
        return this.status;
    }
    public Long getQueryTime()
    {
        return this.queryTime;
    }
    
    @Override
    public long getNumberFound()
    {
        return this.numberFound;
    }

    @Override
    public Long getSum()
    {
        return this.sum;
    }

    @Override
    public Long getMax()
    {
        return this.max;
    }

    @Override
    public Long getMean()
    {
        return this.mean;
    }

    @Override
    public List<StatsResultStat> getStats()
    {
        return this.stats;
    }
}
