/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
