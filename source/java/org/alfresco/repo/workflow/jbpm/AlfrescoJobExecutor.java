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
package org.alfresco.repo.workflow.jbpm;

import org.alfresco.repo.lock.JobLockService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.JbpmConfiguration;
import org.jbpm.job.executor.JobExecutor;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springmodules.workflow.jbpm31.JbpmFactoryLocator;


/**
 * jBPM Job Executor 
 *  
 * @author davidc
 */
public class AlfrescoJobExecutor extends JobExecutor
{
    private static final long serialVersionUID = -4576396495395482111L;
    
    private static Log log = LogFactory.getLog(JobExecutor.class);
    private TransactionService transactionService;
    
    private JbpmConfiguration jbpmConfiguration;
    
    private JobLockService jobLockService;
    private boolean jobExecutorLockEnabled = true;
    
    public void setJobExecutorLockEnabled(boolean jobExecutorLockEnabled)
    {
        this.jobExecutorLockEnabled = jobExecutorLockEnabled;
    }
    
    /**
     * Is Alfresco Job Executor Lock Enabled
     * 
     * @return true if only one executor thread allowed (including across cluster)
     * 
     * @since 3.2
     */
    public boolean getJobExecutorLockEnabled()
    {
        return this.jobExecutorLockEnabled;
    }
    
    /**
     * Gets Transaction Service
     * 
     * @return  transaction service
     */
    public TransactionService getTransactionService()
    {
        return transactionService;
    }
    
    /**
     * Gets Job Lock Service
     * 
     * @return  job lock service
     * 
     * @since 3.2
     */
    public JobLockService getJobLockService()
    {
        return jobLockService;
    }
    
    /**
     * Constructor
     */
    public AlfrescoJobExecutor()
    {
        BeanFactoryLocator factoryLocator = new JbpmFactoryLocator();
        BeanFactoryReference factory = factoryLocator.useBeanFactory(null);
        
        transactionService = (TransactionService)factory.getFactory().getBean(ServiceRegistry.TRANSACTION_SERVICE.getLocalName());
        jobLockService = (JobLockService)factory.getFactory().getBean(ServiceRegistry.JOB_LOCK_SERVICE.getLocalName());
        
        jbpmConfiguration = (JbpmConfiguration)factory.getFactory().getBean("jbpm_configuration");
    }
    
    
    /**
     * {@inheritDoc}
      */
    @SuppressWarnings("unchecked")
    @Override
    protected synchronized void startThread()
    {
        String threadName = getNextThreadName();
        Thread thread = new AlfrescoJobExecutorThread(threadName, this, jbpmConfiguration, getIdleInterval(), getMaxIdleInterval(), getMaxLockTime(), getHistoryMaxSize());
        getThreads().put(threadName, thread);
        log.debug("starting new job executor thread '" + threadName + "'");
        thread.setDaemon(true);
        thread.start();
    }

}
