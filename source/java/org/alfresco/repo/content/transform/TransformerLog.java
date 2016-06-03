package org.alfresco.repo.content.transform;

import java.util.Date;
import java.util.Deque;

import org.apache.commons.logging.Log;

import org.alfresco.api.AlfrescoPublicApi;  

/**
 * Implementation of a {@link Log} that logs messages to a structure accessible via
 * {@link TransformerConfigMBean#getTransformationLog(int)}.<p>
 * 
 * @author Alan Davis
 */
@AlfrescoPublicApi
public class TransformerLog extends TransformerLogger<String>
{
    /**
     * {@inheritDoc}<p>
     * Returns 100 as this is currently held in memory.
     */
    @Override
    protected int getUpperMaxEntries()
    {
        return 1000;
    }

    /**
     * Overridden to specify the property name that specifies the maximum number of entries.
     */
    @Override
    protected String getPropertyName()
    {
        return TransformerConfig.LOG_ENTRIES;
    }

    @Override
    protected void addOrModify(Deque<String> entries, Object message)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(TransformerLogger.DATE_FORMAT.format(new Date()));
        sb.append(' ');
        sb.append(message);

        entries.add(sb.toString());
    }
}
