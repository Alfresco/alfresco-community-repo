/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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
import java.util.function.Function;
import java.util.function.Predicate;

import com.nimbusds.openid.connect.sdk.claims.PersonClaims;
import com.nimbusds.openid.connect.sdk.claims.UserInfo;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;

/**
 * This class handles Just in Time user provisioning. It extracts {@link OIDCUserInfo}
 * from {@link IdentityServiceFacade.DecodedAccessToken} or {@link UserInfo}
 * and creates a new user if it does not exist in the repository.
 */
public class IdentityServiceJITProvisioningHandler
{
    private final IdentityServiceFacade identityServiceFacade;
    private final PersonService personService;
    private final TransactionService transactionService;

    private final Function<IdentityServiceFacade.DecodedAccessToken, Optional<? extends OIDCUserInfo>> mapTokenToUserInfoResponse = token -> {
        Optional<String> firstName = Optional.ofNullable(token.getClaim(PersonClaims.GIVEN_NAME_CLAIM_NAME))
            .filter(String.class::isInstance)
            .map(String.class::cast);
        Optional<String> lastName = Optional.ofNullable(token.getClaim(PersonClaims.FAMILY_NAME_CLAIM_NAME))
            .filter(String.class::isInstance)
            .map(String.class::cast);
        Optional<String> email = Optional.ofNullable(token.getClaim(PersonClaims.EMAIL_CLAIM_NAME))
            .filter(String.class::isInstance)
            .map(String.class::cast);

        return Optional.ofNullable(token.getClaim(PersonClaims.PREFERRED_USERNAME_CLAIM_NAME))
            .filter(String.class::isInstance)
            .map(String.class::cast)
            .map(this::normalizeUserId)
            .map(username -> new OIDCUserInfo(username, firstName.orElse(""), lastName.orElse(""), email.orElse("")));
    };

    public IdentityServiceJITProvisioningHandler(IdentityServiceFacade identityServiceFacade,
        PersonService personService,
        TransactionService transactionService)
    {
        this.identityServiceFacade = identityServiceFacade;
        this.personService = personService;
        this.transactionService = transactionService;
    }

    public Optional<OIDCUserInfo> extractUserInfoAndCreateUserIfNeeded(String bearerToken)
    {
        Optional<OIDCUserInfo> userInfoResponse = Optional.ofNullable(bearerToken)
            .filter(Predicate.not(String::isEmpty))
            .flatMap(token -> extractUserInfoResponseFromAccessToken(token).flatMap(
                response -> response.username() == null || response.username().isEmpty() ?
                    extractUserInfoResponseFromEndpoint(token) :
                    Optional.of(response)).or(() -> extractUserInfoResponseFromEndpoint(token)));

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
                            userInfo = extractUserInfoResponseFromEndpoint(bearerToken).orElse(
                                new OIDCUserInfo(userInfo.username(), "", "", ""));
                        }
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
                    return userInfo;
                });
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    private Optional<OIDCUserInfo> extractUserInfoResponseFromAccessToken(String bearerToken)
    {
        return Optional.ofNullable(bearerToken)
            .map(identityServiceFacade::decodeToken)
            .flatMap(mapTokenToUserInfoResponse);
    }

    private Optional<OIDCUserInfo> extractUserInfoResponseFromEndpoint(String bearerToken)
    {
        return identityServiceFacade.getUserInfo(bearerToken)
            .filter(userInfo -> userInfo.username() != null && !userInfo.username().isEmpty())
            .map(userInfo -> new OIDCUserInfo(normalizeUserId(userInfo.username()),
                Optional.ofNullable(userInfo.firstName()).orElse(""),
                Optional.ofNullable(userInfo.lastName()).orElse(""),
                Optional.ofNullable(userInfo.email()).orElse("")));
    }

    /**
     * Normalizes a user id, taking into account existing user accounts and case sensitivity settings.
     *
     * @param userId the user id
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

}
