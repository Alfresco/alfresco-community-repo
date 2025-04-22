/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.node.integrity;

import java.io.Serializable;
import java.util.List;

/**
 * Represents an integrity violation
 * 
 * @author Derek Hulley
 */
public class IntegrityRecord implements Serializable
{
    private String msg;
    private List<StackTraceElement[]> traces;

    /**
     * @param msg
     *            the violation message
     */
    public IntegrityRecord(String msg)
    {
        this.msg = msg;
        this.traces = null;
    }

    /**
     * Add a stack trace to the list of traces associated with this failure
     * 
     * @param traces
     *            a stack trace
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
