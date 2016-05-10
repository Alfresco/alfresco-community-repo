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

package org.alfresco.rest.api.tests.client.data;

import java.util.List;

/**
 * Representation of an email request for the quick share link
 *
 * @author Jamal Kaabi-Mofrad
 */
public class QuickShareLinkEmailRequest
{
    private String client;
    private String message;
    private String locale;
    private List<String> recipientEmails;

    public String getClient()
    {
        return client;
    }

    public QuickShareLinkEmailRequest setClient(String client)
    {
        this.client = client;
        return this;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getLocale()
    {
        return locale;
    }

    public void setLocale(String locale)
    {
        this.locale = locale;
    }

    public List<String> getRecipientEmails()
    {
        return recipientEmails;
    }

    public void setRecipientEmails(List<String> recipientEmails)
    {
        this.recipientEmails = recipientEmails;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder(250);
        sb.append("QuickShareLinkEmailRequest [client=").append(client)
                    .append(", message=").append(message)
                    .append(", locale=").append(locale)
                    .append(", recipientEmails=").append(recipientEmails)
                    .append(']');
        return sb.toString();
    }
}
