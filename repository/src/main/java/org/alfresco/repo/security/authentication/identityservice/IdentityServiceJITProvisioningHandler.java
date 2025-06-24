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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.identityservice.user.AccessTokenToDecodedTokenUserMapper;
import org.alfresco.repo.security.authentication.identityservice.user.DecodedTokenUser;
import org.alfresco.repo.security.authentication.identityservice.user.OIDCUserInfo;
import org.alfresco.repo.security.authentication.identityservice.user.TokenUserToOIDCUserMapper;
import org.alfresco.repo.security.authentication.identityservice.user.UserInfoAttrMapping;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;

/**
 * This class handles Just in Time user provisioning. It extracts {@link OIDCUserInfo} from the given bearer token and creates a new user if it does not exist in the repository.
 */
public class IdentityServiceJITProvisioningHandler
{
    private final IdentityServiceFacade identityServiceFacade;
    private final PersonService personService;
    private final TransactionService transactionService;
    private final IdentityServiceConfig identityServiceConfig;
    private UserInfoAttrMapping userInfoAttrMapping;
    private TokenUserToOIDCUserMapper tokenUserToOIDCUserMapper;
    private AccessTokenToDecodedTokenUserMapper tokenToDecodedTokenUserMapper;

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

    /**
     * Extracts {@link OIDCUserInfo} from the given bearer token and creates a new user if it does not exist in the repository. Call to the UserInfo endpoint is made only if the token does not contain a username claim or if user needs to be created and some of the {@link OIDCUserInfo} fields are empty.
     */
    public Optional<OIDCUserInfo> extractUserInfoAndCreateUserIfNeeded(String bearerToken)
    {
        if (userInfoAttrMapping == null)
        {
            initMappers(identityServiceConfig);
        }

        Optional<OIDCUserInfo> oidcUserInfo = Optional.ofNullable(bearerToken)
                .filter(Predicate.not(String::isEmpty))
                .flatMap(token -> extractUserInfoResponseFromAccessToken(token).filter(decodedTokenUser -> StringUtils.isNotEmpty(decodedTokenUser.username()))
                        .or(() -> extractUserInfoResponseFromEndpoint(token, userInfoAttrMapping)))
                .map(tokenUserToOIDCUserMapper::toOIDCUser);

        if (transactionService.isReadOnly() || oidcUserInfo.isEmpty())
        {
            return oidcUserInfo;
        }
        return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<>() {
            @Override
            public Optional<OIDCUserInfo> doWork() throws Exception
            {
                return oidcUserInfo.map(oidcUser -> {
                    if (userDoesNotExistsAndCanBeCreated(oidcUser))
                    {

                        if (!oidcUser.allFieldsNotEmpty())
                        {
                            oidcUser = extractUserInfoResponseFromEndpoint(bearerToken, userInfoAttrMapping)
                                    .map(tokenUserToOIDCUserMapper::toOIDCUser)
                                    .orElse(oidcUser);
                        }
                        createPerson(oidcUser);
                    }
                    return oidcUser;
                });
            }

        }, AuthenticationUtil.getSystemUserName());
    }

    private void initMappers(IdentityServiceConfig identityServiceConfig)
    {
        this.userInfoAttrMapping = initUserInfoAttrMapping(identityServiceConfig);
        this.tokenUserToOIDCUserMapper = new TokenUserToOIDCUserMapper(personService);
        this.tokenToDecodedTokenUserMapper = new AccessTokenToDecodedTokenUserMapper(userInfoAttrMapping);
    }

    private boolean userDoesNotExistsAndCanBeCreated(OIDCUserInfo userInfo)
    {
        return userInfo.username() != null && personService.createMissingPeople()
                && !personService.personExists(userInfo.username());
    }

    private Optional<DecodedTokenUser> extractUserInfoResponseFromAccessToken(String bearerToken)
    {
        return Optional.ofNullable(bearerToken)
                .map(identityServiceFacade::decodeToken)
                .flatMap(tokenToDecodedTokenUserMapper::toDecodedTokenUser);
    }

    private Optional<DecodedTokenUser> extractUserInfoResponseFromEndpoint(String bearerToken, UserInfoAttrMapping userInfoAttrMapping)
    {
        return identityServiceFacade.getUserInfo(bearerToken, userInfoAttrMapping)
                .filter(userInfo -> userInfo.username() != null && !userInfo.username().isEmpty());
    }

    private void createPerson(OIDCUserInfo userInfo)
    {
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_USERNAME, userInfo.username());
        properties.put(ContentModel.PROP_FIRSTNAME, userInfo.firstName());
        properties.put(ContentModel.PROP_LASTNAME, userInfo.lastName());
        properties.put(ContentModel.PROP_EMAIL, userInfo.email());
        properties.put(ContentModel.PROP_ORGID, "");
        properties.put(ContentModel.PROP_HOME_FOLDER_PROVIDER, null);
        properties.put(ContentModel.PROP_SIZE_CURRENT, 0L);
        properties.put(ContentModel.PROP_SIZE_QUOTA, -1L); // no quota

        personService.createPerson(properties);
    }

    private UserInfoAttrMapping initUserInfoAttrMapping(IdentityServiceConfig identityServiceConfig)
    {
        return new UserInfoAttrMapping(identityServiceFacade.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName(),
                identityServiceConfig.getFirstNameAttribute(),
                identityServiceConfig.getLastNameAttribute(),
                identityServiceConfig.getEmailAttribute());
    }
}
