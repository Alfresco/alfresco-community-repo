/*
 * Copyright (C) 2006-2010 Alfresco Software Limited.
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

package org.alfresco.filesys.state;

/**
 * File State Listener Interface
 *
 * @author gkspencer
 */
public interface FileStateListener {

	/**
	 * File state has expired. The listener can control whether the file state is removed
	 * from the cache, or not.
	 * 
	 * @param state FileState
	 * @return true to remove the file state from the cache, or false to leave the file state in the cache
	 */
	public boolean fileStateExpired(FileState state);
	
	/**
	 * File state cache is closing down, any resources attached to the file state must be released.
	 * 
	 * @param state FileState
	 */
	public void fileStateClosed(FileState state);
}
