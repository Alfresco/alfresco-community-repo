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
package org.alfresco.repo.remotecredentials;

import org.alfresco.service.cmr.remotecredentials.PasswordCredentialsInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * This class represents a password based set of credentials
 * 
 * @author Nick Burch
 * @since Odin
 */
public class PasswordCredentialsInfoImpl extends AbstractCredentialsImpl implements PasswordCredentialsInfo
{
   private static final long serialVersionUID = -5351115540931076949L;
   private static final QName TYPE = RemoteCredentialsModel.TYPE_PASSWORD_CREDENTIALS;
   
   private String remotePassword;
    
   public PasswordCredentialsInfoImpl()
   {
       super(TYPE);
   }
   public PasswordCredentialsInfoImpl(NodeRef nodeRef, String remoteSystemName, NodeRef remoteSystemContainerNodeRef)
   {
       super(nodeRef, TYPE, remoteSystemName, remoteSystemContainerNodeRef);
   }

   /**
    * @return the Remote Password
    */
   public String getRemotePassword()
   {
       return remotePassword;
   }
   public void setRemotePassword(String password)
   {
       remotePassword = password;
   }
   
   public String toString()
   {
       return "Username & Password Credentials, Username="+getRemoteUsername()+" for System="+
               getRemoteSystemName() + " stored @ " + getNodeRef();
   }
}
