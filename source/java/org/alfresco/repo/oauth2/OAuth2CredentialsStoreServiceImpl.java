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
package org.alfresco.repo.oauth2;

import java.util.Date;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.query.CannedQueryPageDetails;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.remotecredentials.OAuth2CredentialsInfoImpl;
import org.alfresco.repo.remotecredentials.RemoteCredentialsModel;
import org.alfresco.service.cmr.oauth2.OAuth2CredentialsStoreService;
import org.alfresco.service.cmr.remotecredentials.OAuth2CredentialsInfo;
import org.alfresco.service.cmr.remotecredentials.RemoteCredentialsService;
import org.alfresco.service.cmr.remoteticket.NoSuchSystemException;

/**
 * @author Jared Ottley
 */
public class OAuth2CredentialsStoreServiceImpl implements OAuth2CredentialsStoreService
{
    private RemoteCredentialsService remoteCredentialsService;

    public void setRemoteCredentialsService(RemoteCredentialsService remoteCredentialsService)
    {
        this.remoteCredentialsService = remoteCredentialsService;
    }

    /**
     * Add or Update OAuth2 Credentials for the current user to the OAuth2
     * Credential Store
     * 
     * @param remoteSystemId
     * @param accessToken
     * @param refresh Token
     * @param expiresAt
     * @param issuedAt if null, the current Datetime will be used
     * @return OAuth2CredentialsInfo
     */
    @Override
    public OAuth2CredentialsInfo storePersonalOAuth2Credentials(String remoteSystemId,
                String accessToken, String refreshToken, Date expiresAt, Date issuedAt)
                throws NoSuchSystemException
    {

        OAuth2CredentialsInfo credentials = buildPersonalOAuth2CredentialsInfo(remoteSystemId,
                    accessToken, refreshToken, expiresAt, issuedAt);

        if (credentials.getNodeRef() != null)
        {
            return (OAuth2CredentialsInfo) remoteCredentialsService.updateCredentials(credentials);
        }
        else
        {
            return (OAuth2CredentialsInfo) remoteCredentialsService.createPersonCredentials(
                        remoteSystemId, credentials);
        }

    }

    /**
     * Add Shared OAuth2 Credentials to the OAuth2 Credential Store
     * 
     * @param remoteSystemId
     * @param accessToken
     * @param refreshToken
     * @param expiresAt
     * @param issuedAt
     * @return OAuth2CredentialsInfo
     */
    @Override
    public OAuth2CredentialsInfo storeSharedOAuth2Credentials(String remoteSystemId,
                String accessToken, String refreshToken, Date expiresAt, Date issuedAt)
                throws NoSuchSystemException
    {
        OAuth2CredentialsInfo credentials = buildSharedOAuth2CredentialsInfo(remoteSystemId,
                    accessToken, refreshToken, expiresAt, issuedAt);

        return (OAuth2CredentialsInfo) remoteCredentialsService.createSharedCredentials(
                    remoteSystemId, credentials);
    }

    /**
     * @param exisitingCredentials
     * @param remoteSystemId
     * @param accessToken
     * @param refreshToken
     * @param expiresAt
     * @param issuedAt
     * @return OAuth2CredentialsInfo
     */
    @Override
    public OAuth2CredentialsInfo updateSharedOAuth2Credentials(
                OAuth2CredentialsInfo exisitingCredentials, String remoteSystemId,
                String accessToken, String refreshToken, Date expiresAt, Date issuedAt)
                throws NoSuchSystemException
    {
        List<OAuth2CredentialsInfo> shared = listSharedOAuth2Credentials(remoteSystemId);

        for (OAuth2CredentialsInfo credential : shared)
        {
            if (credential.getNodeRef().equals(exisitingCredentials.getNodeRef()))
            {
                OAuth2CredentialsInfoImpl credentials = new OAuth2CredentialsInfoImpl(
                            exisitingCredentials.getNodeRef(),
                            exisitingCredentials.getRemoteSystemName(),
                            exisitingCredentials.getRemoteSystemContainerNodeRef());

                credentials.setOauthAccessToken(accessToken);
                credentials.setOauthRefreshToken(refreshToken);
                credentials.setOauthTokenExpiresAt(expiresAt);
                if (issuedAt != null)
                {
                    credentials.setOauthTokenIssuedAt(issuedAt);
                }
                else
                {
                    credentials.setOauthTokenIssuedAt(new Date());
                }

                return (OAuth2CredentialsInfo) remoteCredentialsService
                            .updateCredentials(credentials);

            }
        }

        throw new AlfrescoRuntimeException(
                    "Cannot update Credentials which haven't been persisted yet!");
    }

    /**
     * @param remoteSystemId
     * @param accessToken
     * @param refreshToken
     * @param expiresAt
     * @param issuedAt
     * @return OAuth2CredentialsInfo
     */
    private OAuth2CredentialsInfo buildPersonalOAuth2CredentialsInfo(String remoteSystemId,
                String accessToken, String refreshToken, Date expiresAt, Date issuedAt)
    {
        OAuth2CredentialsInfoImpl credentials = new OAuth2CredentialsInfoImpl();

        OAuth2CredentialsInfoImpl existing = (OAuth2CredentialsInfoImpl) getPersonalOAuth2Credentials(remoteSystemId);
        if (existing != null)
        {
            credentials = existing;
        }

        credentials.setOauthAccessToken(accessToken);
        credentials.setOauthRefreshToken(refreshToken);
        credentials.setOauthTokenExpiresAt(expiresAt);
        if (issuedAt != null)
        {
            credentials.setOauthTokenIssuedAt(issuedAt);
        }
        else
        {
            credentials.setOauthTokenIssuedAt(new Date());
        }

        return credentials;
    }

    /**
     * @param remoteSystemId
     * @param accessToken
     * @param refreshToken
     * @param expiresAt
     * @param issuedAt
     * @return OAuth2CredentialsInfo
     */
    private OAuth2CredentialsInfo buildSharedOAuth2CredentialsInfo(String remoteSystemId,
                String accessToken, String refreshToken, Date expiresAt, Date issuedAt)
    {
        OAuth2CredentialsInfoImpl credentials = new OAuth2CredentialsInfoImpl();

        credentials.setOauthAccessToken(accessToken);
        credentials.setOauthRefreshToken(refreshToken);
        credentials.setOauthTokenExpiresAt(expiresAt);
        if (issuedAt != null)
        {
            credentials.setOauthTokenIssuedAt(issuedAt);
        }
        else
        {
            credentials.setOauthTokenIssuedAt(new Date());
        }

        return credentials;
    }

    /**
     * Get the current users OAuth2Credentials for the remote systems
     * 
     * @param remoteSystemId
     * @return OAuth2CredentialsInfo
     */
    @Override
    public OAuth2CredentialsInfo getPersonalOAuth2Credentials(String remoteSystemId)
                throws NoSuchSystemException
    {
        return (OAuth2CredentialsInfo) remoteCredentialsService
                    .getPersonCredentials(remoteSystemId);
    }

    /**
     * @param remoteSystemId
     * @return List<OAuth2CredentialInfo>
     */
    @Override
    public List<OAuth2CredentialsInfo> listSharedOAuth2Credentials(String remoteSystemId)
                throws NoSuchSystemException
    {
        PagingRequest paging = new PagingRequest(CannedQueryPageDetails.DEFAULT_PAGE_SIZE);
        @SuppressWarnings("unchecked")
        PagingResults<OAuth2CredentialsInfo> pagingResults = (PagingResults<OAuth2CredentialsInfo>) remoteCredentialsService
                    .listSharedCredentials(remoteSystemId,
                                RemoteCredentialsModel.TYPE_OAUTH2_CREDENTIALS, paging);
        return pagingResults.getPage();
    }

    /**
     * Delete the current users OAuth2 Credentials for the remote system
     * 
     * @param remoteSystemId
     * @return boolean
     */
    @Override
    public boolean deletePersonalOAuth2Credentials(String remoteSystemId)
                throws NoSuchSystemException
    {
        OAuth2CredentialsInfo credentials = getPersonalOAuth2Credentials(remoteSystemId);

        if (credentials == null) { return false; }

        remoteCredentialsService.deleteCredentials(credentials);

        return true;
    }

    @Override
    public boolean deleteSharedOAuth2Credentials(String remoteSystemId,
                OAuth2CredentialsInfo credentials) throws NoSuchSystemException
    {
        List<OAuth2CredentialsInfo> shared = listSharedOAuth2Credentials(remoteSystemId);

        if (shared.isEmpty()) { return false; }

        for (OAuth2CredentialsInfo credential : shared)
        {
            if (credential.getNodeRef().equals(credentials.getNodeRef()))
            {
                remoteCredentialsService.deleteCredentials(credential);
            }
            else
            {
                return false;
            }
        }

        return true;
    }

    /**
     * @param succeeded
     * @param credentials
     * @return
     */
    @Override
    public OAuth2CredentialsInfo updateCredentialsAuthenticationSucceeded(boolean succeeded,
                OAuth2CredentialsInfo credentials)
    {
        return (OAuth2CredentialsInfo) remoteCredentialsService
                    .updateCredentialsAuthenticationSucceeded(succeeded, credentials);
    }
}
