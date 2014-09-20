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
    
    private static String ALFRESCO_DISK = "tracker.alfresco.disk";
    
    private static String ALFRESCO_MEMORY = "tracker.alfresco.memory";

    private static String ARCHIVE_ACTIVE = "tracker.archive.active";

    private static String ARCHIVE_LAG = "tracker.archive.lag";

    private static String ARCHIVE_LAG_DURATION = "tracker.archive.lag.duration";
    
    private static String ARCHIVE_LAST_INDEXED_TXN = "tracker.archive.last.indexed.txn";
    
    private static String ARCHIVE_APPROX_TXNS_REMAINING = "tracker.archive.approx.txns.remaining";
    
    private static String ARCHIVE_APPROX_INDEXING_TIME_REMAINING = "tracker.archive.approx.indexing.time.remaining";
    
    private static String ARCHIVE_DISK = "tracker.archive.disk";
    
    private static String ARCHIVE_MEMORY = "tracker.archive.memory";
    

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
                && !name.equals(SolrChildApplicationContextFactory.ALFRESCO_DISK)
                && !name.equals(SolrChildApplicationContextFactory.ALFRESCO_MEMORY)
                
                && !name.equals(SolrChildApplicationContextFactory.ARCHIVE_ACTIVE)
                && !name.equals(SolrChildApplicationContextFactory.ARCHIVE_LAG) 
                && !name.equals(SolrChildApplicationContextFactory.ARCHIVE_LAG_DURATION)
                && !name.equals(SolrChildApplicationContextFactory.ARCHIVE_APPROX_TXNS_REMAINING)
                && !name.equals(SolrChildApplicationContextFactory.ARCHIVE_APPROX_INDEXING_TIME_REMAINING)
                && !name.equals(SolrChildApplicationContextFactory.ARCHIVE_LAST_INDEXED_TXN)
                && !name.equals(SolrChildApplicationContextFactory.ARCHIVE_DISK)
                && !name.equals(SolrChildApplicationContextFactory.ARCHIVE_MEMORY)
                ;
    }

    @Override
    public String getProperty(String name)
    {
        // MNT-9254 fix, use search.solrAdminHTTPCLient bean to retrive property value only if sorl subsystem is active and started (application context in state should be not null)
        if (false == isUpdateable(name) && ((ApplicationContextState) getState(false)).getApplicationContext(false) != null)
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

                JSONObject alfresco = summary.has("alfresco") ? summary.getJSONObject("alfresco") : null;
                String alfrescoLag = alfresco == null ? "Unavailable" : alfresco.getString("TX Lag");
                String alfrescoActive =  alfresco == null ? "false" : alfresco.getString("Active");
                String alfrescoDuration =  alfresco == null ? "Unavailable" : alfresco.getString("TX Duration");
                String alfrescoLastIndexedTxn =  alfresco == null ? "Unavailable" : alfresco.getString("Id for last TX in index");
                String alfrescoApproxTxnsReminaing =  alfresco == null ? "Unavailable" : alfresco.getString("Approx transactions remaining");
                String alfrescoApproxIndexingTimeReminaing =  alfresco == null ? "Unavailable" : alfresco.getString("Approx transaction indexing time remaining");
                String alfrescoDisk =  alfresco == null ? "Unavailable" : alfresco.getString("On disk (GB)");
                String alfrescoMemory =  alfresco == null ? "Unavailable" : alfresco.getString("Total Searcher Cache (GB)");
               

                JSONObject archive = summary.has("archive") ? summary.getJSONObject("archive") : null;
                String archiveLag = archive == null ? "Unavailable" : archive.getString("TX Lag");
                String archiveActive = archive == null ? "false" : archive.getString("Active");
                String archiveDuration = archive == null ? "Unavailable" : archive.getString("TX Duration");
                String archiveLastIndexedTxn = archive == null ? "Unavailable" : archive.getString("Id for last TX in index");
                String archiveApproxTxnsReminaing = archive == null ? "Unavailable" : archive.getString("Approx transactions remaining");
                String archiveApproxIndexingTimeReminaing = archive == null ? "Unavailable" : archive.getString("Approx transaction indexing time remaining");
                String archiveDisk = archive == null ? "Unavailable" : archive.getString("On disk (GB)");
                String archiveMemory = archive == null ? "Unavailable" : archive.getString("Total Searcher Cache (GB)");

                if (name.equals(SolrChildApplicationContextFactory.ALFRESCO_ACTIVE))
                {
                	if(alfrescoActive == null || alfrescoActive.isEmpty())
                	{
                		// Admin Console is expecting a true/false value, not blank 
                		return "false";
                	}
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
                else if (name.equals(SolrChildApplicationContextFactory.ALFRESCO_DISK))
                {
                    return alfrescoDisk;
                }
                else if (name.equals(SolrChildApplicationContextFactory.ALFRESCO_MEMORY))
                {
                    return alfrescoMemory;
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
                else if (name.equals(SolrChildApplicationContextFactory.ARCHIVE_DISK))
                {
                    return archiveDisk;
                }
                else if (name.equals(SolrChildApplicationContextFactory.ARCHIVE_MEMORY))
                {
                    return archiveMemory;
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
            catch (IllegalArgumentException iae)
            {
                return "Unavailable: " + iae.getMessage();
            }
        }
        else
        {
        	// solr subsystem is not started or not active
            if (name.equals(SolrChildApplicationContextFactory.ALFRESCO_ACTIVE))
            {
             	return "";
            }
            else if (name.equals(SolrChildApplicationContextFactory.ARCHIVE_ACTIVE))
            {
                return "";
            }
            else if (name.equals(SolrChildApplicationContextFactory.ALFRESCO_LAG))
            {
                return "Unavailable: solr subsystem not started";
            }
            else if (name.equals(SolrChildApplicationContextFactory.ALFRESCO_LAG_DURATION))
            {
            	return "Unavailable: solr subsystem not started";
            }
            else if (name.equals(SolrChildApplicationContextFactory.ALFRESCO_LAST_INDEXED_TXN))
            {
            	return "Unavailable: solr subsystem not started";
            }
            else if (name.equals(SolrChildApplicationContextFactory.ALFRESCO_APPROX_TXNS_REMAINING))
            {
            	return "Unavailable: solr subsystem not started";
            }
            else if (name.equals(SolrChildApplicationContextFactory.ALFRESCO_APPROX_INDEXING_TIME_REMAINING))
            {
            	return "Unavailable: solr subsystem not started";
            }
            else if (name.equals(SolrChildApplicationContextFactory.ALFRESCO_DISK))
            {
                return "Unavailable: solr subsystem not started";   
            }
            else if (name.equals(SolrChildApplicationContextFactory.ALFRESCO_MEMORY))
            {
                return "Unavailable: solr subsystem not started";
            }
            else if (name.equals(SolrChildApplicationContextFactory.ARCHIVE_LAG))
            {
            	return "Unavailable: solr subsystem not started";
            }
            else if (name.equals(SolrChildApplicationContextFactory.ARCHIVE_LAG_DURATION))
            {
            	return "Unavailable: solr subsystem not started";
            }
            else if (name.equals(SolrChildApplicationContextFactory.ARCHIVE_LAST_INDEXED_TXN))
            {
            	return "Unavailable: solr subsystem not started";
            }
            else if (name.equals(SolrChildApplicationContextFactory.ARCHIVE_APPROX_TXNS_REMAINING))
            {
            	return "Unavailable: solr subsystem not started";
            }
            else if (name.equals(SolrChildApplicationContextFactory.ARCHIVE_APPROX_INDEXING_TIME_REMAINING))
            {
            	return "Unavailable: solr subsystem not started";
            }
            else if (name.equals(SolrChildApplicationContextFactory.ARCHIVE_DISK))
            {
                return "Unavailable: solr subsystem not started";   
            }
            else if (name.equals(SolrChildApplicationContextFactory.ARCHIVE_MEMORY))
            {
                return "Unavailable: solr subsystem not started";
            }
            else
            {
                return super.getProperty(name);
            }
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
        result.add(SolrChildApplicationContextFactory.ALFRESCO_DISK);
        result.add(SolrChildApplicationContextFactory.ALFRESCO_MEMORY);
       
        result.add(SolrChildApplicationContextFactory.ARCHIVE_ACTIVE);
        result.add(SolrChildApplicationContextFactory.ARCHIVE_LAG);
        result.add(SolrChildApplicationContextFactory.ARCHIVE_LAG_DURATION);
        result.add(SolrChildApplicationContextFactory.ARCHIVE_LAST_INDEXED_TXN);
        result.add(SolrChildApplicationContextFactory.ARCHIVE_APPROX_TXNS_REMAINING);
        result.add(SolrChildApplicationContextFactory.ARCHIVE_APPROX_INDEXING_TIME_REMAINING);
        result.add(SolrChildApplicationContextFactory.ARCHIVE_DISK);
        result.add(SolrChildApplicationContextFactory.ARCHIVE_MEMORY);
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
    
    protected void destroy(boolean isPermanent)
    {
        super.destroy(isPermanent);
        doInit();
    }
}
