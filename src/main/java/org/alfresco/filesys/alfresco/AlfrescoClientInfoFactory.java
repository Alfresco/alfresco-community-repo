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

import org.alfresco.jlan.server.auth.ClientInfo;
import org.alfresco.jlan.server.auth.ClientInfoFactory;


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
