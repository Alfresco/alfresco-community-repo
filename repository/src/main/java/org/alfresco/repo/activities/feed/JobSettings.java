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
