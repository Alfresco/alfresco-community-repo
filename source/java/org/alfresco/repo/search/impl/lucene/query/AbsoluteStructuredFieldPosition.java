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
package org.alfresco.repo.search.impl.lucene.query;

import java.io.IOException;

/**
 * This class patches a term at a specified location.
 * 
 * @author andyh
 */
public class AbsoluteStructuredFieldPosition extends AbstractStructuredFieldPosition
{

    int requiredPosition;

    /**
     * Search for a term at the specified position.
     */

    public AbsoluteStructuredFieldPosition(String termText, int position)
    {
        super(termText, true, true);
        this.requiredPosition = position;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.lucene.extensions.StructuredFieldPosition#matches(int,
     *      org.apache.lucene.index.TermPositions)
     */
    public int matches(int start, int end, int offset) throws IOException
    {
        if (offset >= requiredPosition)
        {
            return -1;
        }

        if (getCachingTermPositions() != null)
        {
            // Doing "termText"
            getCachingTermPositions().reset();
            int count = getCachingTermPositions().freq();
            int realPosition = 0;
            int adjustedPosition = 0;
            for (int i = 0; i < count; i++)
            {
                realPosition = getCachingTermPositions().nextPosition();
                adjustedPosition = realPosition - start;
                if ((end != -1) && (realPosition > end))
                {
                    return -1;
                }
                if (adjustedPosition > requiredPosition)
                {
                    return -1;
                }
                if (adjustedPosition == requiredPosition)
                {
                    return adjustedPosition;
                }

            }
        }
        else
        {
            // Doing "*"
            if ((offset + 1) == requiredPosition)
            {
                return offset + 1;
            }
        }
        return -1;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.lucene.extensions.StructuredFieldPosition#getPosition()
     */
    public int getPosition()
    {
        return requiredPosition;
    }

    public String getDescription()
    {
        return "Absolute Named child";
    }
}