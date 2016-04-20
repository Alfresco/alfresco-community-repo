/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.rest.framework.resource.content;

import java.util.Locale;

/**
 * Basic implementation of information about the returned content.
 */
public class ContentInfoImpl implements ContentInfo
{
    private final String mimeType;
    private final String encoding;
    private final long length;
    private final Locale locale;
    
    public ContentInfoImpl(String mimeType, String encoding, long length, Locale locale)
    {
        super();
        this.mimeType = mimeType;
        this.encoding = encoding;
        this.length = length;
        this.locale = locale;
    }
    
    @Override
    public String getMimeType()
    {
        return this.mimeType;
    }
    @Override
    public String getEncoding()
    {
        return this.encoding;
    }
    @Override
    public long getLength()
    {
        return this.length;
    }
    @Override
    public Locale getLocale()
    {
        return this.locale;
    }
}
