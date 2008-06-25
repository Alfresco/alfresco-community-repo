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
package org.alfresco.repo.activities.feed.control;

import java.util.Date;

import org.alfresco.service.cmr.activities.FeedControl;

/**
 * Activity Feed Control DAO
 */
public class FeedControlDAO
{ 
    private long id; // internal DB-generated id
    private String feedUserId;
    private String siteNetwork;
    private String appTool;
    
    private Date lastModified; // when inserted
    
    // TODO - review - deleted feed controls are not kept and available feed controls are currently retrieved during generation, hence
    // it is possible for a feed control to be applied even if lastModified is greater than postDate - could check the date !
    // it is also possible for a feed control to not be applied if it is deleted just after the post - would need to keep, at least until next generation
    
    public FeedControlDAO()
    {
    }
    
    public FeedControlDAO(String feedUserId)
    {
        this.feedUserId = feedUserId;
    }
    
    public FeedControlDAO(String feedUserId, FeedControl feedControl)
    {
        this.feedUserId = feedUserId;
        this.siteNetwork = feedControl.getSiteId();
        this.appTool = feedControl.getAppToolId();
        this.lastModified = new Date();
    }
    
    public FeedControl getFeedControl()
    {
        return new FeedControl(this.siteNetwork, this.appTool);
    }
    
    public long getId()
    {
        return id;
    }
    
    public void setId(long id)
    {
        this.id = id;
    }

    public String getSiteNetwork()
    {
        return siteNetwork;
    }

    public void setSiteNetwork(String siteNetwork)
    {
        this.siteNetwork = siteNetwork;
    }

    public String getAppTool()
    {
        return appTool;
    }

    public void setAppTool(String appTool)
    {
        this.appTool = appTool;
    }

    public String getFeedUserId()
    {
        return feedUserId;
    }

    public void setFeedUserId(String feedUserId)
    {
        this.feedUserId = feedUserId;
    }

    public Date getLastModified()
    {
        return lastModified;
    }

    public void setLastModified(Date lastModified)
    {
        this.lastModified = lastModified;
    }
}
