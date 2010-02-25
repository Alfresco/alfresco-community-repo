/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * and Open Source Software ("FLOSS") applications as described in Alfresco's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing
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
