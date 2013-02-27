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
package org.alfresco.repo.content;

import java.nio.channels.ReadableByteChannel;

import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.springframework.dao.ConcurrencyFailureException;

/**
 * A blank reader for which <code>exists()</code> always returns false.
 * 
 * @author Derek Hulley
 */
public class EmptyContentReader extends AbstractContentReader
{
    /**
     * @param contentUrl    the content URL
     */
    public EmptyContentReader(String contentUrl)
    {
        super(contentUrl);
    }
    
    /**
     * @return  Returns an instance of the this class
     */
    @Override
    protected ContentReader createReader() throws ContentIOException
    {
        return new EmptyContentReader(this.getContentUrl());
    }

    @Override
    protected ReadableByteChannel getDirectReadableChannel() throws ContentIOException
    {
        // ALF-17708: If we got the contentdata from the transactional cache, there's a chance that eager cleaning can
        // remove the content from under our feet
        throw new ConcurrencyFailureException(getContentUrl() + " no longer exists");
    }

    public boolean exists()
    {
        return false;
    }

    public long getLastModified()
    {
        return 0L;
    }

    public long getSize()
    {
        return 0L;
    }
}
