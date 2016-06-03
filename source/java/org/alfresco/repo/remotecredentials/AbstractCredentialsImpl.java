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
