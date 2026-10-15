/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(IdentityServiceConfig.class);
    private static final String REALMS = "realms";
    private static final char PARAM_DELIMITER = ',';
    private static final char PARAM_SEPARATOR = '=';
    private static final char QUOTE = '\'';

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
    private boolean scopeValidationDisabled;
    private Map<String, String> authorizationAdditionalParams = Collections.emptyMap();

    public boolean isScopeValidationDisabled()
    {
        return scopeValidationDisabled;
    }

    public void setScopeValidationDisabled(boolean scopeValidationDisabled)
    {
        this.scopeValidationDisabled = scopeValidationDisabled;
    }

    public Map<String, String> getAuthorizationAdditionalParams()
    {
        return authorizationAdditionalParams;
    }

    /**
     * Parses a comma-delimited list of additional OIDC parameters in {@code name=value} format. Values containing commas can be single-quoted, using {@code ''} for a literal quote. Malformed entries are ignored and duplicate names use the last value.
     *
     * @param additionalParams
     *            configured additional authorization parameters, or {@code null}
     */
    public void setAuthorizationAdditionalParams(String additionalParams)
    {
        if (StringUtils.isBlank(additionalParams))
        {
            authorizationAdditionalParams = Collections.emptyMap();
            return;
        }

        authorizationAdditionalParams = Collections.unmodifiableMap(parseAdditionalParams(additionalParams));
    }

    /** Parses all configured entries into an insertion-ordered parameter map. */
    private static Map<String, String> parseAdditionalParams(String additionalParams)
    {
        final Map<String, String> parsedParams = new LinkedHashMap<>();
        final List<String> entries = splitAdditionalParams(additionalParams);
        for (int index = 0; index < entries.size(); index++)
        {
            parseAdditionalParam(entries.get(index), index + 1, parsedParams);
        }
        return parsedParams;
    }

    /** Splits on delimiters outside quoted values. */
    private static List<String> splitAdditionalParams(String additionalParams)
    {
        final List<String> entries = new ArrayList<>();
        int entryStart = 0;

        int delimiter = findEntryDelimiter(additionalParams, entryStart);
        while (delimiter >= 0)
        {
            entries.add(additionalParams.substring(entryStart, delimiter));
            entryStart = delimiter + 1;
            delimiter = findEntryDelimiter(additionalParams, entryStart);
        }
        entries.add(additionalParams.substring(entryStart));
        return entries;
    }

    /** Finds the next delimiter, ignoring delimiters inside a quoted value. */
    private static int findEntryDelimiter(String additionalParams, int entryStart)
    {
        final int delimiter = additionalParams.indexOf(PARAM_DELIMITER, entryStart);
        final int separator = additionalParams.indexOf(PARAM_SEPARATOR, entryStart);
        if (separator < 0 || delimiter >= 0 && delimiter < separator)
        {
            return delimiter;
        }

        final int valueStart = skipWhitespace(additionalParams, separator + 1);
        if (valueStart < additionalParams.length() && additionalParams.charAt(valueStart) == QUOTE)
        {
            final int closingQuote = findClosingQuote(additionalParams, valueStart + 1);
            return closingQuote < 0 ? -1 : additionalParams.indexOf(PARAM_DELIMITER, closingQuote + 1);
        }
        return delimiter;
    }

    /** Returns the position of the first non-whitespace character. */
    private static int skipWhitespace(String value, int position)
    {
        while (position < value.length() && Character.isWhitespace(value.charAt(position)))
        {
            position++;
        }
        return position;
    }

    /** Finds a closing quote while skipping escaped quote pairs. */
    private static int findClosingQuote(String value, int position)
    {
        int quote = value.indexOf(QUOTE, position);
        while (quote >= 0 && quote + 1 < value.length() && value.charAt(quote + 1) == QUOTE)
        {
            quote = value.indexOf(QUOTE, quote + 2);
        }
        return quote;
    }

    /** Validates and adds one configured parameter entry to the result map. */
    private static void parseAdditionalParam(String entry, int entryNumber, Map<String, String> parsedParams)
    {
        final int separator = entry.indexOf(PARAM_SEPARATOR);
        if (separator < 0)
        {
            // Tolerate empty entries, e.g. a trailing or doubled delimiter.
            if (!entry.isBlank())
            {
                logMalformedAdditionalParam(entryNumber, "missing '" + PARAM_SEPARATOR + "' separator");
            }
            return;
        }

        final String name = entry.substring(0, separator).strip();
        if (name.isEmpty())
        {
            logMalformedAdditionalParam(entryNumber, "empty parameter name");
            return;
        }

        parseAdditionalParamValue(entry.substring(separator + 1), entryNumber)
                .ifPresent(value -> parsedParams.put(name, value));
    }

    /** Parses an unquoted or single-quoted parameter value. */
    private static Optional<String> parseAdditionalParamValue(String configuredValue, int entryNumber)
    {
        final String value = configuredValue.strip();
        if (value.isEmpty() || value.charAt(0) != QUOTE)
        {
            return Optional.of(value);
        }

        final StringBuilder parsedValue = new StringBuilder();
        int position = 1;
        while (position < value.length())
        {
            final char current = value.charAt(position);
            if (current != QUOTE)
            {
                parsedValue.append(current);
            }
            else if (position + 1 < value.length() && value.charAt(position + 1) == QUOTE)
            {
                parsedValue.append(QUOTE);
                position++;
            }
            else if (value.substring(position + 1).isBlank())
            {
                return Optional.of(parsedValue.toString());
            }
            else
            {
                logMalformedAdditionalParam(entryNumber, "unexpected characters after quoted value");
                return Optional.empty();
            }
            position++;
        }

        logMalformedAdditionalParam(entryNumber, "unterminated quoted value");
        return Optional.empty();
    }

    private static void logMalformedAdditionalParam(int entryNumber, String reason)
    {
        LOGGER.warn("Ignoring malformed entry {} in identity-service.authorization.additional-params: {}", entryNumber, reason);
    }

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
