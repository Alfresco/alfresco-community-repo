/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
package org.alfresco.rest.model;

import org.alfresco.utility.model.TestModel;

import java.util.Set;

/**
 * Created by Claudia Agache on 11/11/2016.
 */
public class RestContentModel extends TestModel
{
    private int sizeInBytes;
    private String mimeTypeName;
    private String mimeType;
    private String encoding;
    private Set<String> storageClasses;

    public int getSizeInBytes()
    {
        return sizeInBytes;
    }

    public void setSizeInBytes(int sizeInBytes)
    {
        this.sizeInBytes = sizeInBytes;
    }

    public String getMimeTypeName()
    {
        return mimeTypeName;
    }

    public void setMimeTypeName(String mimeTypeName)
    {
        this.mimeTypeName = mimeTypeName;
    }

    public String getMimeType()
    {
        return mimeType;
    }

    public void setMimeType(String mimeType)
    {
        this.mimeType = mimeType;
    }

    public String getEncoding()
    {
        return encoding;
    }

    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }

    public Set<String> getStorageClasses()
    {
        return storageClasses;
    }

    public void setStorageClasses(Set<String> storageClasses)
    {
        this.storageClasses = storageClasses;
    }
}
