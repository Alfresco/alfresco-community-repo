/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.job;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.notification.RecordsManagementNotificationHelper;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This job finds all Vital Records which are due for review, optionally
 * excluding those for which notification has already been issued.
 *
 * @author Neil McErlean
 */
public class NotifyOfRecordsDueForReviewJobExecuter extends RecordsManagementJobExecuter
{
    private static Log logger = LogFactory.getLog(NotifyOfRecordsDueForReviewJobExecuter.class);

    private RecordsManagementNotificationHelper recordsManagementNotificationHelper;

    private NodeService nodeService;

    private SearchService searchService;

    public void setRecordsManagementNotificationHelper(
            RecordsManagementNotificationHelper recordsManagementNotificationHelper)
    {
        this.recordsManagementNotificationHelper = recordsManagementNotificationHelper;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.job.RecordsManagementJobExecuter#execute()
     */
    public void executeImpl()
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Job " + this.getClass().getSimpleName() + " starting.");
        }

        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            public Object doWork()
            {
                // Query is for all records that are due for review and for which
                // notification has not been sent.
                StringBuilder queryBuffer = new StringBuilder();
                queryBuffer.append("ASPECT:\"rma:vitalRecord\" ");
                queryBuffer.append("AND @rma\\:reviewAsOf:[MIN TO NOW] ");
                // exclude destroyed electronic records and destroyed nonElectronic records with kept metadata
                queryBuffer.append("AND -ASPECT:\"rma:ghosted\" ");
                String query = queryBuffer.toString();

                ResultSet results = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_FTS_ALFRESCO, query);
                final List<NodeRef> resultNodes = results.getNodeRefs();
                results.close();

                if (logger.isDebugEnabled())
                {
                    logger.debug("Found " + resultNodes.size() + " nodes due for review and without notification.");
                }

                //If we have something to do and a template to do it with
                if(resultNodes.size() != 0)
                {
                    //Send the email message - but we must not retry since email is not transactional
                    RetryingTransactionCallback<Void> txCallbackSendEmail = new RetryingTransactionCallback<Void>()
                    {
                        // Set the notification issued property.
                        public Void execute()
                        {
                            // Send notification
                            recordsManagementNotificationHelper.recordsDueForReviewEmailNotification(resultNodes);

                            return null;
                        }
                    };

                    /**
                     * Now do the work, one action in each transaction
                     */
                    // don't retry the send email
                    retryingTransactionHelper.setMaxRetries(0);
                    retryingTransactionHelper.doInTransaction(txCallbackSendEmail);
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


