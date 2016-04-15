package org.alfresco.filesys.repo;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * The lock keeper tracks multiple locks on files open via the file system protocols.   
 *
 * There can be multiple locks on the same file, the lock keeper keeps track of the individual locks and delegates to the LockService 
 * 
 * @author mrogers
 */
public interface LockKeeper 
{
	/**
	 * Transactional method to make a lock on the specified node ref.
	 * 
	 * @param nodeRef
	 */
	public void addLock(NodeRef nodeRef);
	
	/**
	 * Transactional method to remove a lock on the specified node ref.
	 * @param nodeRef
	 */
	public void removeLock(NodeRef nodeRef);
	
	/**
	 * 
	 */
	public void refreshAllLocks();

}
