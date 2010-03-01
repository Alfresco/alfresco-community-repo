/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.search.impl.lucene.query;

import java.io.IOException;

/**
 * @author andyh
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class AnyStructuredFieldPosition extends AbstractStructuredFieldPosition
{

    /**
     * 
     */
    public AnyStructuredFieldPosition(String termText)
    {
        super(termText, true, false);
        if (termText == null)
        {
            setTerminal(false);
        }
    }

    public AnyStructuredFieldPosition()
    {
        super(null, false, false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.lucene.extensions.StructuredFieldPosition#matches(int,
     *      int, org.apache.lucene.index.TermPositions)
     */
    public int matches(int start, int end, int offset) throws IOException
    {
        // we are doing //name
        if (getCachingTermPositions() != null)
        {
            setTerminal(true);
            int realPosition = 0;
            int adjustedPosition = 0;
            getCachingTermPositions().reset();
            int count = getCachingTermPositions().freq();
            for (int i = 0; i < count; i++)
            {
                realPosition = getCachingTermPositions().nextPosition();
                adjustedPosition = realPosition - start;
                if ((end != -1) && (realPosition > end))
                {
                    return -1;
                }
                if (adjustedPosition > offset)
                {
                    return adjustedPosition;
                }
            }
        }
        else
        {
            // we are doing //
            setTerminal(false);
            return offset;
        }
        return -1;
    }
    
    public String getDescription()
    {
        return "Any";
    }

}
