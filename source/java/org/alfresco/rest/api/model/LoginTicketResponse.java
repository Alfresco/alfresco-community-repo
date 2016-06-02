/*
 * Copyright (C) 2005-2016 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */

package org.alfresco.rest.api.model;

/**
 * @author Jamal Kaabi-Mofrad
 */
public class LoginTicketResponse extends LoginTicket
{

    public LoginTicketResponse()
    {
        this.password = null;
    }

    @Override
    public String getPassword()
    {
        return null;
    }

    @Override
    public void setPassword(String password)
    {
        // intentionally empty
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder(150);
        sb.append("LoginTicketResponse [userId=").append(userId)
                    .append(", password=").append(password)
                    .append(", id=").append(id)
                    .append(']');
        return sb.toString();
    }
}
