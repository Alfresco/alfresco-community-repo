/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.cmis.search;

import java.util.ListIterator;

/**
 * @author andyh
 */
public class CMISResultSetRowIteratorImpl implements ListIterator<CMISResultSetRow>
{
    /**
     * The result set
     */
    private CMISResultSet resultSet;

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
     */
    public CMISResultSetRowIteratorImpl(CMISResultSet resultSet)
    {
        this.resultSet = resultSet;
        this.max = resultSet.length();
    }

    public CMISResultSet getResultSet()
    {
        return resultSet;
    }

    /*
     * ListIterator implementation
     */
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

    public CMISResultSetRow next()
    {
        return resultSet.getRow(moveToNextPosition());
    }

    protected int moveToNextPosition()
    {
        return ++position;
    }

    public CMISResultSetRow previous()
    {
        return resultSet.getRow(moveToPreviousPosition());
    }

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

    /*
     * Mutation is not supported
     */

    public void remove()
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public void set(CMISResultSetRow o)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public void add(CMISResultSetRow o)
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

}
