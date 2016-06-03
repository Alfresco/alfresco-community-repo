package org.alfresco.repo.content.transform;

import java.util.Date;
import java.util.Deque;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;

/**
 * Implementation of a {@link Log} that logs messages to a structure accessible via
 * {@link TransformerConfigMBean#getTransformationDebugLog(int)}.<p>
 * 
 * @author Alan Davis
 */
public class TransformerDebugLog extends TransformerLogger<DebugEntry>
{
    private static Pattern END_OF_REQUEST_ID_PATTERN = Pattern.compile("[^0-9]");

    /**
     * {@inheritDoc}<p>
     * Returns 20 as this is debug and we must currently walk the whole structure each time
     * an new request id is added.
     */
    @Override
    protected int getUpperMaxEntries()
    {
        return 100;
    }

    /**
     * Overridden to specify the property name that specifies the maximum number of entries.
     */
    @Override
    protected String getPropertyName()
    {
        return TransformerConfig.DEBUG_ENTRIES;
    }

    @Override
    protected void addOrModify(Deque<DebugEntry> entries, Object message)
    {
        String msg = (String)message;
        String requestId = getRequestId(msg);
        if (requestId != null)
        {
            Iterator<DebugEntry> iterator = entries.descendingIterator();
            while (iterator.hasNext())
            {
                DebugEntry entry = iterator.next();
                if (requestId.equals(entry.requestId))
                {
                    entry.addLine(msg);
                    return;
                }
            }
            entries.add(new DebugEntry(requestId, msg));
        }
    }

    /**
     * Returns the request id from the debug message. This is the integer at
     * the start of the message.
     */
    private String getRequestId(String message)
    {
        String requestId = null;
        if (message != null)
        {
            Matcher matcher = END_OF_REQUEST_ID_PATTERN.matcher(message);
            if (matcher.find())
            {
                int i = matcher.start();
                requestId = message.substring(0, i);
            }
        }
        return requestId;
    }
}

// Collects multiple lines of debug for the same transformer request.
class DebugEntry
{
    final String requestId;
    private final StringBuilder sb = new StringBuilder();
    boolean complete = false;
    
    DebugEntry(String requestId, String message)
    {
        this.requestId = requestId;
        sb.append(requestId);
        sb.append("             ");
        sb.append(TransformerLogger.DATE_FORMAT.format(new Date()));
        
        addLine(message);
    }
    
    void addLine(String message)
    {
        sb.append('\n');
        sb.append(message);
        complete = message.contains("Finished in"); 
    }
    
    public String toString()
    {
        String string;
        if (complete)
        {
            string = sb.toString();
        }
        else
        {
            int length = sb.length();
            sb.append("\n             <<-- INCOMPLETE -->>");
            string = sb.toString();
            sb.setLength(length);
        }
        return string;
    }
}
