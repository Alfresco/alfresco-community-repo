package org.alfresco.repo.content.metadata;

import org.alfresco.api.AlfrescoPublicApi;    

/**
 * Represents maximum values (that result in exceptions if exceeded) or
 * limits on values (that result in EOF (End Of File) being returned
 * early). The only current option is for elapsed time.
 * 
 * @author Ray Gauss II
 */
@AlfrescoPublicApi
public class MetadataExtracterLimits
{
    private long timeoutMs = -1;
    
    /**
     * Gets the time in milliseconds after which the metadata extracter will be stopped.
     * 
     * @return the timeout
     */
    public long getTimeoutMs()
    {
        return timeoutMs;
    }

    /**
     * Sets the time in milliseconds after which the metadata extracter will be stopped.
     * 
     * @param timeoutMs the timeout
     */
    public void setTimeoutMs(long timeoutMs)
    {
        this.timeoutMs = timeoutMs;
    }

}
