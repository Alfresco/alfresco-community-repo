/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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
package org.alfresco.repo.security.authentication.identityservice;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Class to hold configuration for the Identity Service.
 *
 * @author Gavin Cornwell
 */
@SuppressWarnings("PMD.ExcessivePublicCount")
public class IdentityServiceConfig
{
    private static final String REALMS = "realms";

    private int clientConnectionTimeout;
    private int clientSocketTimeout;
    private String issuerUrl;
    private String audience;
    // client id
    private String resource;
    private String clientSecret;
    private String authServerUrl;
    private String realm;
    private int connectionPoolSize;
    private boolean allowAnyHostname;
    private boolean disableTrustManager;
    private String truststore;
    private String truststorePassword;
    private String clientKeystore;
    private String clientKeystorePassword;
    private String clientKeyPassword;
    private String realmKey;
    private int publicKeyCacheTtl;
    private boolean publicClient;
    private String principalAttribute;
    private boolean clientIdValidationDisabled;
    private String adminConsoleRedirectPath;
    private String signatureAlgorithms;
    private String adminConsoleScopes;
    private String passwordGrantScopes;
    private String issuerAttribute;
    private String firstNameAttribute;
    private String lastNameAttribute;
    private String emailAttribute;
    private long jwtClockSkewMs;
    private String webScriptsHomeRedirectPath;
    private String webScriptsHomeScopes;

    public String getWebScriptsHomeRedirectPath()
    {
        return webScriptsHomeRedirectPath;
    }

    public void setWebScriptsHomeRedirectPath(String webScriptsHomeRedirectPath)
    {
        this.webScriptsHomeRedirectPath = webScriptsHomeRedirectPath;
    }

    /**
     *
     * @return Client connection timeout in milliseconds.
     */
    public int getClientConnectionTimeout()
    {
        return clientConnectionTimeout;
    }

    /**
     *
     * @param clientConnectionTimeout
     *            Client connection timeout in milliseconds.
     */
    public void setClientConnectionTimeout(int clientConnectionTimeout)
    {
        this.clientConnectionTimeout = clientConnectionTimeout;
    }

    /**
     *
     * @return Client socket timeout in milliseconds.s
     */
    public int getClientSocketTimeout()
    {
        return clientSocketTimeout;
    }

    /**
     *
     * @param clientSocketTimeout
     *            Client socket timeout in milliseconds.
     */
    public void setClientSocketTimeout(int clientSocketTimeout)
    {
        this.clientSocketTimeout = clientSocketTimeout;
    }

    public void setConnectionPoolSize(int connectionPoolSize)
    {
        this.connectionPoolSize = connectionPoolSize;
    }

    public int getConnectionPoolSize()
    {
        return connectionPoolSize;
    }

    public String getIssuerUrl()
    {
        return issuerUrl;
    }

    public void setIssuerUrl(String issuerUrl)
    {
        this.issuerUrl = issuerUrl;
    }

    public String getAudience()
    {
        return audience;
    }

    public void setAudience(String audience)
    {
        this.audience = audience;
    }

    public String getAuthServerUrl()
    {
        return Optional.ofNullable(realm)
                .filter(StringUtils::isNotBlank)
                .filter(realm -> StringUtils.isNotBlank(authServerUrl))
                .map(realm -> UriComponentsBuilder.fromUriString(authServerUrl)
                        .pathSegment(REALMS, realm)
                        .build()
                        .toString())
                .orElse(authServerUrl);
    }

    public void setAuthServerUrl(String authServerUrl)
    {
        this.authServerUrl = authServerUrl;
    }

    public String getRealm()
    {
        return realm;
    }

    public void setRealm(String realm)
    {
        this.realm = realm;
    }

    public String getResource()
    {
        return resource;
    }

    public void setResource(String resource)
    {
        this.resource = resource;
    }

    public void setClientSecret(String clientSecret)
    {
        this.clientSecret = clientSecret;
    }

    public String getClientSecret()
    {
        return Optional.ofNullable(clientSecret)
                .orElse("");
    }

    public void setAllowAnyHostname(boolean allowAnyHostname)
    {
        this.allowAnyHostname = allowAnyHostname;
    }

    public boolean isAllowAnyHostname()
    {
        return allowAnyHostname;
    }

    public void setDisableTrustManager(boolean disableTrustManager)
    {
        this.disableTrustManager = disableTrustManager;
    }

    public boolean isDisableTrustManager()
    {
        return disableTrustManager;
    }

    public void setTruststore(String truststore)
    {
        this.truststore = truststore;
    }

    public String getTruststore()
    {
        return truststore;
    }

    public void setTruststorePassword(String truststorePassword)
    {
        this.truststorePassword = truststorePassword;
    }

    public String getTruststorePassword()
    {
        return truststorePassword;
    }

    public void setClientKeystore(String clientKeystore)
    {
        this.clientKeystore = clientKeystore;
    }

    public String getClientKeystore()
    {
        return clientKeystore;
    }

    public void setClientKeystorePassword(String clientKeystorePassword)
    {
        this.clientKeystorePassword = clientKeystorePassword;
    }

    public String getClientKeystorePassword()
    {
        return clientKeystorePassword;
    }

    public void setClientKeyPassword(String clientKeyPassword)
    {
        this.clientKeyPassword = clientKeyPassword;
    }

    public String getClientKeyPassword()
    {
        return clientKeyPassword;
    }

    public void setRealmKey(String realmKey)
    {
        this.realmKey = realmKey;
    }

    public String getRealmKey()
    {
        return realmKey;
    }

    public void setPublicKeyCacheTtl(int publicKeyCacheTtl)
    {
        this.publicKeyCacheTtl = publicKeyCacheTtl;
    }

    public int getPublicKeyCacheTtl()
    {
        return publicKeyCacheTtl;
    }

    public void setPublicClient(boolean publicClient)
    {
        this.publicClient = publicClient;
    }

    public boolean isPublicClient()
    {
        return publicClient;
    }

    public String getPrincipalAttribute()
    {
        return principalAttribute;
    }

    public void setPrincipalAttribute(String principalAttribute)
    {
        this.principalAttribute = principalAttribute;
    }

    public boolean isClientIdValidationDisabled()
    {
        return clientIdValidationDisabled;
    }

    public void setClientIdValidationDisabled(boolean clientIdValidationDisabled)
    {
        this.clientIdValidationDisabled = clientIdValidationDisabled;
    }

    public String getAdminConsoleRedirectPath()
    {
        return adminConsoleRedirectPath;
    }

    public void setAdminConsoleRedirectPath(String adminConsoleRedirectPath)
    {
        this.adminConsoleRedirectPath = adminConsoleRedirectPath;
    }

    public Set<SignatureAlgorithm> getSignatureAlgorithms()
    {
        return Stream.of(signatureAlgorithms.split(","))
                .map(String::trim)
                .map(SignatureAlgorithm::from)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
    }

    public void setSignatureAlgorithms(String signatureAlgorithms)
    {
        this.signatureAlgorithms = signatureAlgorithms;
    }

    public String getIssuerAttribute()
    {
        return issuerAttribute;
    }

    public void setIssuerAttribute(String issuerAttribute)
    {
        this.issuerAttribute = issuerAttribute;
    }

    public Set<String> getAdminConsoleScopes()
    {
        return Stream.of(adminConsoleScopes.split(","))
                .map(String::trim)
                .collect(Collectors.toUnmodifiableSet());
    }

    public void setAdminConsoleScopes(String adminConsoleScopes)
    {
        this.adminConsoleScopes = adminConsoleScopes;
    }

    public Set<String> getWebScriptsHomeScopes()
    {
        return Stream.of(webScriptsHomeScopes.split(","))
                .map(String::trim)
                .collect(Collectors.toUnmodifiableSet());
    }

    public void setWebScriptsHomeScopes(String webScriptsHomeScopes)
    {
        this.webScriptsHomeScopes = webScriptsHomeScopes;
    }

    public Set<String> getPasswordGrantScopes()
    {
        return Stream.of(passwordGrantScopes.split(","))
                .map(String::trim)
                .collect(Collectors.toUnmodifiableSet());
    }

    public void setPasswordGrantScopes(String passwordGrantScopes)
    {
        this.passwordGrantScopes = passwordGrantScopes;
    }

    public void setFirstNameAttribute(String firstNameAttribute)
    {
        this.firstNameAttribute = firstNameAttribute;
    }

    public void setLastNameAttribute(String lastNameAttribute)
    {
        this.lastNameAttribute = lastNameAttribute;
    }

    public void setEmailAttribute(String emailAttribute)
    {
        this.emailAttribute = emailAttribute;
    }

    public void setJwtClockSkewMs(long jwtClockSkewMs)
    {
        this.jwtClockSkewMs = jwtClockSkewMs;
    }

    public String getFirstNameAttribute()
    {
        return firstNameAttribute;
    }

    public String getLastNameAttribute()
    {
        return lastNameAttribute;
    }

    public String getEmailAttribute()
    {
        return emailAttribute;
    }

    public long getJwtClockSkewMs()
    {
        return jwtClockSkewMs;
    }
}
