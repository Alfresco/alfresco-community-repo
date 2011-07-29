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
package org.alfresco.filesys.repo.rules.operations;

import org.alfresco.filesys.repo.rules.Operation;

public class MoveFileOperation implements Operation
{ 
    private String from;
    private String to;
    
    public MoveFileOperation(String from, String to)
    {
        this.from = from;
        this.to = to;
    }

    
    public String getFrom()
    {
        return from;
    }
    
    public String getTo()
    {
        return to;
    }
    
    public String toString()
    {
        return "MoveFileOperation: from " + from + " to "+ to;
    }
    
    public int hashCode()
    {
        return from.hashCode();
    }
    
    public boolean equals(Object o)
    {
        if(o instanceof MoveFileOperation)
        {
            MoveFileOperation r = (MoveFileOperation)o;
            if(from.equals(r.getFrom()) && to.equals(r.getTo()))
            {
                return true;
            }
        }
        return false;
    }



}
