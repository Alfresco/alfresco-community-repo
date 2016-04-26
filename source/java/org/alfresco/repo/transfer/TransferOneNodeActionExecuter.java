
package org.alfresco.repo.transfer;

import java.util.List;

import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.transfer.TransferDefinition;
import org.alfresco.service.cmr.transfer.TransferService;
import org.alfresco.service.cmr.transfer.TransferTarget;

/**
 * @author brian
 *
 */
public class TransferOneNodeActionExecuter extends ActionExecuterAbstractBase
{
    public static final String NAME = "transfer-this-node";
    private TransferService transferService;
    
    /**
     * @param transferService the transferService to set
     */
    public void setTransferService(TransferService transferService)
    {
        this.transferService = transferService;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        TransferTarget target = TransferTestUtil.getTestTarget(transferService);
        TransferDefinition td = new TransferDefinition();
        td.setNodes(actionedUponNodeRef);
        transferService.transfer(target.getName(), td);
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
    }
}
