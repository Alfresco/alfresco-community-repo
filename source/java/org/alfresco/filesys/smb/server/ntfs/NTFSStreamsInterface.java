/*
 * Copyright (C) 2005 Alfresco, Inc.
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
