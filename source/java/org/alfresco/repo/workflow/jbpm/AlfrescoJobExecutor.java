/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.workflow.jbpm;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

    
    /**
     * Constructor
     */
    public AlfrescoJobExecutor()
    {
        BeanFactoryLocator factoryLocator = new JbpmFactoryLocator();
        BeanFactoryReference factory = factoryLocator.useBeanFactory(null);
        transactionService = (TransactionService)factory.getFactory().getBean(ServiceRegistry.TRANSACTION_SERVICE.getLocalName());
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

    /* (non-Javadoc)
     * @see org.jbpm.job.executor.JobExecutor#startThread()
     */
    @SuppressWarnings("unchecked")
    protected synchronized void startThread()
    {
        String threadName = getNextThreadName();
        Thread thread = new AlfrescoJobExecutorThread(threadName, this, getJbpmConfiguration(), getIdleInterval(), getMaxIdleInterval(), getMaxLockTime(), getHistoryMaxSize());
        getThreads().put(threadName, thread);
        log.debug("starting new job executor thread '" + threadName + "'");
        thread.start();
    }

}
