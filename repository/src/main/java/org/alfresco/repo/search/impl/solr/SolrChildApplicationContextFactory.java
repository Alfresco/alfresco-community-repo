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

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.repo.search.QueryParserException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;

/**
 * @author Andy
 */
public class SolrChildApplicationContextFactory extends ChildApplicationContextFactory
{
    
    private static final Log LOGGER = LogFactory.getLog(SolrChildApplicationContextFactory.class);

    protected static final String ALFRESCO_ACTIVE = "tracker.alfresco.active";
    protected static final String ALFRESCO_LAG = "tracker.alfresco.lag";
    protected static final String ALFRESCO_LAG_DURATION = "tracker.alfresco.lag.duration";
    protected static final String ALFRESCO_LAST_INDEXED_TXN = "tracker.alfresco.last.indexed.txn";
    protected static final String ALFRESCO_APPROX_TXNS_REMAINING = "tracker.alfresco.approx.txns.remaining";
    protected static final String ALFRESCO_APPROX_INDEXING_TIME_REMAINING = "tracker.alfresco.approx.indexing.time.remaining";
    protected static final String ALFRESCO_DISK = "tracker.alfresco.disk";
    protected static final String ALFRESCO_MEMORY = "tracker.alfresco.memory";

    protected static final String ARCHIVE_ACTIVE = "tracker.archive.active";
    protected static final String ARCHIVE_LAG = "tracker.archive.lag";
    protected static final String ARCHIVE_LAG_DURATION = "tracker.archive.lag.duration";
    protected static final String ARCHIVE_LAST_INDEXED_TXN = "tracker.archive.last.indexed.txn";
    protected static final String ARCHIVE_APPROX_TXNS_REMAINING = "tracker.archive.approx.txns.remaining";
    protected static final String ARCHIVE_APPROX_INDEXING_TIME_REMAINING = "tracker.archive.approx.indexing.time.remaining";
    protected static final String ARCHIVE_DISK = "tracker.archive.disk";
    protected static final String ARCHIVE_MEMORY = "tracker.archive.memory";
    
    private SolrAdminHTTPClient adminClient;
    
    public void setAdminClient(SolrAdminHTTPClient adminClient)
    {
        this.adminClient = adminClient;
    }
    
    public SolrAdminHTTPClient getAdminClient()
    {
        if (adminClient == null)
        {
            ApplicationContext ctx = getApplicationContext();
            adminClient = (SolrAdminHTTPClient) ctx.getBean("search.solrAdminHTTPCLient");
        }
        return adminClient;
    }
    
    private static boolean isAlfrescoCoreProperty(String name)
    {
        return name.startsWith("tracker.alfresco.");
    }

    private static boolean isArchiveCoreProperty(String name)
    {
        return name.startsWith("tracker.archive.");
    }
    
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
    
    private String getPropertyValue(JSONObject core, String name)
    {
        switch (name)
        {
            case SolrChildApplicationContextFactory.ALFRESCO_ACTIVE:
            case SolrChildApplicationContextFactory.ARCHIVE_ACTIVE:
                return core.getBoolean("Active") ? "true" : "false";
            case SolrChildApplicationContextFactory.ALFRESCO_LAG:
            case SolrChildApplicationContextFactory.ARCHIVE_LAG:
                return core.getString("TX Lag");
            case SolrChildApplicationContextFactory.ALFRESCO_LAG_DURATION:
            case SolrChildApplicationContextFactory.ARCHIVE_LAG_DURATION:
                return core.getString("TX Duration");
            case SolrChildApplicationContextFactory.ALFRESCO_LAST_INDEXED_TXN:
            case SolrChildApplicationContextFactory.ARCHIVE_LAST_INDEXED_TXN:
                return core.getNumber("Id for last TX in index").toString();
            case SolrChildApplicationContextFactory.ALFRESCO_APPROX_TXNS_REMAINING:
            case SolrChildApplicationContextFactory.ARCHIVE_APPROX_TXNS_REMAINING:
                return core.getNumber("Approx transactions remaining").toString();
            case SolrChildApplicationContextFactory.ALFRESCO_APPROX_INDEXING_TIME_REMAINING:
            case SolrChildApplicationContextFactory.ARCHIVE_APPROX_INDEXING_TIME_REMAINING:
                return core.getString("Approx transaction indexing time remaining");
            case SolrChildApplicationContextFactory.ALFRESCO_DISK:
            case SolrChildApplicationContextFactory.ARCHIVE_DISK:
                return core.getNumber("On disk (GB)").toString();
            case SolrChildApplicationContextFactory.ALFRESCO_MEMORY:
            case SolrChildApplicationContextFactory.ARCHIVE_MEMORY:
                return core.getNumber("Total Searcher Cache (GB)").toString();
            default:
                return null;
            
        }
    }
    
    private JSONObject getSolrSummary()
    {
        HashMap<String, String> args = new HashMap<String, String>();
        args.put("action", "SUMMARY");
        args.put("wt", "json");
        JSONObject json = getAdminClient().execute(args);
        return json.getJSONObject("Summary");
    }
    
    @Override
    public String getProperty(String name)
    {
        // MNT-9254 fix, use search.solrAdminHTTPCLient bean to retrieve property value only if sorl subsystem is active and started (application context in state should be not null)
        if (false == isUpdateable(name) && ((ApplicationContextState) getState(false)).getApplicationContext(false) != null)
        {
            try
            {
                
                JSONObject summary = getSolrSummary();
                
                if (isAlfrescoCoreProperty(name))
                {
    	            JSONObject alfresco = null;
                    try
                    {
                        alfresco = summary.getJSONObject("alfresco");
                    }
                    catch (JSONException e)
                    {
                        // The core might be absent.
                        LOGGER.debug("Node alfresco not found", e);
                    }
    
                    if (alfresco != null)
                    {
                        return getPropertyValue(alfresco, name);
                    }
                }
                
                if (isArchiveCoreProperty(name))
                {
                    JSONObject archive = null;
                    try
                    {
                        archive = summary.getJSONObject("archive");
                    }
                    catch (JSONException e)
                    {
                        // The core might be absent.
                        LOGGER.debug("Node archive not found", e);
                    }
    
                    if (archive != null)
                    {
                        return getPropertyValue(archive, name);
                    }
                }

                // Did not find the property in JSON or the core is turned off
                return "Unavailable";
            }
            catch (QueryParserException lqe)
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
