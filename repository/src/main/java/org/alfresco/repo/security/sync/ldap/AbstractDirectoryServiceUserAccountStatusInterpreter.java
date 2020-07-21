/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.repo.security.sync.ldap;

import java.io.Serializable;

public abstract class AbstractDirectoryServiceUserAccountStatusInterpreter
{
    public static final String USER_ACCOUNT_STATUS_NOT_NULL_MESSAGE = "User account status property value must not be null.";

    protected void checkForNullArgument(Serializable arg)
    {
        if (arg == null)
        {
            throw new IllegalArgumentException(USER_ACCOUNT_STATUS_NOT_NULL_MESSAGE);
        }
    }

    /**
     * Check if directory server user account status is disabled.
     * 
     * @param userAccountStatusValue
     *            value to interpret user account status from;
     * 
     * @return true if interpreted as disabled, false otherwise
     */
    public abstract boolean isUserAccountDisabled(Serializable userAccountStatusValue) throws IllegalArgumentException;

    /**
     * Specify if the particular implementation of
     * {@link AbstractDirectoryServiceUserAccountStatusInterpreter#isUserAccountDisabled(Serializable)}
     * will accept null.
     * 
     * @return true if accepts null.
     */
    public abstract boolean acceptsNullArgument();
}
