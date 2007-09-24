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
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.service.cmr.remote;

import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;

/**
 * Remote transport interface for the Loader application.  This adds functionality
 * that is generally required by the application and that is not available on other
 * interfaces.
 * 
 * @see FileFolderRemote
 * 
 * @author Derek Hulley
 * @since 2.2.
 */
public interface LoaderRemote
{
    /** The service name <b>org.alfresco.LoaderRemote</b> */
    public static final String SERVICE_NAME = "org.alfresco.LoaderRemote";
    
    /**
     * Authenticate on the server using the given username and password
     * 
     * @return              the authentication ticket
     */
    public String authenticate(String username, String password);
    
    /**
     * Get the working <b>cm:folder</b> node for the given store.  If there is no working
     * root node, then one is created.
     * 
     * @param ticket        the authentication ticket
     * @param storeRef      the store reference
     * @return              a working <b>cm:folder</b> to use as the root for loading,
     *                      or <tt>null</tt> if it is not available.
     */
    public NodeRef getOrCreateWorkingRoot(String ticket, StoreRef storeRef);
    
    /**
     * @param ticket        the authentication ticket
     * @return              Returns the total number of ADM nodes
     */
    public int getNodeCount(String ticket);
    
    /**
     * @param ticket        the authentication ticket
     * @param storeRef      the store to query against
     * @return              Returns the total number of nodes for the given ADM store
     */
    public int getNodeCount(String ticket, StoreRef storeRef);
    
    /**
     * Upload multiple files to a folder.
     * 
     * @param ticket        the authentication ticket
     * @param folderNodeRef the folder to upload to
     * @param filenames     the names of the files to upload
     * @param bytes         the contents of the files
     * @return              Returns the details of each file created
     */
    public FileInfo[] uploadContent(String ticket, NodeRef folderNodeRef, String[] filenames, byte[][] bytes);
}
