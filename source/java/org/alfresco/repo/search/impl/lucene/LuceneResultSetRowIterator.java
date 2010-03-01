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
package org.alfresco.repo.search.impl.lucene;

import org.alfresco.repo.search.AbstractResultSetRowIterator;
import org.alfresco.service.cmr.search.ResultSetRow;

/**
 * Iterate over the rows in a LuceneResultSet
 * 
 * @author andyh
 * 
 */
public class LuceneResultSetRowIterator extends AbstractResultSetRowIterator
{
    /**
     * Create an iterator over the result set. Follows standard ListIterator
     * conventions
     * 
     * @param resultSet
     */
    public LuceneResultSetRowIterator(LuceneResultSet resultSet)
    {
        super(resultSet);
    }

    public ResultSetRow next()
    {
        return new LuceneResultSetRow((LuceneResultSet)getResultSet(), moveToNextPosition());
    }

    public ResultSetRow previous()
    {
        return new LuceneResultSetRow((LuceneResultSet)getResultSet(), moveToPreviousPosition());
    }
}
