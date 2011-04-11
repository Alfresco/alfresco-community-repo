/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.email.server.impl;

import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.email.EmailMessage;
import org.alfresco.service.cmr.email.EmailMessagePart;

/**
 * Implementation EmailMessage interface. 
 * 
 * @deprecated - class not used.  
 * @author maxim
 * @since 2.2
 */
public class EmailMessageImpl implements EmailMessage
{
    private static final long serialVersionUID = 8215537693963343756L;

    private String to;
    private String from;
    private String subject;
    private Date sentDate;
    private EmailMessagePart body;
    

    public EmailMessageImpl(String to, String from, String subject, String body)
    {
        if (to == null)
        {
            throw new IllegalArgumentException("To cannot be null");
        }
        this.to = to;
        if (from == null)
        {
            throw new IllegalArgumentException("From cannot be null");
        }
        this.from = from;
        if (subject == null)
        {
            throw new IllegalArgumentException("Subject cannot be null");
        }
        this.subject = subject;
        if (body == null)
        {
            throw new IllegalArgumentException("Body cannot be null");
        }
        this.body = new EmailMessagePartImpl("Content.txt", body.getBytes());

        this.sentDate = new Date();
    }

    public String getTo() 
    {
        return to;
    }

    public String getFrom() 
    {
        return from;
    }

    public String getSubject() 
    {
        return subject;
    }
    
    public List<String> getCC() 
    {
        return null;
    }

    public Date getSentDate() 
    {
        return sentDate;
    }

    public EmailMessagePart getBody() 
    {
        return body;
    }
    
    public EmailMessagePart[] getAttachments() 
    {
        return new EmailMessagePart[0];
    }

}
