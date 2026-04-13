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
package org.alfresco.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;


/**
 * Content
 * 
 * @author dcaruana
 */
public interface Content
{
    /**
     * Gets content as a string
     * 
     * @return  content as a string
     * @throws IOException
     */
    public String getContent() throws IOException;

    /**
     * Gets the content mimetype
     * 
     * @return mimetype
     */
    public String getMimetype();
    
    /**
     * Gets the content encoding
     * 
     * @return  encoding
     */
    public String getEncoding();
    
    /**
     * Gets the content length (in bytes)
     * 
     * @return  length
     */
    public long getSize();

    /**
     * Gets the content input stream
     * 
     * @return  input stream
     */
    public InputStream getInputStream();

    /**
     * Gets the content reader (which is sensitive to encoding)
     * 
     * @return Reader
     */
    public Reader getReader() throws IOException;
}
