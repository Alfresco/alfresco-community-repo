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
package org.alfresco.repo.search.impl.solr;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.repo.search.impl.lucene.LuceneQueryParserException;
import org.alfresco.service.cmr.repository.datatype.Duration;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;

/**
 * @author Andy
 */
public class SolrChildApplicationContextFactory extends ChildApplicationContextFactory
{

    private static String ALFRESCO_ACTIVE = "tracker.alfresco.active";

    private static String ALFRESCO_LAG = "tracker.alfresco.lag";

    private static String ALFRESCO_LAG_DURATION = "tracker.alfresco.lag.duration";
    
    private static String ALFRESCO_LAST_INDEXED_TXN = "tracker.alfresco.last.indexed.txn";
    
    private static String ALFRESCO_APPROX_TXNS_REMAINING = "tracker.alfresco.approx.txns.remaining";
    
    private static String ALFRESCO_APPROX_INDEXING_TIME_REMAINING = "tracker.alfresco.approx.indexing.time.remaining";

    private static String ARCHIVE_ACTIVE = "tracker.archive.active";

    private static String ARCHIVE_LAG = "tracker.archive.lag";

    private static String ARCHIVE_LAG_DURATION = "tracker.archive.lag.duration";
    
    private static String ARCHIVE_LAST_INDEXED_TXN = "tracker.archive.last.indexed.txn";
    
    private static String ARCHIVE_APPROX_TXNS_REMAINING = "tracker.archive.approx.txns.remaining";
    
    private static String ARCHIVE_APPROX_INDEXING_TIME_REMAINING = "tracker.archive.approx.indexing.time.remaining";
    
 
    

    @Override
    public boolean isUpdateable(String name)
    {
        // TODO Auto-generated method stub
        return super.isUpdateable(name)
                && !name.equals(SolrChildApplicationContextFactory.ALFRESCO_ACTIVE) 
                && !name.equals(SolrChildApplicationContextFactory.ALFRESCO_LAG)
                && !name.equals(SolrChildApplicationContextFactory.ALFRESCO_LAG_DURATION) 
                && !name.equals(SolrChildApplicationContextFactory.ALFRESCO_LAST_INDEXED_TXN) 
                && !name.equals(SolrChildApplicationContextFactory.ALFRESCO_APPROX_TXNS_REMAINING) 
                && !name.equals(SolrChildApplicationContextFactory.ALFRESCO_APPROX_INDEXING_TIME_REMAINING) 
                
                && !name.equals(SolrChildApplicationContextFactory.ARCHIVE_ACTIVE)
                && !name.equals(SolrChildApplicationContextFactory.ARCHIVE_LAG) 
                && !name.equals(SolrChildApplicationContextFactory.ARCHIVE_LAG_DURATION)
                && !name.equals(SolrChildApplicationContextFactory.ARCHIVE_APPROX_TXNS_REMAINING)
                && !name.equals(SolrChildApplicationContextFactory.ARCHIVE_APPROX_INDEXING_TIME_REMAINING)
                && !name.equals(SolrChildApplicationContextFactory.ARCHIVE_LAST_INDEXED_TXN)
                ;
    }

    @Override
    public String getProperty(String name)
    {
        if (false == isUpdateable(name))
        {
            try
            {
                ApplicationContext ctx = getApplicationContext();
                SolrAdminHTTPClient adminClient = (SolrAdminHTTPClient) ctx.getBean("search.solrAdminHTTPCLient");
                HashMap<String, String> args = new HashMap<String, String>();
                args.put("action", "SUMMARY");
                args.put("wt", "json");
                JSONObject json = adminClient.execute(args);
                JSONObject summary = json.getJSONObject("Summary");

                Date now = new Date();

                JSONObject alfresco = summary.getJSONObject("alfresco");
                String alfrescoLag = alfresco.getString("Lag");
                String alfrescoActive = alfresco.getString("Active");
                String alfrescoDuration = alfresco.getString("Duration");
                String alfrescoLastIndexedTxn = alfresco.getString("Id for last TX in index");
                String alfrescoApproxTxnsReminaing = alfresco.getString("Approx transactions remaining");
                String alfrescoApproxIndexingTimeReminaing = alfresco.getString("Approx indexing time remaining");
               

                JSONObject archive = summary.getJSONObject("archive");
                String archiveLag = archive.getString("Lag");
                String archiveActive = archive.getString("Active");
                String archiveDuration = archive.getString("Duration");
                String archiveLastIndexedTxn = archive.getString("Id for last TX in index");
                String archiveApproxTxnsReminaing = archive.getString("Approx transactions remaining");
                String archiveApproxIndexingTimeReminaing = archive.getString("Approx indexing time remaining");

                if (name.equals(SolrChildApplicationContextFactory.ALFRESCO_ACTIVE))
                {
                    return alfrescoActive;
                }
                else if (name.equals(SolrChildApplicationContextFactory.ALFRESCO_LAG))
                {
                    return alfrescoLag;
                }
                else if (name.equals(SolrChildApplicationContextFactory.ALFRESCO_LAG_DURATION))
                {
                    return alfrescoDuration;
                }
                else if (name.equals(SolrChildApplicationContextFactory.ALFRESCO_LAST_INDEXED_TXN))
                {
                    return alfrescoLastIndexedTxn;
                }
                else if (name.equals(SolrChildApplicationContextFactory.ALFRESCO_APPROX_TXNS_REMAINING))
                {
                    return alfrescoApproxTxnsReminaing;
                }
                else if (name.equals(SolrChildApplicationContextFactory.ALFRESCO_APPROX_INDEXING_TIME_REMAINING))
                {
                    return alfrescoApproxIndexingTimeReminaing;
                }
                else if (name.equals(SolrChildApplicationContextFactory.ARCHIVE_ACTIVE))
                {
                    return archiveActive;
                }
                else if (name.equals(SolrChildApplicationContextFactory.ARCHIVE_LAG))
                {
                    return archiveLag;
                }
                else if (name.equals(SolrChildApplicationContextFactory.ARCHIVE_LAG_DURATION))
                {
                    return archiveDuration;
                }
                else if (name.equals(SolrChildApplicationContextFactory.ARCHIVE_LAST_INDEXED_TXN))
                {
                    return archiveLastIndexedTxn;
                }
                else if (name.equals(SolrChildApplicationContextFactory.ARCHIVE_APPROX_TXNS_REMAINING))
                {
                    return archiveApproxTxnsReminaing;
                }
                else if (name.equals(SolrChildApplicationContextFactory.ARCHIVE_APPROX_INDEXING_TIME_REMAINING))
                {
                    return archiveApproxIndexingTimeReminaing;
                }
                else
                {
                    return "Unavailable";
                }
            }
            catch (LuceneQueryParserException lqe)
            {
                return "Unavailable: " + lqe.getMessage();
            }
            catch (JSONException e)
            {
                return "Unavailable: " + e.getMessage();
            }
        }
        else
        {
            return super.getProperty(name);
        }
    }

    @Override
    public Set<String> getPropertyNames()
    {
        Set<String> result = new TreeSet<String>();
        result.add(SolrChildApplicationContextFactory.ALFRESCO_ACTIVE);
        result.add(SolrChildApplicationContextFactory.ALFRESCO_LAG);
        result.add(SolrChildApplicationContextFactory.ALFRESCO_LAG_DURATION);
        result.add(SolrChildApplicationContextFactory.ALFRESCO_LAST_INDEXED_TXN);
        result.add(SolrChildApplicationContextFactory.ALFRESCO_APPROX_TXNS_REMAINING);
        result.add(SolrChildApplicationContextFactory.ALFRESCO_APPROX_INDEXING_TIME_REMAINING);
       
        result.add(SolrChildApplicationContextFactory.ARCHIVE_ACTIVE);
        result.add(SolrChildApplicationContextFactory.ARCHIVE_LAG);
        result.add(SolrChildApplicationContextFactory.ARCHIVE_LAG_DURATION);
        result.add(SolrChildApplicationContextFactory.ARCHIVE_LAST_INDEXED_TXN);
        result.add(SolrChildApplicationContextFactory.ARCHIVE_APPROX_TXNS_REMAINING);
        result.add(SolrChildApplicationContextFactory.ARCHIVE_APPROX_INDEXING_TIME_REMAINING);
        result.addAll(super.getPropertyNames());
        return result;
    }

    public void setProperty(String name, String value)
    {
        if(false == isUpdateable(name))
        {
            throw new IllegalStateException("Illegal write to property \"" + name + "\"");
        }
        super.setProperty(name, value);
    }    
}
