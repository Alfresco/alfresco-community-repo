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
package org.alfresco.repo.node.integrity;

import java.util.List;

/**
 * Represents an integrity violation
 * 
 * @author Derek Hulley
 */
public class IntegrityRecord
{
    private String msg;
    private List<StackTraceElement[]> traces;
    
    /**
     * @param msg the violation message
     */
    public IntegrityRecord(String msg)
    {
        this.msg = msg;
        this.traces = null;
    }
    
    /**
     * Add a stack trace to the list of traces associated with this failure
     * 
     * @param trace a stack trace
     */
    public void setTraces(List<StackTraceElement[]> traces)
    {
        this.traces = traces;
    }
    
    public String getMessage()
    {
        return msg;
    }
    
    /**
     * Dumps the integrity message and, if present, the stack trace
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder(msg.length() * 2);
        if (traces == null)
        {
            sb.append(msg);
        }
        else
        {
            sb.append(msg);
            for (StackTraceElement[] trace : traces)
            {
                sb.append("\n   Trace of possible cause:");
                for (int i = 0; i < trace.length; i++)
                {
                    sb.append("\n      ").append(trace[i]);
                }
            }
        }
        return sb.toString();
    }
}
