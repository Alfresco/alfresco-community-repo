package org.alfresco.service.cmr.remotecredentials;


/**
 * This class represents a password based set of credentials
 * 
 * @author Nick Burch
 * @since 4.0.2
 */
public interface PasswordCredentialsInfo extends BaseCredentialsInfo 
{
   /**
    * @return the Remote Password
    */
   String getRemotePassword();
}
