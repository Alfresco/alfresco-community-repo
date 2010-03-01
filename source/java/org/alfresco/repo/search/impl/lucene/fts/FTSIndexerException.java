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
package org.alfresco.repo.search.impl.lucene.fts;

/**
 * FTS indexer exception
 * 
 * @author andyh
 *
 */
public class FTSIndexerException extends RuntimeException
{

    /**
     * 
     */
    private static final long serialVersionUID = 3258134635127912754L;

    /**
     * 
     */
    public FTSIndexerException()
    {
        super();
    }

    /**
     * @param message
     */
    public FTSIndexerException(String message)
    {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public FTSIndexerException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * @param cause
     */
    public FTSIndexerException(Throwable cause)
    {
        super(cause);
    }

}
