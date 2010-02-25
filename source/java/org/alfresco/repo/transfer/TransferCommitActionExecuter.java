package org.alfresco.repo.transfer;

import java.util.List;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.transfer.TransferReceiver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TransferCommitActionExecuter extends ActionExecuterAbstractBase
{
    private Log log = LogFactory.getLog(TransferCommitActionExecuter.class);
    
    public static final String NAME = "commit-transfer";
    public static final String PARAM_TRANSFER_ID = "transfer-id";

    private TransferReceiver receiver;

    public void setReceiver(TransferReceiver receiver)
    {
        this.receiver = receiver;
    }

    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        String transferId = (String)action.getParameterValue(PARAM_TRANSFER_ID); 
        if (log.isDebugEnabled())
        {
            log.debug("Transfer id = " + transferId);
        }
        receiver.commit(transferId);
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        paramList.add(new ParameterDefinitionImpl(PARAM_TRANSFER_ID, DataTypeDefinition.TEXT, true,
                getParamDisplayLabel(PARAM_TRANSFER_ID)));
    }
}
