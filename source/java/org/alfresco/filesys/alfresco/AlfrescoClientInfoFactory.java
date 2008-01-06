package org.alfresco.filesys.alfresco;

import org.alfresco.jlan.server.auth.ClientInfo;
import org.alfresco.jlan.server.auth.ClientInfoFactory;

/*
 * AlfrescoClientInfoFactory.java
 *
 * Copyright (c) 2007 Starlasoft. All rights reserved.
 */

/**
 * Alfresco Client Info Factory Class
 */
public class AlfrescoClientInfoFactory implements ClientInfoFactory {

  /**
   * Class constructor
   */
  public AlfrescoClientInfoFactory() {
    
    //  Plug the client info factory in
    
    ClientInfo.setFactory( this);
  }
  
  /**
   * Create the extended client information object
   * 
   * @param user String
   * @param password byte[]
   * @return ClientInfo
   */
  public ClientInfo createInfo(String user, byte[] password) {

    // Return an Alfresco extended client information object
    
    return new AlfrescoClientInfo( user, password);
  }
}
