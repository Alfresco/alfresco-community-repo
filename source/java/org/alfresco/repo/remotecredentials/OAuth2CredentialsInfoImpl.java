package org.alfresco.repo.remotecredentials;

import java.util.Date;

import org.alfresco.service.cmr.remotecredentials.OAuth2CredentialsInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * This class represents an OAuth 2.0 based set of credentials
 * 
 * @author Nick Burch
 * @since Odin
 */
public class OAuth2CredentialsInfoImpl extends AbstractCredentialsImpl implements OAuth2CredentialsInfo 
{
   private static final long serialVersionUID = 4739556616590284462L;
   private static final QName TYPE = RemoteCredentialsModel.TYPE_OAUTH2_CREDENTIALS;
   
   private String oauthAccessToken;
   private String oauthRefreshToken;
   private Date oauthTokenExpiresAt;
   private Date oauthTokenIssuedAt;
   
   public OAuth2CredentialsInfoImpl()
   {
       super(TYPE);
   }

   public OAuth2CredentialsInfoImpl(NodeRef nodeRef, String remoteSystemName, NodeRef remoteSystemContainerNodeRef)
   {
       super(nodeRef, TYPE, remoteSystemName, remoteSystemContainerNodeRef);
   }
   
   /**
    * @return the OAuth Access Token
    */
   public String getOAuthAccessToken()
   {
       return oauthAccessToken;
   }
   public void setOauthAccessToken(String oauthAccessToken)
   {
       this.oauthAccessToken = oauthAccessToken;
   }
   
   /**
    * @return the OAuth Refresh
    */
   public String getOAuthRefreshToken()
   {
       return oauthRefreshToken;
   }
   public void setOauthRefreshToken(String oauthRefreshToken)
   {
       this.oauthRefreshToken = oauthRefreshToken;
   }
   
   /**
    * @return When the Access Token was Issued
    */
   public Date getOAuthTicketIssuedAt()
   {
       return oauthTokenIssuedAt;
   }
   public void setOauthTokenIssuedAt(Date oauthTokenIssuedAt)
   {
       this.oauthTokenIssuedAt = oauthTokenIssuedAt;
   }
   
   /**
    * @return When the Access Token will Expire
    */
   public Date getOAuthTicketExpiresAt()
   {
       return oauthTokenExpiresAt;
   }
   public void setOauthTokenExpiresAt(Date oauthTokenExpiresAt)
   {
       this.oauthTokenExpiresAt = oauthTokenExpiresAt;
   }   
}
