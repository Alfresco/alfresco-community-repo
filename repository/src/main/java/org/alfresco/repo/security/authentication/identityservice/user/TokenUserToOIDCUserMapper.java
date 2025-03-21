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

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.security.PersonService;

public class TokenUserToOIDCUserMapper
{
    private final PersonService personService;

    public TokenUserToOIDCUserMapper(PersonService personService)
    {
        this.personService = personService;
    }

    /**
     * Maps a decoded token user to an OIDC user where the user id (username) is normalized.
     *
     * @param decodedTokenUser
     *            the decoded token user
     * @return the OIDC user
     */
    public OIDCUserInfo toOIDCUser(DecodedTokenUser decodedTokenUser)
    {
        return new OIDCUserInfo(usernameToUserId(decodedTokenUser.username()), decodedTokenUser.firstName(), decodedTokenUser.lastName(), decodedTokenUser.email());
    }

    /**
     * Normalizes a username, taking into account existing user accounts and case sensitivity settings.
     *
     * @param caseSensitiveUserName
     *            the case-sensitive username
     * @return the string
     */
    private String usernameToUserId(final String caseSensitiveUserName)
    {
        if (caseSensitiveUserName == null)
        {
            return null;
        }

        String normalized = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<String>() {
            @Override
            public String doWork() throws Exception
            {
                return personService.getUserIdentifier(caseSensitiveUserName);
            }
        }, AuthenticationUtil.getSystemUserName());

        return normalized == null ? caseSensitiveUserName : normalized;
    }
}
