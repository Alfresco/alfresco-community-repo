
package org.alfresco.repo.transfer;

import java.util.Collection;
import java.util.List;

import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.transfer.TransferCallback;
import org.alfresco.service.cmr.transfer.TransferDefinition;
import org.alfresco.service.cmr.transfer.TransferService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author markr
 */
public class TransferAsyncAction extends ActionExecuterAbstractBase
{
    public static final String ASYNC_QUEUE_NAME = "deployment";

    private TransferService transferService;

    private static Log logger = LogFactory.getLog(TransferAsyncAction.class);

    public void init()
    {
        super.name = "transfer-async";
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        logger.debug("In TransferAsyncAction");

        String targetName = (String) action.getParameterValue("targetName");
        TransferDefinition definition = (TransferDefinition) action.getParameterValue("definition");
        Collection<TransferCallback> callback = (Collection<TransferCallback>) action.getParameterValue("callbacks");

        transferService.transfer(targetName, definition, callback);
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
    }

    public void setTransferService(TransferService transferService)
    {
        this.transferService = transferService;
    }

    public TransferService getTransferService()
    {
        return transferService;
    }
}
