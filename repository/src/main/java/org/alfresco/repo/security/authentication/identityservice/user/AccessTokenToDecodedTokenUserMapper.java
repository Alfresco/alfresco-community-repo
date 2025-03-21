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
package org.alfresco.repo.security.authentication.identityservice.user;

import java.util.Optional;

import com.nimbusds.openid.connect.sdk.claims.PersonClaims;
import org.apache.commons.lang3.StringUtils;

import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade;

public class AccessTokenToDecodedTokenUserMapper
{
    private static final String DEFAULT_USERNAME_CLAIM = PersonClaims.PREFERRED_USERNAME_CLAIM_NAME;

    private final UserInfoAttrMapping userInfoAttrMapping;

    public AccessTokenToDecodedTokenUserMapper(UserInfoAttrMapping userInfoAttrMapping)
    {
        this.userInfoAttrMapping = userInfoAttrMapping;
    }

    /**
     * Maps the given {@link IdentityServiceFacade.DecodedAccessToken} to a {@link DecodedTokenUser}.
     *
     * @param token
     *            the token to map
     * @return the mapped {@link DecodedTokenUser} or {@link Optional#empty()} if the token does not contain a username claim
     */
    public Optional<DecodedTokenUser> toDecodedTokenUser(IdentityServiceFacade.DecodedAccessToken token)
    {
        Object firstName = token.getClaim(userInfoAttrMapping.firstNameClaim());
        Object lastName = token.getClaim(userInfoAttrMapping.lastNameClaim());
        Object email = token.getClaim(userInfoAttrMapping.emailClaim());

        return Optional.ofNullable(token.getClaim(Optional.ofNullable(userInfoAttrMapping.usernameClaim())
                .filter(StringUtils::isNotBlank)
                .orElse(DEFAULT_USERNAME_CLAIM)))
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(username -> DecodedTokenUser.validateAndCreate(username, firstName, lastName, email));
    }
}
