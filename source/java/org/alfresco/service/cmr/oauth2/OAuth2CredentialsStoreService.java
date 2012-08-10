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
package org.alfresco.service.cmr.oauth2;

import java.util.Date;
import java.util.List;

import org.alfresco.service.Auditable;
import org.alfresco.service.cmr.remotecredentials.OAuth2CredentialsInfo;
import org.alfresco.service.cmr.remoteticket.NoSuchSystemException;

/**
 *
 * @author Jared Ottley
 */
public interface OAuth2CredentialsStoreService
{

    @Auditable(parameters = { "remoteSystemId" })
    public abstract OAuth2CredentialsInfo storePersonalOAuth2Credentials(String remoteSystemId,
                String accessToken, String refreshToken, Date expiresAt, Date issuedAt)
                throws NoSuchSystemException;

    @Auditable(parameters = { "remoteSystemId" })
    public abstract OAuth2CredentialsInfo storeSharedOAuth2Credentials(String remoteSystemId,
                String accessToken, String refreshToken, Date expiresAt, Date issuedAt)
                throws NoSuchSystemException;

    @Auditable(parameters = { "remoteSystemId" })
    public abstract OAuth2CredentialsInfo getPersonalOAuth2Credentials(String remoteSystemId)
                throws NoSuchSystemException;

    @Auditable(parameters = { "remoteSystemId" })
    public abstract OAuth2CredentialsInfo updateSharedOAuth2Credentials(
                OAuth2CredentialsInfo exisitingCredentials, String remoteSystemId,
                String accessToken, String refreshToken, Date expiresAt, Date issuedAt)
                throws NoSuchSystemException;

    @Auditable(parameters = { "remoteSystemId" })
    public abstract List<OAuth2CredentialsInfo> listSharedOAuth2Credentials(String remoteSystemId)
                throws NoSuchSystemException;

    @Auditable(parameters = { "remoteSystemId" })
    public abstract boolean deletePersonalOAuth2Credentials(String remoteSystemId)
                throws NoSuchSystemException;

    @Auditable(parameters = { "remoteSystemId" })
    public abstract boolean deleteSharedOAuth2Credentials(String remoteSystemId,
                OAuth2CredentialsInfo credentials) throws NoSuchSystemException;

    @Auditable(parameters = { "remoteSystemId" })
    public abstract OAuth2CredentialsInfo updateCredentialsAuthenticationSucceeded(
                boolean succeeded, OAuth2CredentialsInfo credentials);

}