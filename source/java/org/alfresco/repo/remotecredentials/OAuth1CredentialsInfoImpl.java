package org.alfresco.repo.remotecredentials;

import org.alfresco.service.cmr.remotecredentials.OAuth1CredentialsInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * This class represents an OAuth 1.0 based set of credentials
 * 
 * @author Nick Burch
 * @since Odin
 */
public class OAuth1CredentialsInfoImpl extends AbstractCredentialsImpl implements OAuth1CredentialsInfo 
{
   private static final long serialVersionUID = 4739556616590284462L;
   private static final QName TYPE = RemoteCredentialsModel.TYPE_OAUTH1_CREDENTIALS;
   
   private String oauthToken;
   private String oauthTokenSecret;
   
   public OAuth1CredentialsInfoImpl()
   {
       super(TYPE);
   }

   public OAuth1CredentialsInfoImpl(NodeRef nodeRef, String remoteSystemName, NodeRef remoteSystemContainerNodeRef)
   {
       super(nodeRef, TYPE, remoteSystemName, remoteSystemContainerNodeRef);
   }

   /**
    * @return the OAuth Token Identifier
    */
   public String getOAuthToken()
   {
       return oauthToken;
   }
   public void setOAuthToken(String token)
   {
       this.oauthToken = token;
   }
   
   /**
    * @return the OAuth Token Secret
    */
   public String getOAuthSecret()
   {
       return oauthTokenSecret;
   }
   public void setOAuthSecret(String secret)
   {
       this.oauthTokenSecret = secret;
   }
}
