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

import org.alfresco.service.cmr.remotecredentials.BaseCredentialsInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * This class is the parent of a set of Remote Credentials
 * 
 * @author Nick Burch
 * @since Odin
 */
public abstract class AbstractCredentialsImpl implements BaseCredentialsInfo 
{
   private static final long serialVersionUID = 1825070334051269906L;
   
   private QName   type;
   private NodeRef nodeRef;
   private String  remoteSystemName;
   private NodeRef remoteSystemContainerNodeRef;
   
   private String remoteUsername;
   private boolean lastAuthenticationSucceeded;
   
   /**
    * Creates a new, empty {@link AbstractCredentialsImpl} ready
    *  to be stored later
    */
   public AbstractCredentialsImpl(QName type)
   {
       this.type = type;
       
       // Default is that the authentication worked, unless told otherwise
       this.lastAuthenticationSucceeded = true;
   }
   
   public AbstractCredentialsImpl(NodeRef nodeRef, QName type, String remoteSystemName, NodeRef remoteSystemContainerNodeRef)
   {
       this(type);
       
       // Record the node details
       this.nodeRef = nodeRef;
       this.remoteSystemName = remoteSystemName;
       this.remoteSystemContainerNodeRef = remoteSystemContainerNodeRef;
   }
    
   /**
    * @return the NodeRef of the underlying credentials
    */
   public NodeRef getNodeRef()
   {
       return nodeRef;
   }
   
   /**
    * @return the Type of the underlying credentials
    */
   public QName getCredentialsType()
   {
       return type;
   }
   
   /**
    * @return the Remote System Name the credentials belong to
    */
   public String getRemoteSystemName()
   {
       return remoteSystemName;
   }
   
   /**
    * @return the NodeRef of the container for the Remote System
    */
   public NodeRef getRemoteSystemContainerNodeRef()
   {
       return remoteSystemContainerNodeRef;
   }
   
   
   /**
    * @return the Remote Username
    */
   public String getRemoteUsername()
   {
       return remoteUsername;
   }
   public void setRemoteUsername(String username)
   {
       this.remoteUsername = username;
   }
   
   /**
    * @return whether the last authentication attempt succeeded
    */
   public boolean getLastAuthenticationSucceeded()
   {
       return lastAuthenticationSucceeded;
   }
   public void setLastAuthenticationSucceeded(boolean succeeded)
   {
       this.lastAuthenticationSucceeded = succeeded;
   }
}
