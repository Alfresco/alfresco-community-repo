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

import com.nimbusds.openid.connect.sdk.claims.PersonClaims;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade.DecodedAccessToken;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * This class handles Just in Time user provisioning. It extracts {@link OIDCUserInfo} from {@link IdentityServiceFacade.DecodedAccessToken} or {@link UserInfo} and creates a new user if it does not exist in the repository.
 */
public class IdentityServiceJITProvisioningHandler
{
    private static final String DEFAULT_USERNAME_CLAIM = PersonClaims.PREFERRED_USERNAME_CLAIM_NAME;
    private static final String EMPTY_STRING = "";
    
    private final IdentityServiceConfig identityServiceConfig;
    private final IdentityServiceFacade identityServiceFacade;
    private final PersonService personService;
    private final TransactionService transactionService;

    private final BiFunction<DecodedAccessToken, UserInfoAttrMapping, Optional<? extends OIDCUserInfo>> mapTokenToUserInfoResponse = (token, userMappingClaim) -> {
        Optional<String> firstName = getClaim(token, userMappingClaim.firstNameClaim());
        Optional<String> lastName = getClaim(token, userMappingClaim.lastNameClaim());
        Optional<String> email = getClaim(token, userMappingClaim.emailClaim());

        return getClaim(token, Optional.ofNullable(userMappingClaim.usernameClaim())
                    .filter(StringUtils::isNotBlank)
                    .orElse(DEFAULT_USERNAME_CLAIM))
                .map(this::normalizeUserId)
                .map(username -> new OIDCUserInfo(username, firstName.orElse(EMPTY_STRING), lastName.orElse(EMPTY_STRING), email.orElse(EMPTY_STRING)));
    };

    public IdentityServiceJITProvisioningHandler(IdentityServiceFacade identityServiceFacade,
            PersonService personService,
            TransactionService transactionService,
            IdentityServiceConfig identityServiceConfig)
    {
        this.identityServiceFacade = identityServiceFacade;
        this.personService = personService;
        this.transactionService = transactionService;
        this.identityServiceConfig = identityServiceConfig;
    }

    public Optional<OIDCUserInfo> extractUserInfoAndCreateUserIfNeeded(String bearerToken)
    {
        UserInfoAttrMapping userInfoAttrMapping = new UserInfoAttrMapping(identityServiceFacade.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName(),
            identityServiceConfig.getFirstNameAttribute(),
            identityServiceConfig.getLastNameAttribute(),
            identityServiceConfig.getEmailAttribute());
        Optional<OIDCUserInfo> userInfoResponse = Optional.ofNullable(bearerToken)
                .filter(Predicate.not(String::isEmpty))
                .flatMap(token -> extractUserInfoResponseFromAccessToken(token, userInfoAttrMapping)
                        .filter(userInfo -> StringUtils.isNotEmpty(userInfo.username()))
                        .or(() -> extractUserInfoResponseFromEndpoint(token, userInfoAttrMapping)));

        if (transactionService.isReadOnly() || userInfoResponse.isEmpty())
        {
            return userInfoResponse;
        }
        return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Optional<OIDCUserInfo>>()
        {
            @Override
            public Optional<OIDCUserInfo> doWork() throws Exception
            {
                return userInfoResponse.map(userInfo -> {
                    if (userInfo.username() != null && personService.createMissingPeople()
                            && !personService.personExists(userInfo.username()))
                    {

                        if (!userInfo.allFieldsNotEmpty())
                        {
                            userInfo = extractUserInfoResponseFromEndpoint(bearerToken, userInfoAttrMapping).orElse(userInfo);
                        }
                        createPerson(userInfo);
                    }
                    return userInfo;
                });
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    private Optional<OIDCUserInfo> extractUserInfoResponseFromAccessToken(String bearerToken, UserInfoAttrMapping userInfoAttrMapping)
    {
        return Optional.ofNullable(bearerToken)
                .map(identityServiceFacade::decodeToken)
                .flatMap(decodedToken -> mapTokenToUserInfoResponse.apply(decodedToken, userInfoAttrMapping));
    }

    private Optional<OIDCUserInfo> extractUserInfoResponseFromEndpoint(String bearerToken, UserInfoAttrMapping userInfoAttrMapping)
    {
        return identityServiceFacade.getUserInfo(bearerToken, userInfoAttrMapping)
                .filter(userInfo -> userInfo.username() != null && !userInfo.username().isEmpty())
                .map(userInfo -> new OIDCUserInfo(normalizeUserId(userInfo.username()),
                        Optional.ofNullable(userInfo.firstName()).orElse(EMPTY_STRING),
                        Optional.ofNullable(userInfo.lastName()).orElse(EMPTY_STRING),
                        Optional.ofNullable(userInfo.email()).orElse(EMPTY_STRING)));
    }

    /**
     * Normalizes a user id, taking into account existing user accounts and case sensitivity settings.
     *
     * @param userId
     *         the user id
     * @return the string
     */
    private String normalizeUserId(final String userId)
    {
        if (userId == null)
        {
            return null;
        }

        String normalized = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<String>()
        {
            @Override
            public String doWork() throws Exception
            {
                return personService.getUserIdentifier(userId);
            }
        }, AuthenticationUtil.getSystemUserName());

        return normalized == null ? userId : normalized;
    }

    private Optional<String> getClaim(DecodedAccessToken token, String claimName) {
        return Optional.ofNullable(token)
                .map(jwtToken -> jwtToken.getClaim(claimName))
                .filter(String.class::isInstance)
                .map(String.class::cast);
    }

    private void createPerson(OIDCUserInfo userInfo) {
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_USERNAME, userInfo.username());
        properties.put(ContentModel.PROP_FIRSTNAME, userInfo.firstName());
        properties.put(ContentModel.PROP_LASTNAME, userInfo.lastName());
        properties.put(ContentModel.PROP_EMAIL, userInfo.email());
        properties.put(ContentModel.PROP_ORGID, EMPTY_STRING);
        properties.put(ContentModel.PROP_HOME_FOLDER_PROVIDER, null);
        properties.put(ContentModel.PROP_SIZE_CURRENT, 0L);
        properties.put(ContentModel.PROP_SIZE_QUOTA, -1L); // no quota

        personService.createPerson(properties);
    }

}
