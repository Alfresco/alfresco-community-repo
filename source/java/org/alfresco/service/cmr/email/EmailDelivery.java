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
package org.alfresco.service.cmr.email;

import java.io.Serializable;

/**
 * Delivery instructions for an email message.
 * @author mrogers
 *
 */
public class EmailDelivery implements Serializable
{
    private String recipient;
    private String from;
    private String auth;

    /**
     * New Email Delivery Instructions.  Who gets the message and who sent it.
     * Which may be different from the contents of the message.
     * @param recipient String
     * @param from String
     * @param auth - may be null if the email is not authenticated
     */
    public EmailDelivery(String recipient, String from, String auth)
    {
        this.recipient = recipient;
        this.from = from;
        this.auth = auth;
    }

    public String getRecipient()
    {
        return recipient;
    }
    
    public String getFrom()
    {
        return from;
    }
    public String getAuth()
    {
        return auth;
    }

}
