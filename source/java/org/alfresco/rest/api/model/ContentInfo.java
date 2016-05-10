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
 * Representation of content info
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

	public ContentInfo(String mimeType, String mimeTypeName, Long sizeInBytes, String encoding)
	{
		this.mimeType = mimeType;
		this.mimeTypeName = mimeTypeName;
		this.sizeInBytes = sizeInBytes;
		this.encoding = encoding;
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

    public Long getSizeInBytes() {
        return sizeInBytes;
    }

    public String getEncoding() {
        return encoding;
    }

    @Override
	public String toString()
	{
		return "ContentInfo [mimeType=" + mimeType + ", mimeTypeName=" + mimeTypeName
				+ ", encoding=" + encoding + ", sizeInBytes=" + sizeInBytes + "]";
	}
}
