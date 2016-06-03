package org.alfresco.service.cmr.remotecredentials;

import java.util.Date;

/**
 * This class represents an OAuth 2.0 based set of credentials
 * 
 * @author Nick Burch
 * @since 4.0.2
 */
public interface OAuth2CredentialsInfo extends BaseCredentialsInfo 
{
   /**
    * @return the OAuth Access Token
    */
   String getOAuthAccessToken();
   
   /**
    * @return the OAuth Refresh
    */
   String getOAuthRefreshToken();
   
   /**
    * @return When the Access Token was Issued
    */
   Date getOAuthTicketIssuedAt();
   
   /**
    * @return When the Access Token will Expire
    */
   Date getOAuthTicketExpiresAt();
}
