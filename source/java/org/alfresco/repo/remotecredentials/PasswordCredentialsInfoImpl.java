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
