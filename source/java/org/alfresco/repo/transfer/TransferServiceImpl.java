/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.transfer.TransferCallback;
import org.alfresco.service.cmr.transfer.TransferCancelledException;
import org.alfresco.service.cmr.transfer.TransferDefinition;
import org.alfresco.service.cmr.transfer.TransferEndEvent;
import org.alfresco.service.cmr.transfer.TransferEventCancelled;
import org.alfresco.service.cmr.transfer.TransferException;
import org.alfresco.service.cmr.transfer.TransferService;
import org.alfresco.service.cmr.transfer.TransferTarget;


/**
 * Implementation of the Transfer Service.
 * 
 * Note: The TransferService interface is now deprecated (replaced by TransferService2). This implementation
 *       delegates to the implementation of TransferService2.
 * 
 * @author davidc
 *
 */
public class TransferServiceImpl implements TransferService
{
    private TransferServiceImpl2 transferServiceImpl2;
    
    public void setTransferServiceImpl2(TransferServiceImpl2 transferServiceImpl2)
    {
        this.transferServiceImpl2 = transferServiceImpl2;
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.transfer.TransferService#createTransferTarget(java.lang.String)
     */
    public TransferTarget createTransferTarget(String name)
    {
        return transferServiceImpl2.createTransferTarget(name);
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.transfer.TransferService#createAndSaveTransferTarget(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, int, java.lang.String, java.lang.String, char[])
     */
    public TransferTarget createAndSaveTransferTarget(String name, String title, String description, String endpointProtocol, String endpointHost, int endpointPort, String endpointPath, String username, char[] password)
    {
        return transferServiceImpl2.createAndSaveTransferTarget(name, title, description, endpointProtocol, endpointHost, endpointPort, endpointPath, username, password);
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.transfer.TransferService#getTransferTargets()
     */
    public Set<TransferTarget> getTransferTargets()
    {
        return transferServiceImpl2.getTransferTargets();
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.transfer.TransferService#getTransferTargets(java.lang.String)
     */
    public Set<TransferTarget> getTransferTargets(String groupName)
    {
        return transferServiceImpl2.getTransferTargets(groupName);
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.transfer.TransferService#deleteTransferTarget(java.lang.String)
     */
    public void deleteTransferTarget(String name)
    {
        transferServiceImpl2.deleteTransferTarget(name);
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.transfer.TransferService#enableTransferTarget(java.lang.String, boolean)
     */
    public void enableTransferTarget(String name, boolean enable)
    {
        transferServiceImpl2.enableTransferTarget(name, enable);
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.transfer.TransferService#targetExists(java.lang.String)
     */
    public boolean targetExists(String name)
    {
        return transferServiceImpl2.targetExists(name);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.transfer.TransferService#getTransferTarget(java.lang.String)
     */
    public TransferTarget getTransferTarget(String name)
    {
        return transferServiceImpl2.getTransferTarget(name);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.transfer.TransferService#saveTransferTarget(org.alfresco.service.cmr.transfer.TransferTarget)
     */
    public TransferTarget saveTransferTarget(TransferTarget update)
    {
        return transferServiceImpl2.saveTransferTarget(update);
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.transfer.TransferService#transferAsync(java.lang.String, org.alfresco.service.cmr.transfer.TransferDefinition, org.alfresco.service.cmr.transfer.TransferCallback[])
     */
    public void transferAsync(String targetName, TransferDefinition definition, TransferCallback... callbacks)
    {
        transferServiceImpl2.transferAsync(targetName, definition, callbacks);
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.transfer.TransferService#transferAsync(java.lang.String, org.alfresco.service.cmr.transfer.TransferDefinition, java.util.Collection)
     */
    public void transferAsync(String targetName, TransferDefinition definition, Collection<TransferCallback> callbacks)
    {
        transferServiceImpl2.transferAsync(targetName, definition, callbacks);
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.transfer.TransferService#transfer(java.lang.String, org.alfresco.service.cmr.transfer.TransferDefinition)
     */
    public NodeRef transfer(String targetName, TransferDefinition definition)
    {
        return transfer(targetName, definition, new TransferCallback[]{});
    }
    
    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.transfer.TransferService#transfer(java.lang.String, org.alfresco.service.cmr.transfer.TransferDefinition, org.alfresco.service.cmr.transfer.TransferCallback[])
     */
    public NodeRef transfer(String targetName, TransferDefinition definition, TransferCallback... callbacks)
    {
        return transfer(targetName, definition, Arrays.asList(callbacks));
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.transfer.TransferService#transfer(java.lang.String, org.alfresco.service.cmr.transfer.TransferDefinition, java.util.Collection)
     */
    public NodeRef transfer(String targetName, TransferDefinition definition, Collection<TransferCallback> callbacks)
    {
        TransferEndEvent event = transferServiceImpl2.transfer(targetName, definition, callbacks);
        if (event instanceof TransferEventCancelled)
        {
            // NOTE: throw this exception to keep compatibility with TransferService contract
            throw new TransferCancelledException();
        }
        return event.getSourceReport();
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.transfer.TransferService#verify(org.alfresco.service.cmr.transfer.TransferTarget)
     */
    public void verify(TransferTarget target) throws TransferException
    {
        transferServiceImpl2.verify(target);
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.transfer.TransferService#cancelAsync(java.lang.String)
     */
    public void cancelAsync(String transferId)
    {
        transferServiceImpl2.cancelAsync(transferId);
    }

}
