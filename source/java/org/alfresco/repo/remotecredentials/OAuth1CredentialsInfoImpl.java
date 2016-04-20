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
