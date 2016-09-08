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
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
public class DispositionLifecycleJobExecuter extends RecordsManagementJobExecuter
{
    private static Log logger = LogFactory.getLog(DispositionLifecycleJobExecuter.class);

    private RecordsManagementActionService recordsManagementActionService;
    
    private NodeService nodeService;
    
    private SearchService searchService;
    
    public void setRecordsManagementActionService(RecordsManagementActionService recordsManagementActionService)
    {
        this.recordsManagementActionService = recordsManagementActionService;
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
        logger.debug("Job Starting");
       
        StringBuilder sb = new StringBuilder();
        sb.append("+TYPE:\"rma:dispositionAction\" ");
        sb.append("+(@rma\\:dispositionAction:(\"cutoff\" OR \"retain\"))");
        sb.append("+ISNULL:\"rma:dispositionActionCompletedAt\" ");
        sb.append("+( ");
        sb.append("@rma\\:dispositionEventsEligible:true "); 
        sb.append("OR @rma\\:dispositionAsOf:[MIN TO NOW] ");
        sb.append(") ");

        String query = sb.toString();

        ResultSet results = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
                    SearchService.LANGUAGE_LUCENE, query);
        List<NodeRef> resultNodes = results.getNodeRefs();
        results.close();


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
                                RecordsManagementAction rmAction = recordsManagementActionService.getDispositionAction(dispAction);
                                if (rmAction.isExecutable(parent.getParentRef(), null) == true)
                                {
                                    recordsManagementActionService.executeRecordsManagementAction(parent.getParentRef(), dispAction);
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
            retryingTransactionHelper.doInTransaction(processTranCB);
        }
 
        logger.debug("Job Finished");
    }
}
