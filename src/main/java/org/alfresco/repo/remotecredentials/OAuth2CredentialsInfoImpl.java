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
