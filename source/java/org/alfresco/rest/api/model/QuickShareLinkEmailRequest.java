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

import java.util.List;

/**
 * Representation of an email request for the quick share link
 *
 * @author Jamal Kaabi-Mofrad
 */
public class QuickShareLinkEmailRequest extends Target
{
    private String templateId;
    private String sharedNodeUrl;
    private String message;
    private String locale;
    private List<String> recipientEmails;
    private Boolean isSendFromDefaultEmail;
    private Boolean isIgnoreSendFailure;

    public String getTemplateId()
    {
        return templateId;
    }

    public void setTemplateId(String templateId)
    {
        this.templateId = templateId;
    }

    public String getSharedNodeUrl()
    {
        return sharedNodeUrl;
    }

    public void setSharedNodeUrl(String sharedNodeUrl)
    {
        this.sharedNodeUrl = sharedNodeUrl;
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

    public Boolean getIsSendFromDefaultEmail()
    {
        return isSendFromDefaultEmail;
    }

    public void setIsSendFromDefaultEmail(Boolean isSendFromDefaultEmail)
    {
        this.isSendFromDefaultEmail = isSendFromDefaultEmail;
    }

    public Boolean getIsIgnoreSendFailure()
    {
        return isIgnoreSendFailure;
    }

    public void setIsIgnoreSendFailure(Boolean isIgnoreSendFailure)
    {
        this.isIgnoreSendFailure = isIgnoreSendFailure;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder(250);
        sb.append("QuickShareLinkEmailRequest [templateId='").append(templateId)
                    .append(", sharedNodeUrl='").append(sharedNodeUrl)
                    .append(", message='").append(message)
                    .append(", locale='").append(locale)
                    .append(", recipientEmails=").append(recipientEmails)
                    .append(", isSendFromDefaultEmail").append(isSendFromDefaultEmail)
                    .append(", isIgnoreSendFailure=").append(isIgnoreSendFailure)
                    .append(']');
        return sb.toString();
    }
}
