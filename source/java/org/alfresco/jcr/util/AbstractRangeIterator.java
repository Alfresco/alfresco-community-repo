/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
