/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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
package org.alfresco.service.cmr.repository;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import org.alfresco.api.AlfrescoPublicApi;

@AlfrescoPublicApi
public class DirectAccessUrl implements Serializable
{
    private static final long serialVersionUID = -881676208224414139L;

    private String contentUrl;
    private Date expiryTime;
    private boolean attachment;
    private String fileName;

    public String getContentUrl()
    {
        return contentUrl;
    }

    public void setContentUrl(String contentUrl)
    {
        this.contentUrl = contentUrl;
    }

    public Date getExpiryTime()
    {
        return expiryTime;
    }

    public void setExpiryTime(Date expiryTime)
    {
        this.expiryTime = expiryTime;
    }

    public boolean isAttachment()
    {
        return attachment;
    }

    public void setAttachment(boolean attachment)
    {
        this.attachment = attachment;
    }

    public String getFileName()
    {
        return fileName;
    }

    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    @Override public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        DirectAccessUrl that = (DirectAccessUrl) obj;
        return Objects.equals(fileName, that.fileName) && attachment == that.attachment && Objects.equals(contentUrl,
                that.contentUrl) && Objects.equals(expiryTime, that.expiryTime);
    }

    @Override public int hashCode()
    {
        return Objects.hash(contentUrl, expiryTime, attachment, fileName);
    }
}
