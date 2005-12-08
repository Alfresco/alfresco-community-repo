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
package org.alfresco.repo.search.impl.lucene.query;

import java.io.IOException;

/**
 * Search for a term relative to the last one found.
 * 
 * @author andyh
 */
public class RelativeStructuredFieldPosition extends AbstractStructuredFieldPosition
{

    int relativePosition;

    /**
     * 
     */
    public RelativeStructuredFieldPosition(String termText)
    {
        super(termText.equals("*") ? null : termText, true, false);
        relativePosition = 1;
        
    }

    public RelativeStructuredFieldPosition()
    {
        super(null, false, false);
        relativePosition = 1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.lucene.extensions.StructuredFieldPosition#matches(int,
     *      int, org.apache.lucene.index.TermPositions)
     */
    public int matches(int start, int end, int offset) throws IOException
    {

        if (getCachingTermPositions() != null)
        {
            // Doing "termText"
            getCachingTermPositions().reset();
            int count = getCachingTermPositions().freq();
            int requiredPosition = offset + relativePosition;
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
                if (adjustedPosition == requiredPosition)
                {
                    return adjustedPosition;
                }
                if (adjustedPosition > requiredPosition)
                {
                    return -1;
                }
            }
        }
        else
        {
            // Doing "*";
            return offset + 1;
        }
        return -1;
    }
    
    public String getDescription()
    {
        return "Relative Named child";
    }
}
