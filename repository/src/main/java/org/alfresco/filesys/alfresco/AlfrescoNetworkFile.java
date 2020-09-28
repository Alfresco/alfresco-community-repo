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
package org.alfresco.filesys.alfresco;

import org.alfresco.jlan.server.filesys.NetworkFile;
import org.alfresco.jlan.server.filesys.cache.FileState;
import org.alfresco.jlan.server.filesys.cache.NetworkFileStateInterface;


/**
 * Alfresco Network File Class
 * 
 * <p>Adds Alfresco extensions to the network file.
 */
public abstract class AlfrescoNetworkFile extends NetworkFile implements NetworkFileStateInterface {

  // Associated file state
  
  private FileState m_state;
  
  /**
   * Create a network file object with the specified file/directory name.
   * 
   * @param name File name string.
   */
  public AlfrescoNetworkFile(String name)
  {
      super( name);
  }

  /**
   * Return the associated file state
   * 
   * @return FileState
   */
  public FileState getFileState()
  {
    return m_state;
  }
  
  /**
   * Set the associated file state
   * 
   * @param state FileState
   */
  public final void setFileState( FileState state)
  {
    m_state = state;
  }
  
  /**
   * Tell JLAN it needs to call disk.closeFile rather than short cutting.
   * @return boolean
   */
  public boolean allowsOpenCloseViaNetworkFile() {
      return false;
  }
}
