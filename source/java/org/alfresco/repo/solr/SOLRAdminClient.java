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
package org.alfresco.repo.solr;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.httpclient.HttpClientFactory;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.NamedList;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

/**
 * Provides an interface to the Solr admin APIs, used by the Alfresco Enterprise JMX layer.
 * Also tracks whether Solr is available, sending Spring events when its availability changes.
 * 
 * @since 4.0
 *
 */
public class SOLRAdminClient implements ApplicationEventPublisherAware, DisposableBean
{
	private String solrHost;
	private int solrPort;
	private int solrSSLPort;
	private String solrUrl;
	private String solrUser;
	private String solrPassword;
	private String solrPingCronExpression;
	private String baseUrl;
	private CommonsHttpSolrServer server;
	private int solrConnectTimeout = 30000; // ms

	private ApplicationEventPublisher applicationEventPublisher;
	private SolrTracker solrTracker;
	
	private HttpClientFactory httpClientFactory;
    private Scheduler scheduler;

	public SOLRAdminClient()
	{
	}

	public void setSolrHost(String solrHost)
	{
		this.solrHost = solrHost;
	}
	
	public void setSolrPort(String solrPort)
	{
		this.solrPort = Integer.parseInt(solrPort);
	}
	
	public void setSolrsslPort(int solrSSLPort)
	{
		this.solrSSLPort = solrSSLPort;
	}

	public void setSolrUser(String solrUser)
	{
		this.solrUser = solrUser;
	}

	public void setSolrPassword(String solrPassword)
	{
		this.solrPassword = solrPassword;
	}
	
	public void setSolrConnectTimeout(String solrConnectTimeout)
	{
		this.solrConnectTimeout = Integer.parseInt(solrConnectTimeout);
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher)
	{
		this.applicationEventPublisher = applicationEventPublisher;
	}
	
	public void setSolrPingCronExpression(String solrPingCronExpression)
    {
        this.solrPingCronExpression = solrPingCronExpression;
    }

    public void setHttpClientFactory(HttpClientFactory httpClientFactory)
	{
		this.httpClientFactory = httpClientFactory;
	}
   
    public void setBaseUrl(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    /**
     * @param scheduler the scheduler to set
     */
    public void setScheduler(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    public void init()
	{
    	ParameterCheck.mandatory("solrHost", solrHost);
    	ParameterCheck.mandatory("solrPort", solrPort);
    	ParameterCheck.mandatory("solrUser", solrUser);
    	ParameterCheck.mandatory("solrPassword", solrPassword);
    	ParameterCheck.mandatory("solrPingCronExpression", solrPingCronExpression);
    	ParameterCheck.mandatory("solrConnectTimeout", solrConnectTimeout);

		try
		{
	    	StringBuilder sb = new StringBuilder();
	    	sb.append(httpClientFactory.isSSL() ? "https://" : "http://");
	    	sb.append(solrHost);
	    	sb.append(":");
	    	sb.append(httpClientFactory.isSSL() ? solrSSLPort: solrPort);
	    	sb.append(baseUrl);
			this.solrUrl = sb.toString();
			HttpClient httpClient = httpClientFactory.getHttpClient();

			server = new CommonsHttpSolrServer(solrUrl, httpClient);
			server.setParser(new XMLResponseParser());
			// TODO remove credentials because we're using SSL?
			Credentials defaultcreds = new UsernamePasswordCredentials(solrUser, solrPassword); 
			server.getHttpClient().getState().setCredentials(new AuthScope(solrHost, solrPort, AuthScope.ANY_REALM), 
					defaultcreds);
			server.setConnectionTimeout(solrConnectTimeout);
			server.setSoTimeout(20000);

			this.solrTracker = new SolrTracker(scheduler);
		}
		catch(MalformedURLException e)
		{
			throw new AlfrescoRuntimeException("Cannot initialise Solr admin http client", e);
		}
	}

	public QueryResponse basicQuery(ModifiableSolrParams params)
	{
    	try
    	{
		    QueryResponse response = server.query(params);
		    return response;
		}
		catch(SolrServerException e)
		{
			return null;
		}
	}

	public QueryResponse query(ModifiableSolrParams params) throws SolrServerException
	{
    	try
    	{
		    QueryResponse response = server.query(params);
		    if(response.getStatus() != 0)
		    {
		    	solrTracker.setSolrActive(false);
		    }

		    return response;
		}
		catch(SolrServerException e)
		{
			solrTracker.setSolrActive(false);
			throw e;
		}
	}
	
	public List<String> getRegisteredCores()
	{
		return solrTracker.getRegisteredCores();
	}
	
	/**
	 * Tracks the availability of Solr.
	 * 
	 * @since 4.0
	 *
	 */
	class SolrTracker
	{
	    private final WriteLock writeLock;
		private boolean solrActive = false;

	    private Scheduler scheduler = null;
	    private Trigger trigger;

	    private List<String> cores;

		SolrTracker(Scheduler scheduler)
		{
		    this.scheduler = scheduler;
	        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	        writeLock = lock.writeLock();
	        
	        cores = new ArrayList<String>(5);

	    	setupTimer();
		}
		
		protected void pingSolr()
		{
		    ModifiableSolrParams params = new ModifiableSolrParams();
		    params.set("qt", "/admin/cores");
		    params.set("action", "STATUS");
			
		    QueryResponse response = basicQuery(params);
		    if(response != null && response.getStatus() == 0)
		    {
			    NamedList<Object> results = response.getResponse();
			    @SuppressWarnings("unchecked")
                NamedList<Object> report = (NamedList<Object>)results.get("status");
			    Iterator<Map.Entry<String, Object>> coreIterator = report.iterator();
			    List<String> cores = new ArrayList<String>(report.size());
			    while(coreIterator.hasNext())
			    {
			    	Map.Entry<String, Object> core = coreIterator.next();
			    	cores.add(core.getKey());
			    }
			    
			    registerCores(cores);
		    	setSolrActive(true);
		    }
		    else
		    {
		    	setSolrActive(false);
		    }
		}
		
		void setSolrActive(boolean active)
		{
			boolean statusChanged = false;

			try
			{
		        writeLock.lock();
		        try
		        {
		        	if(solrActive != active)
		        	{
			        	solrActive = active;
			        	statusChanged = true;
		        	}
		        }
		        finally
		        {
		            writeLock.unlock();
		        }
		        
		        if(statusChanged)
		        {
		        	// do this outside the write lock
		        	if(solrActive)
		        	{
		        		stopTimer();
		        		applicationEventPublisher.publishEvent(new SolrActiveEvent(this));
		        	}
		        	else
		        	{
		        		startTimer();
		        		applicationEventPublisher.publishEvent(new SolrInactiveEvent(this));
		        	}
		        }
			}
			catch(Exception e)
			{
				throw new AlfrescoRuntimeException("", e);
			}
		}
		
		boolean isSolrActive()
		{
			return solrActive;
		}

	    protected void setupTimer()
	    {
	    	try
	    	{
                final String jobName = "SolrWatcher";
                final String jobGroup = "Solr";
                
                // If a Quartz job already exists with this name and group then we want to replace it.
                // It is not expected that this will occur during production, but it is possible during automated testing
                // where application contexts could be rebuilt between test cases, leading to multiple creations of
                // equivalent Quartz jobs. Quartz disallows the scheduling of multiple jobs with the same name and group.
                JobDetail existingJob = scheduler.getJobDetail(jobName, jobGroup);
                if (existingJob != null)
                {
                    scheduler.deleteJob(jobName, jobGroup);
                }
		        
		        JobDetail job = new JobDetail(jobName, jobGroup, SOLRWatcherJob.class);
		        JobDataMap jobDataMap = new JobDataMap();
		        jobDataMap.put("SOLR_TRACKER", this);
		        job.setJobDataMap(jobDataMap);

	            trigger = new CronTrigger("SolrWatcherTrigger", jobGroup, solrPingCronExpression);
	            scheduler.scheduleJob(job, trigger);
	    	}
	    	catch(Exception e)
	    	{
	    		throw new AlfrescoRuntimeException("Unable to set up SOLRTracker timer", e);
	    	}
	    }
	    
	    protected void startTimer() throws SchedulerException
	    {
	    	scheduler.resumeTrigger(trigger.getName(), trigger.getGroup());
	    }
	    
	    protected void stopTimer() throws SchedulerException
	    {
	    	scheduler.pauseTrigger(trigger.getName(), trigger.getGroup());
	    }

	    void registerCores(List<String> cores)
	    {
	        writeLock.lock();
	        try
	        {
	        	this.cores = cores;
	        }
	        finally
	        {
	            writeLock.unlock();
	        }
	    }
	    
	    @SuppressWarnings("unchecked")
		List<String> getRegisteredCores()
	    {
	        writeLock.lock();
	        try
	        {
	        	return (cores != null ? cores : Collections.EMPTY_LIST);
	        }
	        finally
	        {
	            writeLock.unlock();
	        }
	    }
	}

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.DisposableBean#destroy()
     */
    @Override
    public void destroy() throws Exception
    {
        solrTracker.stopTimer();
    }

}
