/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.activities.feed;

import java.io.Serializable;
    
/**
 * Job settings passed from grid task to grid job
 */
public class JobSettings implements Serializable
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
