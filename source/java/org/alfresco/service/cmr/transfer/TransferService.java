/*
 * Copyright (C) 2009-2011 Alfresco Software Limited.
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

package org.alfresco.service.cmr.transfer;

import java.util.Collection;
import java.util.Set;

import org.alfresco.service.PublicService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.Auditable;
import org.alfresco.service.NotAuditable;

/**
 * The transfer service is responsible for transferring nodes between one instance of Alfresco and another remote instance.
 * as well as the transfer method, this interface also provides methods for managing transfer targets.
 * 
 * @see TransferService2
 * 
 * @author Mark Rogers
 */
@Deprecated
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
    @Auditable(parameters={"targetName"})
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
   @Auditable(parameters={"targetName"})
   public NodeRef transfer(String targetName, TransferDefinition definition, Collection<TransferCallback> callback) throws TransferException;
   
   /**
    * Transfer nodes sync, with callback.  This synchronous version of the transfer method waits for the transfer to complete 
    * before returning to the caller.  Callbacks are called in the current thread context, so will be associated with the current 
    * transaction and user.
    *  
    * @param targetName the name of the target to transfer to
    * @param definition - the definition of the transfer.   Specifies which nodes to transfer. 
    * The following properties must be set, nodes
    * @param callbacks - a list of callback handlers that will be called as transfer proceeds.  May be null.
    * @throws TransferException
    * @return the node reference of the transfer report
    */
  @Auditable(parameters={"targetName"})
  public NodeRef transfer(String targetName, TransferDefinition definition, TransferCallback... callbacks) throws TransferException;
  
  /**
   * Transfer nodes async with callback.   The asynchronous version of the transfer method starts a transfer and returns as 
   * soon as possible.
   * 
   * The transfer callbacks will be called by a different thread to that used to call the transferAsync method so transaction 
   * context will be different to the calling context. The asychronous transfer does not have access to uncommitted 
   * data in the calling transaction.
   * 
   * @param targetName the name of the target to transfer to
   * @param definition - the definition of the transfer. Specifies which nodes to transfer.  
   * The following properties must be set, nodes
   * @param callback - a collection of callback handlers that will be called as transfer proceeds.  May be null.
   * 
   * @throws TransferException
   */
  @Auditable(parameters={"targetName"})
  public void transferAsync(String targetName, TransferDefinition definition, Collection<TransferCallback> callback) throws TransferException;

  /**
   * Transfer nodes async with callback.   The asynchronous version of the transfer method starts a transfer and returns as 
   * soon as possible.
   * 
   * The transfer callbacks will be called by a different thread to that used to call the transferAsync method so transaction 
   * context will be different to the calling context. The asychronous transfer does not have access to uncommitted 
   * data in the calling transaction.
   * 
   * @param targetName the name of the target to transfer to
   * @param definition - the definition of the transfer. Specifies which nodes to transfer.  
   * The following properties must be set, nodes
   * @param callbacks - a collection of callback handlers that will be called as transfer proceeds.  May be null.
   * 
   * @throws TransferException
   */  
   @Auditable(parameters={"targetName"})
   public void transferAsync(String targetName, TransferDefinition definition, TransferCallback... callbacks) throws TransferException;

   /**
    * Verify a target is available and that the configured credentials are valid.     
    * @throws TransferException  
    */
   @NotAuditable
   public void verify(TransferTarget target) throws TransferException;
    
    /**
     * Create and save a new transfer target.  Creates and saves a new transfer target with a single, but long, method call.
     *  
     * @param name, the name of this transfer target, which must be unique
     * @param title, the display name of this transfer target
     * @param description,
     * @param endpointProtocol, either http or https 
     * @param endpointHost, 
     * @param endpointPort,
     * @param endpointPath, 
     * @param username, 
     * @param password,
     * @return the newly created transfer target.
     */
    @Auditable
    public TransferTarget createAndSaveTransferTarget(String name, String title, String description, String endpointProtocol, 
            String endpointHost, int endpointPort, String endpointPath, String username, char[] password) throws TransferException;
    
    /**
     * Creates an in memory transfer target.  Before it is used it must be populated with the following values and
     * saved with the saveTransferTarget method.   The name of the transfer target must be unique.
     * <ul>
     * <li>title</li>
     * <li>description</li>
     * <li>endpointProtocol</li> 
     * <li>endpointHost</li>
     * <li>endpointPort</li>
     * <li>endpointPath</li>
     * <li>username</li>
     * <li>password</li>
     * </ul>
     * @return an in memory transfer target
     */
    @Auditable(parameters={"name"})
    public TransferTarget createTransferTarget(String name);

    /**
      * Get all the transfer targets
      */
    @NotAuditable
    public Set<TransferTarget>getTransferTargets() throws TransferException;

    /**
      * Get All the transfer targets for a particular transfer target group.
      * @param groupName, the name of the transfer group
      */
    @NotAuditable
    public Set<TransferTarget>getTransferTargets(String groupName) throws TransferException;
    
    /**
     * Get a transfer target by its name
     * @throws TransferException - target does not exist
     */
    @NotAuditable
    public TransferTarget getTransferTarget(String name) throws TransferException;
    
    /**
     * Test to see if the target with the specified name exists
     * @param name
     * @return true if the specified target exists, and false otherwise
     */
    @NotAuditable
    public boolean targetExists(String name);
    
    /**
     * Delete a transfer target.  After calling this method the transfer target will no longer exist.
     * @throws TransferException - target does not exist
     * @param name, the name of this transfer target,
     */
    @Auditable(parameters={"name"})
    public void deleteTransferTarget(String name) throws TransferException;
    
    /**
     * Save TransferTarget, will create a transfer target if it does not already exist or update an existing transfer target.
     * 
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
    @Auditable
    public TransferTarget saveTransferTarget(TransferTarget update) throws TransferException;
    
    /**
     * Enables/Disables the named transfer target
     * @param name the name of the transfer target
     * @param enable (or false=disable)
     */
    @Auditable(parameters={"name", "enable"})
    public void enableTransferTarget(String name, boolean enable) throws TransferException;
    
    /**
     * Asynchronously cancel an in-progress transfer
     * 
     * This method tells an in-process transfer to give up, rollback and stop as soon as possible.
     * 
     * Depending upon the state of the in-progress transfer, the transfer may still complete, 
     * despite calling this method, however in most cases the transfer will not complete.    
     * 
     * Calling this method for a transfer that does not exist, possibly because it has already finished, has no 
     * effect and will not throw an exception.
     * 
     * The transfer identifier can be obtained from the TransferEventBegin event that is passed to registered callbacks when
     * transfer starts.
     * 
     * @param transferId the unique identifier of the transfer to cancel.
     * 
     * @see TransferEventBegin;
     */
    @Auditable(parameters={"transferId"})
    public void cancelAsync(String transferId);
    
}
