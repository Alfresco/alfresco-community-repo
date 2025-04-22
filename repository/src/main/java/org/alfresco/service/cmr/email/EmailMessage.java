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
import java.util.Date;
import java.util.List;

/**
 * Interface to process email messages.
 * 
 * @author maxim
 * @since 2.2
 */
public interface EmailMessage extends Serializable
{
    /**
     * @return FROM address.
     */
    public String getFrom();

    /**
     * @return TO address.
     */
    public String getTo();

    /**
     * @return CC addresses.
     */
    public List<String> getCC();

    /**
     * @return sent date.
     */
    public Date getSentDate();

    /**
     * Get the subject of the message
     * 
     * @return subject of the message or null if there is no subject.
     */
    public String getSubject();

    /**
     * @return part of the mail body.
     */
    public EmailMessagePart getBody();

    /**
     * @return parts of the mail attachments.
     */
    public EmailMessagePart[] getAttachments();

}
