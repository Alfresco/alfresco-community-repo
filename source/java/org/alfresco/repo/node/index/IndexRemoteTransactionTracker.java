package org.alfresco.repo.node.index;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Component to check and recover the indexes.
 * 
 * @deprecated    Deprecated as of 1.4.5.  Use {@linkplain IndexTransactionTracker}
 * 
 * @author Derek Hulley
 */
public class IndexRemoteTransactionTracker extends AbstractReindexComponent
{
    private static Log logger = LogFactory.getLog(IndexRemoteTransactionTracker.class);
    
    /**
     * Dumps an error message.
     */
    public IndexRemoteTransactionTracker()
    {
        logger.warn(
                "The component 'org.alfresco.repo.node.index.IndexRemoteTransactionTracker' " +
                "has been replaced by 'org.alfresco.repo.node.index.IndexTransactionTracker' \n" +
                "See the extension sample file 'index-tracking-context.xml.sample'. \n" +
                "See http://wiki.alfresco.com/wiki/High_Availability_Configuration_V1.4_to_V2.1#Lucene_Index_Synchronization.");
    }

    /**
     * As of release 1.4.5, 2.0.5 and 2.1.1, this property is no longer is use.
     */
    public void setRemoteOnly(boolean remoteOnly)
    {
    }

    @Override
    protected void reindexImpl()
    {
    }
}