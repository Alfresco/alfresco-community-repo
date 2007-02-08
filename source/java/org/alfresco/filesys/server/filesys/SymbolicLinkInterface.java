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

import org.alfresco.filesys.server.SrvSession;

/**
 * Symbolic Link Interface
 * 
 * <p>Optional interface that a filesystem driver can implement to indicate that symbolic links are supported.
 */
public interface SymbolicLinkInterface {

  /**
   * Read the link data for a symbolic link
   * 
   * @param sess SrvSession
   * @param tree TreeConnection
   * @param path String
   * @return String
   * @exception AccessDeniedException 
   */
  public String readSymbolicLink( SrvSession sess, TreeConnection tree, String path)
    throws AccessDeniedException;
}
