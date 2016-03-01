 
package org.alfresco.module.org_alfresco_module_rm.action.impl;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.module.org_alfresco_module_rm.transfer.TransferService;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Transfer complete action
 *
 * @author Roy Wetherall
 */
public class TransferCompleteAction extends RMActionExecuterAbstractBase
{
    /** I18N */
    private static final String MSG_NODE_NOT_TRANSFER = "rm.action.node-not-transfer";

    /** Action name */
    public static final String NAME = "transferComplete";

    /** Transfer service */
    private TransferService transferService;

    /**
     * @return transfer service
     */
    protected TransferService getTransferService()
    {
        return this.transferService;
    }

    /**
     * @param transferService transfer service
     */
    public void setTransferService(TransferService transferService)
    {
        this.transferService = transferService;
    }

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action,
     *      org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        checkTransferSubClass(actionedUponNodeRef);
        getTransferService().completeTransfer(actionedUponNodeRef);
    }

    /**
     * Checks if the actioned upon node reference is a sub class of transfer
     *
     * @param actionedUponNodeRef actioned upon node reference
     */
    private void checkTransferSubClass(NodeRef actionedUponNodeRef)
    {
        QName type = getNodeService().getType(actionedUponNodeRef);
        if (!getDictionaryService().isSubClass(type, TYPE_TRANSFER))
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_NODE_NOT_TRANSFER));
        }
    }
}
