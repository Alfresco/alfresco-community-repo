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

import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementAction;
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
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
 * The Disposition Lifecycle Job Finds all disposition action nodes which are
 * for "retain" or "cutOff" actions Where asOf > now OR
 * dispositionEventsEligible = true; 
 * 
 * Runs the cut off or retain action for
 * elligible records. 
 * 
 * @author mrogers
 */
public class DispositionLifecycleJob implements Job
{
    private static Log logger = LogFactory.getLog(DispositionLifecycleJob.class);

    /**
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        final RecordsManagementActionService rmActionService = (RecordsManagementActionService) context
                    .getJobDetail().getJobDataMap().get("recordsManagementActionService");
        final NodeService nodeService = (NodeService) context.getJobDetail().getJobDataMap().get(
                    "nodeService");
        final SearchService search = (SearchService) context.getJobDetail().getJobDataMap().get(
                    "searchService");
        final TransactionService trxService = (TransactionService) context.getJobDetail()
                    .getJobDataMap().get("transactionService");

        logger.debug("Job Starting");

        AuthenticationUtil.runAs(new RunAsWork<Object>()
        {
            public Object doWork() throws Exception
            {
                StringBuilder sb = new StringBuilder();
                sb.append("+TYPE:\"rma:dispositionAction\" ");
                sb.append("+(@rma\\:dispositionAction:(\"cutoff\" OR \"retain\"))");
                sb.append("+ISNULL:\"rma:dispositionActionCompletedAt\" ");
                sb.append("+( ");
                sb.append("@rma\\:dispositionEventsEligible:true "); 
                sb.append("OR @rma\\:dispositionAsOf:[MIN TO NOW] ");
                sb.append(") ");

                String query = sb.toString();

                ResultSet results = search.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
                            SearchService.LANGUAGE_LUCENE, query);
                List<NodeRef> resultNodes = results.getNodeRefs();
                results.close();

                RetryingTransactionHelper trn = trxService.getRetryingTransactionHelper();

                for (NodeRef node : resultNodes)
                {
                    final NodeRef currentNode = node;

                    RetryingTransactionCallback<Boolean> processTranCB = new RetryingTransactionCallback<Boolean>()
                    {
                        public Boolean execute() throws Throwable
                        {
                            final String dispAction = (String) nodeService.getProperty(currentNode,
                                        RecordsManagementModel.PROP_DISPOSITION_ACTION);

                            // Run "retain" and "cutoff" actions.

                            if (dispAction != null)
                            {
                                if (dispAction.equalsIgnoreCase("cutoff") ||
                                    dispAction.equalsIgnoreCase("retain"))
                                {
                                    ChildAssociationRef parent = nodeService.getPrimaryParent(currentNode);
                                    if (parent.getTypeQName().equals(RecordsManagementModel.ASSOC_NEXT_DISPOSITION_ACTION))
                                    {
                                        // Check that the action is executable
                                        RecordsManagementAction rmAction = rmActionService.getDispositionAction(dispAction);
                                        if (rmAction.isExecutable(parent.getParentRef(), null) == true)
                                        {
                                            rmActionService.executeRecordsManagementAction(parent.getParentRef(), dispAction);
                                            if (logger.isDebugEnabled())
                                            {
                                                logger.debug("Processed action: " + dispAction + "on" + parent);
                                            }
                                        }
                                        else
                                        {
                                            logger.debug("The disposition action " + dispAction + " is not executable.");
                                        }
                                    }
                                    return null;
                                }
                            }
                            return Boolean.TRUE;
                        }
                    };
                    /**
                     * Now do the work, one action in each transaction
                     */
                    trn.doInTransaction(processTranCB);
                }
                return null;
            };

        }, AuthenticationUtil.getSystemUserName());

        logger.debug("Job Finished");
    }
}
