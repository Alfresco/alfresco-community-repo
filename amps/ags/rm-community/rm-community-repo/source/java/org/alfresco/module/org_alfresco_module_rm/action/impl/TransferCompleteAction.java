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
