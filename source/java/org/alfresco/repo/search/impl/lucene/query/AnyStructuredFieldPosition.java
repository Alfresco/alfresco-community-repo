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
