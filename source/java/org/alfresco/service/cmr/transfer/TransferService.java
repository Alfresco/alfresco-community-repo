/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
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
 * http://www.alfresco.com/legal/licensing"
 */

package org.alfresco.service.cmr.transfer;

import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * The transfer service is responsible for transfering nodes between one instance of Alfresco and another remote instance.
 * as well as the transfer method, this interface also provides methods for managing the  
 * 
 * @author Mark Rogers
 */
public interface TransferService
{

    /**
      * Transfer nodes, sync.  This synchronous version of the transfer method waits for the transfer to complete 
      * before returning to the caller.  Callbacks are called in the current thread context, so will be associated with the current 
      * transaction and user.
      * 
      * @param targetName the name of the target to transfer to
      * The following properties must be set, nodes
      * @param definition, the definition of the transfer. Specifies which nodes to transfer. 
      * @throws TransferException
      * @return the node reference of the transfer report
      */       
    public NodeRef transfer(String targetName, TransferDefinition definition) throws TransferException;
    
    /**
     * Transfer nodes sync, with callback.  This synchronous version of the transfer method waits for the transfer to complete 
     * before returning to the caller.  Callbacks are called in the current thread context, so will be associated with the current 
     * transaction and user.
     *  
     * @param targetName the name of the target to transfer to
     * @param definition - the definition of the transfer.   Specifies which nodes to transfer. 
     * The following properties must be set, nodes
     * @param callback - a set of callback handlers that will be called as transfer proceeds.  May be null.
     * @throws TransferException
     * @return the node reference of the transfer report
     */       
   public NodeRef transfer(String targetName, TransferDefinition definition, Set<TransferCallback> callback) throws TransferException;
   
   /**
    * Transfer nodes async with callback.   The asynchronous version of the transfer method starts a transfer and returns as 
    * soon as possible.
    * The transfer callbacks will be called by a different thread to that used to call the transferAsync method so transaction 
    * context will be different to the calling context. 
    * 
    * Please also be aware that the asychronous transfer does not have access to uncommitted 
    * data in the calling transaction.
    * 
    * @param targetName the name of the target to transfer to
    * @param definition - the definition of the transfer. Specifies which nodes to transfer.  
    * The following properties must be set, nodes
    * @param callback - a set of callback handlers that will be called as transfer proceeds.  May be null.
    * @throws TransferException
    */       
   public void transferAsync(String targetName, TransferDefinition definition, Set<TransferCallback> callback) throws TransferException;

    /**
      * Verify a target is available and that the configured credentials correctly identify an admin user.     
      * @throws TransferException  
      */
    public void verify(TransferTarget target) throws TransferException;
    
    /**
     * crate a new transfer target 
     * @param name, the name of this transfer target, which must be unique
     * @param title, the display name of this transfer target
     * @param description,
     * @param endpointProtocol, either http or https 
     * @param endpointHost, 
     * @param endpointPort,
     * @param endpointPath, 
     * @param username, 
     * @param password,
     */
    public TransferTarget createTransferTarget(String name, String title, String description, String endpointProtocol, String endpointHost, int endpointPort, String endpointPath, String username, char[] password) throws TransferException;

    /**
      * Get all the transfer targets
      */
    public Set<TransferTarget>getTransferTargets() throws TransferException;

    /**
      * Get All the transfer targets for a particular transfer target group.
      * @param groupName, the name of the transfer group
      */
    public Set<TransferTarget>getTransferTargets(String groupName) throws TransferException;
    
    /**
     * Get a transfer target by its name
     * @throws TransferException - target does not exist
     */
    public TransferTarget getTransferTarget(String name) throws TransferException;
    
    /**
     * Delete a transfer target.  After calling this method the transfer target will no longer exist.
     * @throws TransferException - target does not exist
     * @param name, the name of this transfer target,
     */
    public void deleteTransferTarget(String name) throws TransferException;
    
    /**
     * Update TransferTarget
     * The following properties may be updated:
     *    endpointHost,
     *    endpointPort,
     *    endpointProtocol,
     *    endpointPath,
     *    username,
     *    password,  
     *    title,
     *    description
     *    
     * The following properties may not be updated:
     *    name, must be specified.
     *    nodeRef, if specified will be ignored.
     *    
     *    @param update
     */
    public TransferTarget updateTransferTarget(TransferTarget update) throws TransferException;
    
    /**
     * Enables/Disables the named transfer target
     * @param name the name of the transfer target
     * @param enable (or false=disable)
     */
    public void enableTransferTarget(String name, boolean enable) throws TransferException;
    
}
