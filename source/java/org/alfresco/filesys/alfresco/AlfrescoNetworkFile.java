package org.alfresco.filesys.alfresco;

import org.alfresco.jlan.server.filesys.NetworkFile;
import org.alfresco.filesys.state.FileState;

/*
 * AlfrescoNetworkFile.java
 *
 * Copyright (c) 2007 Starlasoft. All rights reserved.
 */

/**
 * Alfresco Network File Class
 * 
 * <p>Adds Alfresco extensions to the network file.
 */
public abstract class AlfrescoNetworkFile extends NetworkFile {

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
  public final FileState getFileState()
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
}
