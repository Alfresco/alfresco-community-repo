/*
 * Copyright (C) 2009-2011 Alfresco Software Limited.
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

package org.alfresco.module.org_alfresco_module_rm.job;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.notification.RecordsManagementNotificationHelper;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * This job finds all Vital Records which are due for review, optionally
 * excluding those for which notification has already been issued.
 * 
 * @author Neil McErlean
 */
public class NotifyOfRecordsDueForReviewJob implements Job
{
    private static Log logger = LogFactory.getLog(NotifyOfRecordsDueForReviewJob.class);
    
    /**
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        final RecordsManagementNotificationHelper notificationHelper = (RecordsManagementNotificationHelper)context.getJobDetail().getJobDataMap().get("recordsManagementNotificationHelper");
        final NodeService nodeService = (NodeService) context.getJobDetail().getJobDataMap().get("nodeService");
        final SearchService searchService = (SearchService) context.getJobDetail().getJobDataMap().get("searchService");
        final TransactionService trxService = (TransactionService) context.getJobDetail().getJobDataMap().get("transactionService");  
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Job " + this.getClass().getSimpleName() + " starting.");
        }

        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                // Query is for all records that are due for review and for which
                // notification has not been sent.
                StringBuilder queryBuffer = new StringBuilder();
                queryBuffer.append("+ASPECT:\"rma:vitalRecord\" ");                
                queryBuffer.append("+(@rma\\:reviewAsOf:[MIN TO NOW] ) ");
                queryBuffer.append("+( ");
                queryBuffer.append("@rma\\:notificationIssued:false "); 
                queryBuffer.append("OR ISNULL:\"rma:notificationIssued\" ");
                queryBuffer.append(") ");                
                String query = queryBuffer.toString();

                ResultSet results = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_LUCENE, query);             
                final List<NodeRef> resultNodes = results.getNodeRefs();
                results.close();
                
                if (logger.isDebugEnabled() == true)
                {
                    logger.debug("Found " + resultNodes.size() + " nodes due for review and without notification.");
                }                
                    
                //If we have something to do and a template to do it with
                if(resultNodes.size() != 0)
                {
                    RetryingTransactionHelper trn = trxService.getRetryingTransactionHelper();
                    
                    //Send the email message - but we must not retry since email is not transactional
                    RetryingTransactionCallback<Boolean> txCallbackSendEmail = new RetryingTransactionCallback<Boolean>()
                    {
                        // Set the notification issued property.
                        public Boolean execute() throws Throwable
                        {
                            // Send notification
                            notificationHelper.recordsDueForReviewEmailNotification(resultNodes);
                                    
                            return null;
                        }
                    };
                    
                    RetryingTransactionCallback<Boolean> txUpdateNodesCallback = new RetryingTransactionCallback<Boolean>()
                    {
                        // Set the notification issued property.
                        public Boolean execute() throws Throwable
                        {
                            for (NodeRef node : resultNodes)
                            {
                                nodeService.setProperty(node, RecordsManagementModel.PROP_NOTIFICATION_ISSUED, "true");
                            }
                            return Boolean.TRUE;
                        }
                    };
      
                    /**
                     * Now do the work, one action in each transaction
                     */
                    trn.setMaxRetries(0);   // don't retry the send email
                    trn.doInTransaction(txCallbackSendEmail);
                    trn.setMaxRetries(10);
                    trn.doInTransaction(txUpdateNodesCallback);
                }
                return null;
            }
            
        }, AuthenticationUtil.getSystemUserName());

        if (logger.isDebugEnabled())
        {
            logger.debug("Job " + this.getClass().getSimpleName() + " finished");
        }     
    }  // end of execute method
        
}


