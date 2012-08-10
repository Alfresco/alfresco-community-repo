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
package org.alfresco.service.cmr.oauth1;

import java.util.List;

import org.alfresco.service.Auditable;
import org.alfresco.service.cmr.remotecredentials.OAuth1CredentialsInfo;
import org.alfresco.service.cmr.remoteticket.NoSuchSystemException;

/**
 *
 * @author Jared Ottley
 */
public interface OAuth1CredentialsStoreService
{

    @Auditable(parameters = { "remoteSystemId" })
    public abstract OAuth1CredentialsInfo storePersonalOAuth1Credentials(String remoteSystemId,
                String token, String secret)
                throws NoSuchSystemException;

    @Auditable(parameters = { "remoteSystemId" })
    public abstract OAuth1CredentialsInfo storeSharedOAuth1Credentials(String remoteSystemId,
                String token, String secret)
                throws NoSuchSystemException;

    @Auditable(parameters = { "remoteSystemId" })
    public abstract OAuth1CredentialsInfo getPersonalOAuth1Credentials(String remoteSystemId)
                throws NoSuchSystemException;

    @Auditable(parameters = { "remoteSystemId" })
    public abstract OAuth1CredentialsInfo updateSharedOAuth1Credentials(
                OAuth1CredentialsInfo exisitingCredentials, String remoteSystemId,
                String token, String secret)
                throws NoSuchSystemException;

    @Auditable(parameters = { "remoteSystemId" })
    public abstract List<OAuth1CredentialsInfo> listSharedOAuth1Credentials(String remoteSystemId)
                throws NoSuchSystemException;

    @Auditable(parameters = { "remoteSystemId" })
    public abstract boolean deletePersonalOAuth1Credentials(String remoteSystemId)
                throws NoSuchSystemException;

    @Auditable(parameters = { "remoteSystemId" })
    public abstract boolean deleteSharedOAuth1Credentials(String remoteSystemId,
                OAuth1CredentialsInfo credentials) throws NoSuchSystemException;

    @Auditable(parameters = { "remoteSystemId" })
    public abstract OAuth1CredentialsInfo updateCredentialsAuthenticationSucceeded(
                boolean succeeded, OAuth1CredentialsInfo credentials);

}