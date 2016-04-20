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
package org.alfresco.repo.domain.activities;

import java.util.Date;

import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.NodeRef;
  
/**
 * Activity Post DAO
 */
public class ActivityPostEntity
{ 
    public enum STATUS { POSTED, PENDING, PROCESSED, ERROR };
    
    private Long id; // internal DB-generated sequence id
    private String activityData;
    private String activityType;
    private String userId;
    private int jobTaskNode = -1;
    private String siteNetwork;
    private String appTool;
    private String status;
    private Date postDate;
    private Date lastModified; // for debug
    
    // derived
    private String tenantDomain = TenantService.DEFAULT_DOMAIN;
    private NodeRef parentNodeRef;
    
    // for selector
    private long minId = -1;
    private long maxId = -1;
    
    public Long getId()
    {
        return id;
    }
    public void setId(Long id)
    {
        this.id = id;
    }
    
    public String getUserId()
    {
        return userId;
    }
    
    public void setUserId(String userId)
    {
        this.userId = userId;
    }
    
    public int getJobTaskNode()
    {
        return jobTaskNode;
    }
    
    public void setJobTaskNode(int jobTaskNode)
    {
        this.jobTaskNode = jobTaskNode;
    }
 
    public long getMinId()
    {
        return minId;
    }
    
    public void setMinId(long minId)
    {
        this.minId = minId;
    }
    
    public long getMaxId()
    {
        return maxId;
    }
    
    public void setMaxId(long maxId)
    {
        this.maxId = maxId;
    }
    
	public String getSiteNetwork() 
	{
		return siteNetwork;
	}
	
	public void setSiteNetwork(String siteNetwork) 
	{
		this.siteNetwork = siteNetwork;
	}
	
    public String getActivityData()
    {
        return activityData;
    }
    
    public void setActivityData(String activityData)
    {
        this.activityData = activityData;
    }
    
    public String getActivityType()
    {
        return activityType;
    }
    
    public void setActivityType(String activityType)
    {
        this.activityType = activityType;
    }
    
    public Date getPostDate()
    {
        return postDate;
    }
    
    public void setPostDate(Date postDate)
    {
        this.postDate = postDate;
    }
    
    public String getStatus()
    {
        return status;
    }
    
    public void setStatus(String status)
    {
        this.status = status;
    }
    
    public Date getLastModified()
    {
        return lastModified;
    }
    
    public void setLastModified(Date lastModified)
    {
        this.lastModified = lastModified;
    }
    
    public String getAppTool()
    {
        return appTool;
    }
    
    public void setAppTool(String appTool)
    {
        this.appTool = appTool;
    }
    
    public String getTenantDomain()
    {
        return tenantDomain;
    }
    
    public void setTenantDomain(String tenantDomain)
    {
        this.tenantDomain = tenantDomain;
    }
    
    public NodeRef getParentNodeRef()
    {
        return parentNodeRef;
    }
    
    public void setParentNodeRef(NodeRef parentNodeRef)
    {
        this.parentNodeRef = parentNodeRef;
    }
    
    // for debug only
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("ActivityPost\n[");
        sb.append("id=").append(id).append(",");
        sb.append("status=").append(status).append(",");
        sb.append("postDate=").append(postDate).append(",");
        sb.append("userId=").append(userId).append(",");
        sb.append("siteNetwork=").append(siteNetwork).append(",");
        sb.append("appTool=").append(appTool).append(",");
        sb.append("type=").append(activityType).append(",");
        sb.append("jobTaskNode=").append(jobTaskNode).append(",");
        sb.append("data=\n").append(activityData).append("\n]");
        return sb.toString();
    }
}
