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
