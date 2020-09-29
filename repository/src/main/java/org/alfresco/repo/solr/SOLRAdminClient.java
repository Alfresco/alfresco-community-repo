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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.index.shard.ShardRegistry;
import org.alfresco.repo.search.QueryParserException;
import org.alfresco.repo.search.impl.lucene.JSONAPIResult;
import org.alfresco.repo.search.impl.lucene.JSONAPIResultFactory;
import org.alfresco.repo.search.impl.lucene.SolrActionStatusResult;
import org.alfresco.repo.search.impl.lucene.SolrCommandBackupResult;
import org.alfresco.repo.search.impl.solr.AbstractSolrAdminHTTPClient;
import org.alfresco.repo.search.impl.solr.ExplicitSolrStoreMappingWrapper;
import org.alfresco.repo.search.impl.solr.SolrAdminClientInterface;
import org.alfresco.repo.search.impl.solr.SolrClientUtil;
import org.alfresco.repo.search.impl.solr.SolrStoreMapping;
import org.alfresco.repo.search.impl.solr.SolrStoreMappingWrapper;
import org.alfresco.service.cmr.repository.StoreRef;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.httpclient.HttpClient;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
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
public class SOLRAdminClient extends AbstractSolrAdminHTTPClient
        implements ApplicationEventPublisherAware, DisposableBean, SolrAdminClientInterface 
{

	private String solrPingCronExpression;
	private String baseUrl;

	private ApplicationEventPublisher applicationEventPublisher;
	private SolrTracker solrTracker;
	
    private Scheduler scheduler;
    
    private List<SolrStoreMapping> storeMappings;
    
    private HashMap<StoreRef, SolrStoreMappingWrapper> mappingLookup = new HashMap<>();

    private BeanFactory beanFactory;
    
    private ShardRegistry shardRegistry;
    
    private boolean useDynamicShardRegistration;
    
    public SOLRAdminClient()
	{
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
    
    /**
     * SOLR properties identified by store like "alfresco" or "archive"
     * @param storeMappings
     */
    public void setStoreMappings(List<SolrStoreMapping> storeMappings) {
        this.storeMappings = storeMappings;
    }
    
    /* (non-Javadoc)
     * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
     */
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException
    {
        this.beanFactory = beanFactory;
    }
    
    public void setShardRegistry(ShardRegistry shardRegistry) {
        this.shardRegistry = shardRegistry;
    }
    
    public void setUseDynamicShardRegistration(boolean useDynamicShardRegistration) {
        this.useDynamicShardRegistration = useDynamicShardRegistration;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception
    {
        mappingLookup.clear();
        for(SolrStoreMapping mapping : storeMappings)
        {
            mappingLookup.put(mapping.getStoreRef(), new ExplicitSolrStoreMappingWrapper(mapping, beanFactory));
        }
    }

    public void init()
	{
		this.solrTracker = new SolrTracker(scheduler);
	}
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.solr.SolrAdminClient#executeAction(org.alfresco.repo.search.impl.solr.SolrAdminClient.ACTION, java.util.Map)
     */
    @Override
    public JSONAPIResult executeAction(String core, JSONAPIResultFactory.ACTION action, Map<String, String> parameters) {
        
        StoreRef store = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
        SolrStoreMappingWrapper mapping = 
                SolrClientUtil.extractMapping(store, 
                                              mappingLookup,
                                              shardRegistry, 
                                              useDynamicShardRegistration,
                                              beanFactory);
        
        HttpClient httpClient = mapping.getHttpClientAndBaseUrl().getFirst();
        
        StringBuilder url = new StringBuilder();
        url.append(baseUrl);
     
        if(!url.toString().endsWith("/"))
        {
            url.append("/");
        }
        url.append("admin/cores");
        
        URLCodec encoder = new URLCodec();
        url.append("?action=" + action);
        parameters.forEach((key, value) -> {
            try {
                url.append("&" + key + "=" + encoder.encode(value));
            } catch (EncoderException e) {
                throw new RuntimeException(e);
            }
        });

        url.append("&alfresco.shards=");
        if(mapping.isSharded())
        {
            url.append(mapping.getShards());
        }
        else
        {
            String solrurl = httpClient.getHostConfiguration().getHostURL() + mapping.getHttpClientAndBaseUrl().getSecond();
            url.append(solrurl);
        }
        if (core != null)
        {
            url.append("&core=" + core);
        }
        
        try 
        {   
            
            return JSONAPIResultFactory.buildActionResult(action, getOperation(httpClient, url.toString()));
        
        }
        catch (IOException e)
        {
            throw new QueryParserException("action", e);
        }
        
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.search.impl.solr.SolrAdminClient#executeCommand(java.lang.String, org.alfresco.repo.search.impl.solr.SolrAdminClient.HANDLER, org.alfresco.repo.search.impl.solr.SolrAdminClient.COMMAND, java.util.Map)
     */
    @Override
    public JSONAPIResult executeCommand(String core, JSONAPIResultFactory.HANDLER handler, JSONAPIResultFactory.COMMAND command, Map<String, String> parameters) {
        
        StoreRef store = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;
        SolrStoreMappingWrapper mapping = 
                SolrClientUtil.extractMapping(store, 
                                              mappingLookup,
                                              shardRegistry, 
                                              useDynamicShardRegistration,
                                              beanFactory);
        
        HttpClient httpClient = mapping.getHttpClientAndBaseUrl().getFirst();
        
        StringBuilder url = new StringBuilder();
        url.append(baseUrl);
        
        if(!url.toString().endsWith("/"))
        {
            url.append("/");
        }
        
        url.append(core + "/" + handler.toString().toLowerCase());
        
        URLCodec encoder = new URLCodec();
        url.append("?command=" + command.toString().toLowerCase());
        parameters.forEach((key, value) -> {
            try {
                url.append("&" + key + "=" + encoder.encode(value));
            } catch (EncoderException e) {
                throw new RuntimeException(e);
            }
        });

        url.append("&alfresco.shards=");
        if(mapping.isSharded())
        {
            url.append(mapping.getShards());
        }
        else
        {
           String solrurl = httpClient.getHostConfiguration().getHostURL() + mapping.getHttpClientAndBaseUrl().getSecond();
            url.append(solrurl);
        }
        
        try {
        
            JSONAPIResult response = new SolrCommandBackupResult(getOperation(httpClient, url.toString()));
    
            if(response.getStatus() != 0)
            {
                solrTracker.setSolrActive(false);
            }
            
            return response;
            
        }
        catch (IOException e)
        {
            throw new QueryParserException("action", e);
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
		    
		    SolrActionStatusResult result = (SolrActionStatusResult) executeAction(null, JSONAPIResultFactory.ACTION.STATUS, JSON_PARAM);
		    
		    if(result != null)
		    {
			    registerCores(result.getCores());
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
                JobDetail existingJob = scheduler.getJobDetail(new JobKey(jobName, jobGroup));
                if (existingJob != null)
                {
                    scheduler.deleteJob(existingJob.getKey());
                }
                JobDataMap jobDataMap = new JobDataMap();
                jobDataMap.put("SOLR_TRACKER", this);
                final JobDetail jobDetail = JobBuilder.newJob()
                        .withIdentity(jobName, jobGroup)
                        .usingJobData(jobDataMap)
                        .ofType(SOLRWatcherJob.class)
                        .build();
	            trigger = TriggerBuilder.newTrigger()
                        .withIdentity("rmt")
                        .withSchedule(CronScheduleBuilder.cronSchedule(solrPingCronExpression))
                        .build();
	            scheduler.scheduleJob(jobDetail, trigger);
	    	}
	    	catch(Exception e)
	    	{
	    		throw new AlfrescoRuntimeException("Unable to set up SOLRTracker timer", e);
	    	}
	    }
	    
	    protected void startTimer() throws SchedulerException
	    {
	    	scheduler.resumeTrigger(trigger.getKey());
	    }
	    
	    protected void stopTimer() throws SchedulerException
	    {
	    	scheduler.pauseTrigger(trigger.getKey());
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
