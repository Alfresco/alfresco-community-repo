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
package org.alfresco.jcr.util;

import java.util.NoSuchElementException;

import javax.jcr.RangeIterator;


/**
 * Alfresco implementation of a Node Iterator
 * 
 * @author David Caruana
 */
public abstract class AbstractRangeIterator implements RangeIterator
{
    private int position = -1;
    
    /**
     * Construct
     * 
     * @param context  session context
     * @param nodes  node list
     */
    public AbstractRangeIterator()
    {
    }

    /**
     * Skip 1 position
     * 
     * @return  current position
     */
    protected long skip()
    {
        skip(1);
        return position;
    }
        
    /* (non-Javadoc)
     * @see javax.jcr.RangeIterator#skip(long)
     */
    public void skip(long skipNum)
    {
        if (skipNum < 0)
        {
            throw new IllegalArgumentException("skipNum must be positive.");
        }
        if (position + skipNum >= getSize())
        {
            throw new NoSuchElementException("Cannot skip " + skipNum + " elements from position " + getPosition() + " as only " + getSize() + " elements are available.");
        }
        position += skipNum;
    }

    /* (non-Javadoc)
     * @see javax.jcr.RangeIterator#getPosition()
     */
    public long getPosition()
    {
        return position + 1;
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext()
    {
        return getPosition() < getSize();
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#remove()
     */
    public void remove()
    {
        throw new UnsupportedOperationException();
    }

}
