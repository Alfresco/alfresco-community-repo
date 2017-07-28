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
package org.alfresco.repo.security.authentication;

import java.io.Serializable;

/**
 * An object to hold information about unsuccessful logins.
 * It is used for brute force attack mitigation in {@link AuthenticationServiceImpl}
 *
 * @since 5.2.0
 * @author amukha
 */
/*package*/ class ProtectedUser implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String userId;
    /** number of consecutive unsuccessful login attempts */
    private long numLogins;
    /** time stamp of last unsuccessful login attempt */
    private long timeStamp;

    /*package*/ ProtectedUser(String userId)
    {
        this.userId = userId;
        this.numLogins = 1;
        this.timeStamp = System.currentTimeMillis();
    }

    /*package*/ ProtectedUser(String userId, long numLogins)
    {
        this.userId = userId;
        this.numLogins = numLogins;
        this.timeStamp = System.currentTimeMillis();
    }

    /*package*/ long getNumLogins()
    {
        return numLogins;
    }

    /*package*/ long getTimeStamp()
    {
        return timeStamp;
    }

    @Override
    public String toString()
    {
        return "ProtectedUser{" +
                "userId='" + userId + '\'' +
                ", numLogins=" + numLogins +
                ", timeStamp=" + timeStamp +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProtectedUser that = (ProtectedUser) o;

        if (numLogins != that.numLogins) return false;
        if (timeStamp != that.timeStamp) return false;
        return userId.equals(that.userId);
    }

    @Override
    public int hashCode()
    {
        int result = userId.hashCode();
        result = 31 * result + (int) (numLogins ^ (numLogins >>> 32));
        result = 31 * result + (int) (timeStamp ^ (timeStamp >>> 32));
        return result;
    }
}
