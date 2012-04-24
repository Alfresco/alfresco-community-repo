/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.remotecredentials;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.node.encryption.MetadataEncryptor;
import org.alfresco.service.cmr.remotecredentials.BaseCredentialsInfo;
import org.alfresco.service.cmr.remotecredentials.OAuth2CredentialsInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * The Factory for building {@link OAuth2CredentialsInfo} objects
 * 
 * @author Nick Burch
 * @since Odin
 */
public class OAuth2CredentialsFactory implements RemoteCredentialsInfoFactory
{
    private MetadataEncryptor metadataEncryptor;
    
    public void setMetadataEncryptor(MetadataEncryptor metadataEncryptor)
    {
        this.metadataEncryptor = metadataEncryptor;
    }
    
    /**
     * Creates a new {@link OAuth2CredentialsInfo} based on the details of the underlying node.
     */
    public OAuth2CredentialsInfo createCredentials(QName type, NodeRef nodeRef, String remoteSystemName, 
            NodeRef remoteSystemContainerNodeRef, Map<QName,Serializable> properties)
    {
        // Decrypt the token details
        String accessToken = (String)metadataEncryptor.decrypt(
                RemoteCredentialsModel.PROP_OAUTH2_ACCESS_TOKEN, properties.get(RemoteCredentialsModel.PROP_OAUTH2_ACCESS_TOKEN));
        String refreshToken = (String)metadataEncryptor.decrypt(
                RemoteCredentialsModel.PROP_OAUTH2_REFRESH_TOKEN, properties.get(RemoteCredentialsModel.PROP_OAUTH2_REFRESH_TOKEN));
        
        // Get the dates
        Date tokenIssuedAt = (Date)properties.get(RemoteCredentialsModel.PROP_OAUTH2_TOKEN_ISSUED_AT);
        Date tokenExpiresAt = (Date)properties.get(RemoteCredentialsModel.PROP_OAUTH2_TOKEN_EXPIRES_AT);
        
        // Build the object
        OAuth2CredentialsInfoImpl credentials = 
            new OAuth2CredentialsInfoImpl(nodeRef, remoteSystemName, remoteSystemContainerNodeRef);
        
        // Populate
        RemoteCredentialsInfoFactory.FactoryHelper.setCoreCredentials(credentials, properties);
        credentials.setOauthAccessToken(accessToken);
        credentials.setOauthRefreshToken(refreshToken);
        credentials.setOauthTokenIssuedAt(tokenIssuedAt);
        credentials.setOauthTokenExpiresAt(tokenExpiresAt);
        
        // All done
        return credentials;
    }

    /**
     * Serializes the given {@link BaseCredentialsInfo} object to node properties.
     * 
     * @param info The Credentials object to serialize
     * @param coreProperties The core rc:credentialBase properties for the node
     * @return The final set of properties to be serialized for the node
     */
    public Map<QName,Serializable> serializeCredentials(BaseCredentialsInfo info)
    {
        if (! (info instanceof OAuth2CredentialsInfo))
        {
            throw new IllegalStateException("Incorrect registration, info must be a OAuth2CredentialsInfo");
        }
        
        // Encrypt the token details
        OAuth2CredentialsInfo credentials = (OAuth2CredentialsInfo)info;
        
        Serializable accessTokenEncrypted = metadataEncryptor.encrypt(
                RemoteCredentialsModel.PROP_OAUTH2_ACCESS_TOKEN, credentials.getOAuthAccessToken());
        Serializable refreshTokenEncrypted = metadataEncryptor.encrypt(
                RemoteCredentialsModel.PROP_OAUTH2_REFRESH_TOKEN, credentials.getOAuthRefreshToken());

        // Store our specific types and return
        Map<QName,Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(RemoteCredentialsModel.PROP_OAUTH2_ACCESS_TOKEN, accessTokenEncrypted);
        properties.put(RemoteCredentialsModel.PROP_OAUTH2_REFRESH_TOKEN, refreshTokenEncrypted);
        properties.put(RemoteCredentialsModel.PROP_OAUTH2_TOKEN_ISSUED_AT, credentials.getOAuthTicketIssuedAt());
        properties.put(RemoteCredentialsModel.PROP_OAUTH2_TOKEN_EXPIRES_AT, credentials.getOAuthTicketExpiresAt());
        return properties;
    }
}
