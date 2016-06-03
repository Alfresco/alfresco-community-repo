package org.alfresco.repo.activities.feed.local;

import java.io.Serializable;

import org.alfresco.repo.activities.feed.FeedGridJob;
import org.alfresco.repo.activities.feed.FeedTaskProcessor;
import org.alfresco.repo.activities.feed.JobSettings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation to execute local (ie. not grid) feed job
 */
public class LocalFeedGridJob implements FeedGridJob
{
    private static final Log logger = LogFactory.getLog(LocalFeedGridJob.class);
    
    private JobSettings arg;
    
    private FeedTaskProcessor feedTaskProcessor;
    
    public void setFeedTaskProcessor(FeedTaskProcessor feedTaskProcessor)
    {
        this.feedTaskProcessor = feedTaskProcessor;
    }
    
    public Serializable execute() throws Exception
    {
        JobSettings js = getArgument();
        
        if (logger.isDebugEnabled()) { logger.debug(">>> Execute: nodehash '" + js.getJobTaskNode() + "' from seq '" + js.getMinSeq() + "' to seq '" + js.getMaxSeq() + "' on this node"); }
        
        feedTaskProcessor.process(js.getJobTaskNode(), js.getMinSeq(), js.getMaxSeq(), js.getWebScriptsCtx());
        
        // This job does not return any result.
        return null;
    }
    
    public void setArgument(JobSettings arg)
    {
        this.arg = arg;
    }
    
    public JobSettings getArgument()
    {
        return this.arg;
    }
}
