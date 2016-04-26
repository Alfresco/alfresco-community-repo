
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
