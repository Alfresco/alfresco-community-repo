/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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

package org.alfresco.rest.api.model;

/**
 * Representation of a password reset.
 *
 * @author Jamal Kaabi-Mofrad
 * @since 5.2.1
 */
public class PasswordReset
{
    /** new password */
    private String password;
    /** workflow Id */
    private String id;
    /** workflow Key */
    private String key;

    public PasswordReset()
    {
    }

    public String getPassword()
    {
        return password;
    }

    public PasswordReset setPassword(String password)
    {
        this.password = password;
        return this;
    }

    public String getId()
    {
        return id;
    }

    public PasswordReset setId(String id)
    {
        this.id = id;
        return this;
    }

    public String getKey()
    {
        return key;
    }

    public PasswordReset setKey(String key)
    {
        this.key = key;
        return this;
    }

    @Override
    public String toString()
    {
        // we don't return the password for the obvious reason
        final StringBuilder sb = new StringBuilder(100);
        sb.append("PasswordReset [id=").append(id)
                    .append(", key=").append(key)
                    .append(']');
        return sb.toString();
    }
}
