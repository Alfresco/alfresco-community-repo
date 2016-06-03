package org.alfresco.service.cmr.remotecredentials;

import java.io.Serializable;

import org.alfresco.repo.security.permissions.PermissionCheckValue;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * This class is the parent of a set of Remote Credentials
 * 
 * @author Nick Burch
 * @since 4.0.2
 */
public interface BaseCredentialsInfo extends Serializable, PermissionCheckValue 
{
   /**
    * @return the NodeRef of the underlying credentials
    */
   NodeRef getNodeRef();
   
   /**
    * @return the Type of the underlying credentials
    */
   QName getCredentialsType();
   
   /**
    * @return the Remote System Name the credentials belong to
    */
   String getRemoteSystemName();
   
   /**
    * @return the NodeRef of the container for the Remote System
    */
   NodeRef getRemoteSystemContainerNodeRef();
   
   /**
    * @return the Remote Username
    */
   String getRemoteUsername();
   
   /**
    * @return whether the last authentication attempt succeeded
    */
   boolean getLastAuthenticationSucceeded();
}
