/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.filesys.smb.server.ntfs;

import java.io.IOException;

import org.alfresco.filesys.server.SrvSession;
import org.alfresco.filesys.server.filesys.TreeConnection;

/**
 * NTFS Streams Interface
 * <p>
 * Optional interface that a DiskInterface driver can implement to provide file streams support.
 */
public interface NTFSStreamsInterface
{

    /**
     * Determine if NTFS streams are enabled
     * 
     * @param sess SrvSession
     * @param tree TreeConnection
     * @return boolean
     */
    public boolean hasStreamsEnabled(SrvSession sess, TreeConnection tree);

    /**
     * Return stream information for the specified stream
     * 
     * @param sess SrvSession
     * @param tree TreeConnection
     * @param streamInfo StreamInfo
     * @return StreamInfo
     * @exception IOException I/O error occurred
     */
    public StreamInfo getStreamInformation(SrvSession sess, TreeConnection tree, StreamInfo streamInfo)
            throws IOException;

    /**
     * Return a list of the streams for the specified file
     * 
     * @param sess SrvSession
     * @param tree TreeConnection
     * @param fileName String
     * @return StreamInfoList
     * @exception IOException I/O error occurred
     */
    public StreamInfoList getStreamList(SrvSession sess, TreeConnection tree, String fileName) throws IOException;

    /**
     * Rename a stream
     * 
     * @param sess SrvSession
     * @param tree TreeConnection
     * @param oldName String
     * @param newName String
     * @param overWrite boolean
     * @exception IOException
     */
    public void renameStream(SrvSession sess, TreeConnection tree, String oldName, String newName, boolean overWrite)
            throws IOException;
}
