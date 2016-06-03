package org.alfresco.repo.activities.feed;

import java.io.Serializable;
    
/**
 * Job settings passed from grid task to grid job
 */
public class JobSettings implements Serializable, Cloneable
{
    public static final long serialVersionUID = -3896042917378679686L;
    
    private int jobTaskNode;
    private long maxSeq;      
    private long minSeq;
    private RepoCtx ctx;
    private int maxItemsPerCycle;
    
    public int getJobTaskNode()
    {
        return jobTaskNode;
    }
    
    public void setJobTaskNode(int jobTaskNode)
    {
        this.jobTaskNode = jobTaskNode;
    }
    
    public long getMaxSeq()
    {
        return maxSeq;
    }

    public void setMaxSeq(long maxSeq)
    {
        this.maxSeq = maxSeq;
    }
    
    public long getMinSeq()
    {
        return minSeq;
    }

    public void setMinSeq(long minSeq)
    {
        this.minSeq = minSeq;
    }

	public RepoCtx getWebScriptsCtx() {
		return ctx;
	}
	
	public void setWebScriptsCtx(RepoCtx ctx) {
		this.ctx = ctx;
	}

    public int getMaxItemsPerCycle()
    {
        return maxItemsPerCycle;
    }

    public void setMaxItemsPerCycle(int maxItemsPerCycle)
    {
        this.maxItemsPerCycle = maxItemsPerCycle;
    }
    
    @Override
    public JobSettings clone()
    {
        JobSettings js = new JobSettings();
        js.setMaxItemsPerCycle(this.maxItemsPerCycle);
        js.setMaxSeq(this.maxSeq);
        js.setMinSeq(this.minSeq);
        js.setJobTaskNode(this.jobTaskNode);
        js.setWebScriptsCtx(this.ctx); // note: shallow copy
        return js;
    }
}
