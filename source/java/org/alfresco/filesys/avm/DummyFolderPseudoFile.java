/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.filesys.avm;

import org.alfresco.filesys.server.filesys.FileAttribute;
import org.alfresco.filesys.server.filesys.FileInfo;
import org.alfresco.filesys.server.filesys.FileName;
import org.alfresco.filesys.server.filesys.NetworkFile;
import org.alfresco.filesys.server.pseudo.PseudoFile;
import org.alfresco.filesys.server.pseudo.PseudoFolderNetworkFile;

/**
 * Dummy Folder Pseudo File Class
 * 
 * <p>Represents a dummy folder within the virtualization filesystem view.
 *
 * @author gkspencer
 */
public class DummyFolderPseudoFile extends PseudoFile {

	/**
	 * Class constructor
	 * 
	 * @param fname String
	 */
	public DummyFolderPseudoFile( String fname)
	{
		super( fname, FileAttribute.Directory + FileAttribute.ReadOnly);
		
		// Create static file information from the folder details
		
		FileInfo fInfo = new FileInfo( fname, 0L, FileAttribute.Directory + FileAttribute.ReadOnly);
		fInfo.setCreationDateTime( System.currentTimeMillis());
		
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
