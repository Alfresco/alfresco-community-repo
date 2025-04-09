/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.search;

import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;

/**
 * Iterate over the rows in a ResultSet
 * 
 * @author andyh
 * 
 */
public abstract class AbstractResultSetRowIterator implements ResultSetRowIterator
{
    /**
     * The result set
     */
    private ResultSet resultSet;

    /**
     * The current position
     */
    private int position = -1;

    /**
     * The maximum position
     */
    private int max;

    /**
     * Create an iterator over the result set. Follows stadard ListIterator conventions
     * 
     * @param resultSet
     *            ResultSet
     */
    public AbstractResultSetRowIterator(ResultSet resultSet)
    {
        super();
        this.resultSet = resultSet;
        this.max = resultSet.length();
    }

    public ResultSet getResultSet()
    {
        return resultSet;
    }

    /* ListIterator implementation */
    public boolean hasNext()
    {
        return position < (max - 1);
    }

    public boolean allowsReverse()
    {
        return true;
    }

    public boolean hasPrevious()
    {
        return position > 0;
    }

    abstract public ResultSetRow next();

    protected int moveToNextPosition()
    {
        return ++position;
    }

    abstract public ResultSetRow previous();

    protected int moveToPreviousPosition()
    {
        return --position;
    }

    public int nextIndex()
    {
        return position + 1;
    }

    public int previousIndex()
    {
        return position - 1;
    }

    /* Mutation is not supported */

    public void remove()
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public void set(ResultSetRow o)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public void add(ResultSetRow o)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

}
