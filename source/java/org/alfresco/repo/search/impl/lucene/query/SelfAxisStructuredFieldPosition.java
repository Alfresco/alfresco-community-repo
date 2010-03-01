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
package org.alfresco.repo.search.impl.lucene.query;

import java.io.IOException;

public class SelfAxisStructuredFieldPosition extends AbstractStructuredFieldPosition
{

    public SelfAxisStructuredFieldPosition()
    {
        super(null, true, false);
    }

    public int matches(int start, int end, int offset) throws IOException
    {
        return offset;
    }

    public String getDescription()
    {
        return "Self Axis";
    }

    public boolean linkSelf()
    {
        return true;
    }

    public boolean isTerminal()
    {
        return false;
    }

   
    
    
}
