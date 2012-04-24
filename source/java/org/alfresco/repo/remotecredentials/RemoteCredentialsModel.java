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

import org.alfresco.service.namespace.QName;

/**
 * Remote Credentials models constants
 * 
 * @author Nick Burch
 * @since Odin
 */
public interface RemoteCredentialsModel
{
    /** Remote Credentials Model */
    public static final String REMOTE_CREDENTIALS_MODEL_URL = "http://www.alfresco.org/model/remotecredentials/1.0";
    public static final String REMOTE_CREDENTIALS_MODEL_PREFIX = "rc";
    
    /** Applied to something that holds rc:remoteCredentialsSystem objects */
    public static final QName ASPECT_REMOTE_CREDENTIALS_SYSTEM_CONTAINER = QName.createQName(REMOTE_CREDENTIALS_MODEL_URL, "remoteCredentialsSystemContainer"); 
    public static final QName ASSOC_CREDENTIALS_SYSTEM = QName.createQName(REMOTE_CREDENTIALS_MODEL_URL, "credentialsSystem");
    
    /** Remote Credentials Holder */
    public static final QName TYPE_REMOTE_CREDENTIALS_SYSTEM = QName.createQName(REMOTE_CREDENTIALS_MODEL_URL, "remoteCredentialsSystem"); 
    public static final QName ASSOC_CREDENTIALS = QName.createQName(REMOTE_CREDENTIALS_MODEL_URL, "credentials");
    
    /** Credentials Base */
    public static final QName TYPE_CREDENTIALS_BASE = QName.createQName(REMOTE_CREDENTIALS_MODEL_URL, "credentialBase"); 
    public static final QName PROP_REMOTE_USERNAME = QName.createQName(REMOTE_CREDENTIALS_MODEL_URL, "remoteUsername"); 
    public static final QName PROP_LAST_AUTHENTICATION_SUCCEEDED = QName.createQName(REMOTE_CREDENTIALS_MODEL_URL, "lastAuthenticationSucceeded"); 
    
    /** Password Credentials */
    public static final QName TYPE_PASSWORD_CREDENTIALS = QName.createQName(REMOTE_CREDENTIALS_MODEL_URL, "passwordCredentials"); 
    public static final QName PROP_REMOTE_PASSWORD = QName.createQName(REMOTE_CREDENTIALS_MODEL_URL, "remotePassword"); 
    
    /** OAuth 1.0 Credentials */
    public static final QName TYPE_OAUTH1_CREDENTIALS = QName.createQName(REMOTE_CREDENTIALS_MODEL_URL, "oauth1Credentials"); 
    public static final QName PROP_OAUTH1_TOKEN = QName.createQName(REMOTE_CREDENTIALS_MODEL_URL, "oauth1Token"); 
    public static final QName PROP_OAUTH1_TOKEN_SECRET = QName.createQName(REMOTE_CREDENTIALS_MODEL_URL, "oauth1TokenSecret"); 
    
    /** OAuth 2.0 Credentials */
    public static final QName TYPE_OAUTH2_CREDENTIALS = QName.createQName(REMOTE_CREDENTIALS_MODEL_URL, "oauth2Credentials"); 
    public static final QName PROP_OAUTH2_ACCESS_TOKEN = QName.createQName(REMOTE_CREDENTIALS_MODEL_URL, "oauth2AccessToken"); 
    public static final QName PROP_OAUTH2_REFRESH_TOKEN = QName.createQName(REMOTE_CREDENTIALS_MODEL_URL, "oauth2RefreshToken"); 
    public static final QName PROP_OAUTH2_TOKEN_ISSUED_AT = QName.createQName(REMOTE_CREDENTIALS_MODEL_URL, "oauth2TokenIssuedAt"); 
    public static final QName PROP_OAUTH2_TOKEN_EXPIRES_AT = QName.createQName(REMOTE_CREDENTIALS_MODEL_URL, "oauth2TokenExpiresAt"); 
}