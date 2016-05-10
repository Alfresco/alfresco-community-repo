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

import static org.junit.Assert.assertTrue;

/**
 * Representation of content info (initially for client tests for File Folder API)
 *
 * @author janv
 *
 */
public class ContentInfo
{
    private String mimeType;
    private String mimeTypeName;
    private Long sizeInBytes;
    private String encoding;

    public ContentInfo()
    {
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getMimeTypeName() {
        return mimeTypeName;
    }

    public void setMimeTypeName(String mimeTypeName) {
        this.mimeTypeName = mimeTypeName;
    }

    public Long getSizeInBytes() {
        return sizeInBytes;
    }

    public void setSizeInBytes(Long sizeInBytes) {
        this.sizeInBytes = sizeInBytes;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void expected(Object o)
    {
        assertTrue(o instanceof ContentInfo);

        ContentInfo other = (ContentInfo) o;

        AssertUtil.assertEquals("mimeType", mimeType, other.getMimeType());
        AssertUtil.assertEquals("mimeTypeName", mimeTypeName, other.getMimeTypeName());
        AssertUtil.assertEquals("sizeInBytes", sizeInBytes, other.getSizeInBytes());
        AssertUtil.assertEquals("encoding", encoding, other.getEncoding());
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder(150);
        sb.append("ContentInfo [mimeType='").append(mimeType)
                    .append(", mimeTypeName='").append(mimeTypeName)
                    .append(", sizeInBytes=").append(sizeInBytes)
                    .append(", encoding='").append(encoding)
                    .append(']');
        return sb.toString();
    }
}
