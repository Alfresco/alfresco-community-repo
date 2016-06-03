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

package org.alfresco.repo.transfer;

import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.transfer.NodeCrawler;
import org.alfresco.service.cmr.transfer.TransferCallback;
import org.alfresco.service.cmr.transfer.TransferDefinition;
import org.alfresco.service.cmr.transfer.TransferEvent;
import org.alfresco.service.cmr.transfer.TransferEventBegin;
import org.alfresco.service.cmr.transfer.TransferEventCommittingStatus;
import org.alfresco.service.cmr.transfer.TransferService;
import org.alfresco.service.cmr.transfer.TransferTarget;

/**
 * @author brian
 *
 */
public class TransferTreeWithCancelActionExecuter extends ActionExecuterAbstractBase
{
    public static final String NAME = "transfer-tree-with-cancel";
    private TransferService transferService;
    private ServiceRegistry serviceRegistry;
    
    /**
     * @param transferService the transferService to set
     */
    public void setTransferService(TransferService transferService)
    {
        this.transferService = transferService;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        TransferTarget target = serviceRegistry.getTransactionService().getRetryingTransactionHelper().doInTransaction(
                new RetryingTransactionHelper.RetryingTransactionCallback<TransferTarget>()
        {
            public TransferTarget execute() throws Throwable
            {
                return TransferTestUtil.getTestTarget(transferService);
            }
        }, false, true);
        
        NodeCrawler crawler = new StandardNodeCrawlerImpl(serviceRegistry);
        crawler.setNodeFinders(new ChildAssociatedNodeFinder(ContentModel.ASSOC_CONTAINS));
        Set<NodeRef> nodes = crawler.crawl(actionedUponNodeRef);
        TransferDefinition td = new TransferDefinition();
        td.setNodes(nodes);
        transferService.transferAsync(target.getName(), td, new TransferCallback(){

            private String transferId;

            public void processEvent(TransferEvent event)
            {
                if (event instanceof TransferEventBegin) 
                {
                    transferId = ((TransferEventBegin)event).getTransferId();
                }
                else if (event instanceof TransferEventCommittingStatus)
                {
                    transferService.cancelAsync(transferId);
                }
            }
            
        });
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
    }
}
