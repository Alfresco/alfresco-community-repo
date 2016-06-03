package org.alfresco.repo.node.integrity;

import java.util.List;
import java.io.Serializable;

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
     * @param traces a stack trace
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
