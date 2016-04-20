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
