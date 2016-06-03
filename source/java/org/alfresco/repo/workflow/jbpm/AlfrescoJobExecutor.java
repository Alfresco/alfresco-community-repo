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
