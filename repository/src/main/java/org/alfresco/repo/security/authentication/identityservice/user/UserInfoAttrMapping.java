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

public class UserInfoAttrMapping
{
    private final String usernameClaim;
    private final String firstNameClaim;
    private final String lastNameClaim;
    private final String emailClaim;

    /**
     * The UserInfoAttrMapping class represents the mapping of claims fetched from the UserInfo endpoint to create an Alfresco user.
     *
     * @param usernameClaim
     *            the claim that represents the username
     * @param firstNameClaim
     *            the claim that represents the first name
     * @param lastNameClaim
     *            the claim that represents the last name
     * @param emailClaim
     *            the claim that represents the email
     */
    public UserInfoAttrMapping(String usernameClaim, String firstNameClaim, String lastNameClaim, String emailClaim)
    {
        this.usernameClaim = usernameClaim;
        this.firstNameClaim = firstNameClaim;
        this.lastNameClaim = lastNameClaim;
        this.emailClaim = emailClaim;
    }

    public String usernameClaim()
    {
        return usernameClaim;
    }

    public String firstNameClaim()
    {
        return firstNameClaim;
    }

    public String lastNameClaim()
    {
        return lastNameClaim;
    }

    public String emailClaim()
    {
        return emailClaim;
    }
}
