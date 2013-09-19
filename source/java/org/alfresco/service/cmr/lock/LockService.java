/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.service.cmr.lock;

import java.util.Collection;
import java.util.List;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.repo.lock.mem.Lifetime;
import org.alfresco.repo.lock.mem.LockState;
import org.alfresco.service.Auditable;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;


/**
 * Interface for public and internal lock operations.
 * 
 * @author Roy Wetherall
 */
@AlfrescoPublicApi
public interface LockService
{
   /**
    * Places a lock on a node.  
    * <p>
    * The lock prevents any other user or process from comitting updates 
    * to the node until the lock is released.  
    * <p>
    * The lock will be owned by the current user.
    * <p>
    * A lock made with this call will never expire.
    * 
    * @param  nodeRef  a reference to a node 
    * @param  userName  a reference to the user that will own the lock
    * @param  lockType the lock type
    * @throws UnableToAquireLockException
    *                  thrown if the lock could not be obtained
    */
   @Auditable(parameters = {"nodeRef", "lockType"})
   public void lock(NodeRef nodeRef, LockType lockType)
       throws UnableToAquireLockException;
   
   /**
    * Places a {@link Lifetime#PERSISTENT persistent} lock on a node.  
    * <p>
    * The lock prevents any other user or process from comitting updates 
    * to the node until the lock is released.  
    * <p>
    * The lock will be owned by the current user.
    * <p>
    * If the time to expire is 0 then the lock will never expire.  Otherwise the
    * timeToExpire indicates the number of seconds before the lock expires.  When
    * a lock expires the lock is considered to have been released.
    * <p>
    * If the node is already locked and the user is the lock owner then the lock will
    * be renewed with the passed timeToExpire.
    * 
    * @param  nodeRef       a reference to a node 
    * @param  lockType      the lock type
    * @param  timeToExpire  the number of seconds before the locks expires.
    * @throws UnableToAquireLockException
    *                       thrown if the lock could not be obtained
    */
   @Auditable(parameters = {"nodeRef", "lockType", "timeToExpire"})
   public void lock(NodeRef nodeRef, LockType lockType, int timeToExpire)
       throws UnableToAquireLockException;

   /**
    * Places a lock on a node.  
    * <p>
    * The lock prevents any other user or process from comitting updates 
    * to the node until the lock is released.  
    * <p>
    * The lock will be owned by the current user.
    * <p>
    * If the time to expire is 0 then the lock will never expire.  Otherwise the
    * timeToExpire indicates the number of seconds before the lock expires.  When
    * a lock expires the lock is considered to have been released.
    * <p>
    * If the node is already locked and the user is the lock owner then the lock will
    * be renewed with the passed timeToExpire.
    * 
    * @param  nodeRef         a reference to a node 
    * @param  lockType        the lock type
    * @param  timeToExpire    the number of seconds before the locks expires.
    * @param  lifetime        allows persistent or ephemeral locks to be specified.
    * @throws UnableToAquireLockException
    *                       thrown if the lock could not be obtained
    */
   @Auditable(parameters = {"nodeRef", "lockType", "timeToExpire", "lifetime"})
   public void lock(NodeRef nodeRef, LockType lockType, int timeToExpire, Lifetime lifetime)
               throws UnableToAquireLockException;
   
   /**
    * Places a lock on a node.  
    * <p>
    * The lock prevents any other user or process from comitting updates 
    * to the node until the lock is released.  
    * <p>
    * The lock will be owned by the current user.
    * <p>
    * If the time to expire is 0 then the lock will never expire.  Otherwise the
    * timeToExpire indicates the number of seconds before the lock expires.  When
    * a lock expires the lock is considered to have been released.
    * <p>
    * If the node is already locked and the user is the lock owner then the lock will
    * be renewed with the passed timeToExpire.
    * 
    * @param  nodeRef         a reference to a node 
    * @param  lockType        the lock type
    * @param  timeToExpire    the number of seconds before the locks expires.
    * @param  lifetime        allows persistent or ephemeral locks to be specified.
    * @param  additionalInfo  additional lock data stored alongside core lock data such as
    *                         timeToExpire. NOTE: only valid for ephemeral locks.
    * @throws UnableToAquireLockException
    *                       thrown if the lock could not be obtained
    */
   @Auditable(parameters = {"nodeRef", "lockType", "timeToExpire", "lifetime"})
   public void lock(NodeRef nodeRef, LockType lockType, int timeToExpire, Lifetime lifetime, String additionalInfo)
               throws UnableToAquireLockException;
   
   /**
    * Places a lock on a node and optionally on all its children.  
    * <p>
    * The lock prevents any other user or process from comitting updates 
    * to the node until the lock is released.  
    * <p>
    * The lock will be owned by the current user.
    * <p>  
    * If any one of the child locks can not be taken then an exception will 
    * be raised and all locks canceled.
    * <p>
    * If the time to expire is 0 then the lock will never expire.  Otherwise the
    * timeToExpire indicates the number of seconds before the lock expires.  When
    * a lock expires the lock is considered to have been released.
    * <p>
    * If the node is already locked and the user is the lock owner then the lock will
    * be renewed with the passed timeToExpire.
    * 
    * @param nodeRef            a reference to a node
    * @param lockType           the lock type 
    * @param timeToExpire       the number of seconds before the locks expires.
    * @param lockChildren       if true indicates that all the children (and 
    *                           grandchildren, etc) of the node will also be locked, 
    *                           false otherwise
    * 
    * @throws UnableToAquireLockException
    *                           thrown if the lock could not be obtained
    */
   @Auditable(parameters = {"nodeRef", "lockType", "timeToExpire", "lockChildren"})
   public void lock(NodeRef nodeRef, LockType lockType, int timeToExpire, boolean lockChildren)
       throws UnableToAquireLockException;
   
   /**
    * Places a lock on all the nodes referenced in the passed list.  
    * <p>
    * The lock prevents any other user or process from comitting updates 
    * to the node until the lock is released.  
    * <p>
    * The lock will be owned by the current user.
    * <p>
    * If the time to expire is 0 then the lock will never expire.  Otherwise the
    * timeToExpire indicates the number of seconds before the lock expires.  When
    * a lock expires the lock is considered to have been released.
    * <p>
    * If the node is already locked and the current user is the lock owner then the lock will
    * be renewed with the passed timeToExpire.
    * 
    * @param  nodeRefs          a list of node references
    * @param  lockType          the type of lock being created
    * @param  timeToExpire      the number of seconds before the locks expires.
    * @throws UnableToAquireLockException
    *                           thrown if the lock could not be obtained
    */
   @Auditable(parameters = {"nodeRefs", "lockType", "timeToExpire"})
   public void lock(Collection<NodeRef> nodeRefs, LockType lockType, int timeToExpire)
       throws UnableToAquireLockException;
   
   /**
    * Removes the lock on a node; if there is no lock then nothing is done.
    * <p>
    * The user must have sufficient permissions to remove the lock (ie: be the 
    * owner of the lock or have admin rights) and the node must not be checked
    * out. Otherwise an exception will be raised. 
    * 
    * @param  nodeRef  a reference to a node
    * @throws UnableToReleaseLockException
    *                  thrown if the lock could not be released             
    */
   @Auditable(parameters = {"nodeRef"}) 
   public void unlock(NodeRef nodeRef)
       throws UnableToReleaseLockException;
   
   /**
    * Removes the lock on a node and optional on its children.  
    * <p>
    * The user must have sufficient permissions to remove the lock(s) (ie: be 
    * the owner of the lock(s) or have admin rights) and the node(s) must not be
    * checked out. Otherwise an exception will be raised.
    * <p>
    * If one of the child nodes is not locked then it will be ignored and 
    * the process continue without error.  
    * <p>
    * If the lock on any one of the child nodes cannot be released then an 
    * exception will be raised.
    * 
    * @param  nodeRef        a node reference
    * @param  lockChildren   if true then all the children (and grandchildren, etc) 
    *                        of the node will also be unlocked, false otherwise
    * @throws UnableToReleaseLockException
    *                  thrown if the lock could not be released
    */
   @Auditable(parameters = {"nodeRef", "lockChildren"}) 
   public void unlock(NodeRef nodeRef, boolean lockChildren)
       throws UnableToReleaseLockException;
   
   /**
    * Removes the lock on a node and optional on its children.  
    * <p>
    * The user must have sufficient permissions to remove the lock(s) (ie: be 
    * the owner of the lock(s) or have admin rights) otherwise an exception 
    * will be raised.
    * <p>
    * If one of the child nodes is not locked then it will be ignored and 
    * the process continue without error.  
    * <p>
    * If the lock on any one of the child nodes cannot be released then an 
    * exception will be raised.
    * 
    * @param  nodeRef        a node reference
    * @param  lockChildren   if true then all the children (and grandchildren, etc) 
    *                        of the node will also be unlocked, false otherwise
    * @param allowCheckedOut is it permissable for a node to be a checked out node?
    * @throws UnableToReleaseLockException
    *                  thrown if the lock could not be released
    */
   @Auditable(parameters = {"nodeRef", "lockChildren", "allowCheckedOut"}) 
   public void unlock(NodeRef nodeRef, boolean lockChildren, boolean allowCheckedOut)
       throws UnableToReleaseLockException;
   /**
    * Removes a lock on the nodes provided.
    * <p>
    * The user must have sufficient permissions to remove the locks (ie: be 
    * the owner of the locks or have admin rights) otherwise an exception 
    * will be raised.
    * <p>
    * If one of the nodes is not locked then it will be ignored and the
    * process will continue without an error.
    * <p>
    * If the lock on any one of the nodes cannot be released than an exception 
    * will be raised and the process rolled back.
    * 
    * @param  nodeRefs  the node references
    * @param  userName   the user reference
    * @throws UnableToReleaseLockException
    *                  thrown if the lock could not be released
    */
   @Auditable(parameters = {"nodeRefs"}) 
   public void unlock(Collection<NodeRef> nodeRefs)
       throws UnableToReleaseLockException;
   
   /**
    * Gets the lock status for the node reference relative to the current user.
    * 
    * @see LockService#getLockStatus(NodeRef, NodeRef)
    * 
    * @param nodeRef    the node reference
    * @return           the lock status
    */
   @Auditable(parameters = {"nodeRef"})
   public LockStatus getLockStatus(NodeRef nodeRef);
   
   
   /**
    * Gets the lock status for the node reference for the specified user.
    * 
    * @see LockService#getLockStatus(NodeRef, NodeRef)
    * 
    * @param nodeRef    the node reference
    * @param userName   the user name
    * @return           the lock status
    */
   @Auditable(parameters = {"nodeRef", "userName"})
   public LockStatus getLockStatus(NodeRef nodeRef, String userName);
   
   /**
    * Gets the lock type for the node indicated.  
    * <p>
    * Returns null if the node is not locked.
    * <p>
    * Throws an exception if the node does not have the lock aspect.
    * 
    * @param  nodeRef  the node reference
    * @return          the lock type, null is returned if the object in question has no
    *                  lock
    */
   @Auditable(parameters = {"nodeRef"})
   public LockType getLockType(NodeRef nodeRef);
   
   /**
    * Checks to see if the current user has access to the specified node.
    * <p>
    * If the node is locked by another user then a NodeLockedException is thrown. 
    * <p>
    * Gets the user reference from the current session.
    * 
    * @param nodeRef    the node reference
    * 
    * @throws NodeLockedException
    *                   thrown if the node is locked by someone else.  This is based on the lock status of the lock,
    *                   the user ref and the lock type.
    */
   @Auditable(parameters = {"nodeRef"})
   public void checkForLock(NodeRef nodeRef);
   
   /**
    * Allow the current transaction to update a node despite any locks that may be on it.
    * <p>
    * Used for the system to be able to update locked nodes. 
    */
   public void suspendLocks();
   
   /**
    * After calling suspendLocks turn the locks back on.
    */
   public void enableLocks();
   
   /**
    * Retrieve the additional lock info associated with a node ref.
    * <p>
    * @param nodeRef
    * @return Additional info string or null
    */
   public String getAdditionalInfo(NodeRef nodeRef);
   
   /**
    * Retrieve the lock properties for the given NodeRef.
    * <p>
    * Do NOT use the returned information to determine the actual state of a lock,
    * use {@link #getLockStatus(NodeRef)} and other LockService methods for this purpose.
    * <p>
    * The returned data is intended for information purposes only, e.g. returning the timeout
    * in a WebDAV response.
    * 
    * @param nodeRef
    * @return LockState
    */
   public LockState getLockState(NodeRef nodeRef);
}
