package org.alfresco.service.cmr.remotecredentials;

/**
 * This class represents an OAuth 1.0 based set of credentials
 * 
 * @author Nick Burch
 * @since 4.0.2
 */
public interface OAuth1CredentialsInfo extends BaseCredentialsInfo 
{
   /**
    * @return the OAuth Token Identifier
    */
   String getOAuthToken();
   
   /**
    * @return the OAuth Token Secret
    */
   String getOAuthSecret();
}
