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

package org.alfresco.filesys.smb.server.repo.pseudo;

import org.alfresco.filesys.server.SrvSession;
import org.alfresco.filesys.server.filesys.TreeConnection;

/**
 * Pseudo File Interface
 * 
 * <p>Provides the ability to add files into the file listing of a folder.
 * 
 * @author gkspencer
 */
public interface PseudoFileInterface
{
    /**
     * Check if the specified path refers to a pseudo file
     * 
     * @param sess SrvSession
     * @param tree TreeConnection
     * @param path String
     * @return boolean
     */
    public boolean isPseudoFile(SrvSession sess, TreeConnection tree, String path);

    /**
     * Return the pseudo file for the specified path, or null if the path is not a pseudo file
     * 
     * @param sess SrvSession
     * @param tree TreeConnection
     * @param path String
     * @return PseudoFile
     */
    public PseudoFile getPseudoFile(SrvSession sess, TreeConnection tree, String path);
    
    /**
     * Add pseudo files to a folder so that they appear in a folder search
     * 
     * @param sess SrvSession
     * @param tree TreeConnection
     * @param path String
     * @return int
     */
    public int addPseudoFilesToFolder(SrvSession sess, TreeConnection tree, String path);
    
    /**
     * Delete a pseudo file
     * 
     * @param sess SrvSession
     * @param tree TreeConnection
     * @param path String
     */
    public void deletePseudoFile(SrvSession sess, TreeConnection tree, String path);
}
