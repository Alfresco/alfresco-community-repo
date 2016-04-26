
package org.alfresco.repo.transfer;

import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.transfer.NodeCrawler;
import org.alfresco.service.cmr.transfer.TransferDefinition;
import org.alfresco.service.cmr.transfer.TransferService;
import org.alfresco.service.cmr.transfer.TransferTarget;

/**
 * @author brian
 *
 */
public class TransferTreeActionExecuter extends ActionExecuterAbstractBase
{
    public static final String NAME = "transfer-this-tree";
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
        TransferTarget target = TransferTestUtil.getTestTarget(transferService);
        NodeCrawler crawler = new StandardNodeCrawlerImpl(serviceRegistry);
        crawler.setNodeFinders(new ChildAssociatedNodeFinder(ContentModel.ASSOC_CONTAINS));
        Set<NodeRef> nodes = crawler.crawl(actionedUponNodeRef);
        TransferDefinition td = new TransferDefinition();
        td.setNodes(nodes);
        transferService.transfer(target.getName(), td);
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
    }
}
