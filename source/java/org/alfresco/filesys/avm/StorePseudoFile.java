/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * http://www.alfresco.com/legal/licensing" */

package org.alfresco.filesys.avm;

import org.alfresco.filesys.server.filesys.FileAttribute;
import org.alfresco.filesys.server.filesys.FileInfo;
import org.alfresco.filesys.server.filesys.FileName;
import org.alfresco.filesys.server.filesys.NetworkFile;
import org.alfresco.filesys.server.pseudo.PseudoFile;
import org.alfresco.filesys.server.pseudo.PseudoFolderNetworkFile;
import org.alfresco.service.cmr.avm.AVMStoreDescriptor;

/**
 * Store Pseudo File Class
 * 
 * <p>Represents an AVM store as a folder.
 *
 * @author gkspencer
 */
public class StorePseudoFile extends PseudoFile {

	/**
	 * Class constructor
	 * 
	 * @param storeDesc AVMStoreDescriptor
	 * @param relPath String
	 */
	public StorePseudoFile( AVMStoreDescriptor storeDesc, String relPath)
	{
		super( storeDesc.getName(), FileAttribute.Directory + FileAttribute.ReadOnly);
		
		// Create static file information from the store details
		
		FileInfo fInfo = new FileInfo( storeDesc.getName(), 0L, FileAttribute.Directory + FileAttribute.ReadOnly);

		fInfo.setCreationDateTime( storeDesc.getCreateDate());
		fInfo.setModifyDateTime( storeDesc.getCreateDate());
		fInfo.setAccessDateTime( storeDesc.getCreateDate());
		fInfo.setChangeDateTime( storeDesc.getCreateDate());
		
		fInfo.setPath( relPath);
		fInfo.setFileId( relPath.hashCode());
		
		setFileInfo( fInfo);
	}
	
	/**
	 * Class constructor
	 * 
	 * @param storeName String
	 * @param relPath String
	 */
	public StorePseudoFile( String storeName, String relPath)
	{
		super( storeName, FileAttribute.Directory + FileAttribute.ReadOnly);
		
		// Create static file information from the store details
		
		FileInfo fInfo = new FileInfo( storeName, 0L, FileAttribute.Directory + FileAttribute.ReadOnly);

		long timeNow = System.currentTimeMillis();
		fInfo.setCreationDateTime( timeNow);
		fInfo.setModifyDateTime( timeNow);
		fInfo.setAccessDateTime( timeNow);
		fInfo.setChangeDateTime( timeNow);
		
		fInfo.setPath( relPath);
		fInfo.setFileId( relPath.hashCode());
		
		setFileInfo( fInfo);
	}
	
    /**
     * Return a network file for reading/writing the pseudo file
     * 
     * @param netPath String
     * @return NetworkFile
     */
	@Override
	public NetworkFile getFile(String netPath) {
		
		// Split the path to get the name
		
		String[] paths = FileName.splitPath( netPath);
		
		// Create a network file for the folder
		
		return new PseudoFolderNetworkFile( paths[1], netPath);
	}

    /**
     * Return the file information for the pseudo file
     *
     * @return FileInfo
     */
	@Override
	public FileInfo getFileInfo() {
		return getInfo();
	}
}
