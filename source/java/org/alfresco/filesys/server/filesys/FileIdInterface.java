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
package org.alfresco.filesys.server.filesys;

import java.io.FileNotFoundException;

import org.alfresco.filesys.server.SrvSession;

/**
 * File Id Interface
 * <p>
 * Optional interface that a DiskInterface driver can implement to provide file id to path
 * conversion.
 */
public interface FileIdInterface
{

    /**
     * Convert a file id to a share relative path
     * 
     * @param sess SrvSession
     * @param tree TreeConnection
     * @param dirid int
     * @param fileid
     * @return String
     * @exception FileNotFoundException
     */
    public String buildPathForFileId(SrvSession sess, TreeConnection tree, int dirid, int fileid)
            throws FileNotFoundException;
}
