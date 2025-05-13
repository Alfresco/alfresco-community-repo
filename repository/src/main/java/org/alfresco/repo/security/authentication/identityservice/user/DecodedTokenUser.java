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

public class DecodedTokenUser
{
    private final String username;
    private final String firstName;
    private final String lastName;
    private final String email;

    public DecodedTokenUser(String username, String firstName, String lastName, String email)
    {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    private static final String EMPTY_STRING = "";

    public static DecodedTokenUser validateAndCreate(String username, Object firstName, Object lastName, Object email)
    {
        return new DecodedTokenUser(username, getStringVal(firstName), getStringVal(lastName), getStringVal(email));
    }

    private static String getStringVal(Object firstName)
    {
        return Optional.ofNullable(firstName).filter(String.class::isInstance).map(String.class::cast).orElse(EMPTY_STRING);
    }

    public String username()
    {
        return username;
    }

    public String firstName()
    {
        return firstName;
    }

    public String lastName()
    {
        return lastName;
    }

    public String email()
    {
        return email;
    }
}
