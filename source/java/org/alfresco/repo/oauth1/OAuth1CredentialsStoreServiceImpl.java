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
package org.alfresco.repo.oauth1;

import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.query.CannedQueryPageDetails;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.remotecredentials.OAuth1CredentialsInfoImpl;
import org.alfresco.repo.remotecredentials.RemoteCredentialsModel;
import org.alfresco.service.cmr.oauth1.OAuth1CredentialsStoreService;
import org.alfresco.service.cmr.remotecredentials.OAuth1CredentialsInfo;
import org.alfresco.service.cmr.remotecredentials.RemoteCredentialsService;
import org.alfresco.service.cmr.remoteticket.NoSuchSystemException;

/**
 * @author Jared Ottley
 */
public class OAuth1CredentialsStoreServiceImpl implements OAuth1CredentialsStoreService
{
    private RemoteCredentialsService remoteCredentialsService;

    public void setRemoteCredentialsService(RemoteCredentialsService remoteCredentialsService)
    {
        this.remoteCredentialsService = remoteCredentialsService;
    }

    /**
     * Add or Update OAuth1 Credentials for the current user to the OAuth1
     * Credential Store
     * 
     * @param remoteSystemId
     * @param token
     * @param secret

     * @return OAuth1CredentialsInfo
     */
    @Override
    public OAuth1CredentialsInfo storePersonalOAuth1Credentials(String remoteSystemId,
                String token, String secret)
                throws NoSuchSystemException
    {

        OAuth1CredentialsInfo credentials = buildPersonalOAuth1CredentialsInfo(remoteSystemId,
                    token, secret);

        if (credentials.getNodeRef() != null)
        {
            return (OAuth1CredentialsInfo) remoteCredentialsService.updateCredentials(credentials);
        }
        else
        {
            return (OAuth1CredentialsInfo) remoteCredentialsService.createPersonCredentials(
                        remoteSystemId, credentials);
        }

    }

    /**
     * Add Shared OAuth1 Credentials to the OAuth1 Credential Store
     * 
     * @param remoteSystemId
     * @param token
     * @param secret
     * @return OAuth1CredentialsInfo
     */
    @Override
    public OAuth1CredentialsInfo storeSharedOAuth1Credentials(String remoteSystemId,
                String token, String secret)
                throws NoSuchSystemException
    {
        OAuth1CredentialsInfo credentials = buildSharedOAuth1CredentialsInfo(remoteSystemId,
                    token, secret);

        return (OAuth1CredentialsInfo) remoteCredentialsService.createSharedCredentials(
                    remoteSystemId, credentials);
    }

    /**
     * @param exisitingCredentials
     * @param remoteSystemId
     * @param token
     * @param secret
     * @return OAuth1CredentialsInfo
     */
    @Override
    public OAuth1CredentialsInfo updateSharedOAuth1Credentials(
                OAuth1CredentialsInfo exisitingCredentials, String remoteSystemId,
                String token, String secret)
                throws NoSuchSystemException
    {
        List<OAuth1CredentialsInfo> shared = listSharedOAuth1Credentials(remoteSystemId);

        for (OAuth1CredentialsInfo credential : shared)
        {
            if (credential.getNodeRef().equals(exisitingCredentials.getNodeRef()))
            {
                OAuth1CredentialsInfoImpl credentials = new OAuth1CredentialsInfoImpl(
                            exisitingCredentials.getNodeRef(),
                            exisitingCredentials.getRemoteSystemName(),
                            exisitingCredentials.getRemoteSystemContainerNodeRef());

                credentials.setOAuthToken(token);
                credentials.setOAuthSecret(secret);

                return (OAuth1CredentialsInfo) remoteCredentialsService
                            .updateCredentials(credentials);

            }
        }

        throw new AlfrescoRuntimeException(
                    "Cannot update Credentials which haven't been persisted yet!");
    }

    /**
     * @param remoteSystemId
     * @param token
     * @param secret
     * @return OAuth1CredentialsInfo
     */
    private OAuth1CredentialsInfo buildPersonalOAuth1CredentialsInfo(String remoteSystemId,
                String token, String secret)
    {
        OAuth1CredentialsInfoImpl credentials = new OAuth1CredentialsInfoImpl();

        OAuth1CredentialsInfoImpl existing = (OAuth1CredentialsInfoImpl) getPersonalOAuth1Credentials(remoteSystemId);
        if (existing != null)
        {
            credentials = existing;
        }

        credentials.setOAuthToken(token);
        credentials.setOAuthSecret(secret);

        return credentials;
    }

    /**
     * @param remoteSystemId
     * @param token
     * @param secret
     * @return OAuth1CredentialsInfo
     */
    private OAuth1CredentialsInfo buildSharedOAuth1CredentialsInfo(String remoteSystemId,
                String token, String secret)
    {
        OAuth1CredentialsInfoImpl credentials = new OAuth1CredentialsInfoImpl();

        credentials.setOAuthToken(token);
        credentials.setOAuthSecret(secret);

        return credentials;
    }

    /**
     * Get the current users OAuth1Credentials for the remote systems
     * 
     * @param remoteSystemId
     * @return OAuth1CredentialsInfo
     */
    @Override
    public OAuth1CredentialsInfo getPersonalOAuth1Credentials(String remoteSystemId)
                throws NoSuchSystemException
    {
        return (OAuth1CredentialsInfo) remoteCredentialsService
                    .getPersonCredentials(remoteSystemId);
    }

    /**
     * @param remoteSystemId
     * @return List<OAuth1CredentialInfo>
     */
    @Override
    public List<OAuth1CredentialsInfo> listSharedOAuth1Credentials(String remoteSystemId)
                throws NoSuchSystemException
    {
        PagingRequest paging = new PagingRequest(CannedQueryPageDetails.DEFAULT_PAGE_SIZE);
        @SuppressWarnings("unchecked")
        PagingResults<OAuth1CredentialsInfo> pagingResults = (PagingResults<OAuth1CredentialsInfo>) remoteCredentialsService
                    .listSharedCredentials(remoteSystemId,
                                RemoteCredentialsModel.TYPE_OAUTH1_CREDENTIALS, paging);
        return pagingResults.getPage();
    }

    /**
     * Delete the current users OAuth1 Credentials for the remote system
     * 
     * @param remoteSystemId
     * @return boolean
     */
    @Override
    public boolean deletePersonalOAuth1Credentials(String remoteSystemId)
                throws NoSuchSystemException
    {
        OAuth1CredentialsInfo credentials = getPersonalOAuth1Credentials(remoteSystemId);

        if (credentials == null) { return false; }

        remoteCredentialsService.deleteCredentials(credentials);

        return true;
    }

    @Override
    public boolean deleteSharedOAuth1Credentials(String remoteSystemId,
                OAuth1CredentialsInfo credentials) throws NoSuchSystemException
    {
        List<OAuth1CredentialsInfo> shared = listSharedOAuth1Credentials(remoteSystemId);

        if (shared.isEmpty()) { return false; }

        for (OAuth1CredentialsInfo credential : shared)
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
    public OAuth1CredentialsInfo updateCredentialsAuthenticationSucceeded(boolean succeeded,
                OAuth1CredentialsInfo credentials)
    {
        return (OAuth1CredentialsInfo) remoteCredentialsService
                    .updateCredentialsAuthenticationSucceeded(succeeded, credentials);
    }
}
