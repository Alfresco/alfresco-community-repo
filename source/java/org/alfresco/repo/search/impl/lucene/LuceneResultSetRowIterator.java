/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
