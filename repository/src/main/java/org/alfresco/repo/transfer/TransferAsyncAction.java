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
