/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.activities.feed;

import org.alfresco.repo.activities.ActivityPostServiceImpl;
import org.alfresco.repo.domain.activities.ActivityPostDAO;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.springframework.extensions.surf.util.PropertyCheck;
import org.alfresco.util.VmShutdownListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionException;

/**
 * Implementations of the abstract feed generator component are responsible for generating activity feed entries
 */
public abstract class AbstractFeedGenerator implements FeedGenerator
{
    private static Log logger = LogFactory.getLog(AbstractFeedGenerator.class);
    
    private static VmShutdownListener vmShutdownListener = new VmShutdownListener(AbstractFeedGenerator.class.getName());
    
    private int maxItemsPerCycle = 100;
    
    private ActivityPostDAO postDAO;
    private ActivityPostServiceImpl activityPostServiceImpl;
    private AuthenticationService authenticationService;
    
    private String repoEndPoint; // http://hostname:port/webapp (eg. http://localhost:8080/alfresco)
    
    private boolean userNamesAreCaseSensitive = false;

    private RepoCtx ctx = null;
    
    private volatile boolean busy;
    
    public void setActivityPostServiceImpl(ActivityPostServiceImpl activityPostServiceImpl)
    {
        this.activityPostServiceImpl = activityPostServiceImpl;
    }
    
    public void setPostDAO(ActivityPostDAO postDAO)
    {
        this.postDAO = postDAO;
    }
    
    public void setAuthenticationService(AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }
    
    public void setRepoEndPoint(String repoEndPoint)
    {
        this.repoEndPoint = repoEndPoint;
    }
    
    public void setUserNamesAreCaseSensitive(boolean userNamesAreCaseSensitive)
    {
        this.userNamesAreCaseSensitive = userNamesAreCaseSensitive;
    }
    
    public void setMaxItemsPerCycle(int maxItemsPerCycle)
    {
        this.maxItemsPerCycle = maxItemsPerCycle;
    }
    
    public int getMaxItemsPerCycle()
    {
        return this.maxItemsPerCycle;
    }

    public ActivityPostDAO getPostDaoService()
    {
        return this.postDAO;
    }
    
    public AuthenticationService getAuthenticationService()
    {
        return this.authenticationService;
    }
   
    public RepoCtx getWebScriptsCtx()
    {
        return this.ctx;
    }
     
    public void init() throws Exception
    {
        ctx = new RepoCtx(repoEndPoint);
        ctx.setUserNamesAreCaseSensitive(userNamesAreCaseSensitive);
        
        busy = false;
    }
    
    /**
     * Perform basic checks to ensure that the necessary dependencies were injected.
     */
    private void checkProperties()
    {
        PropertyCheck.mandatory(this, "postDAO", postDAO);
        
        activityPostServiceImpl.setEstimatedGridSize(getEstimatedGridSize());
    }
     
    abstract public int getEstimatedGridSize();
    
    public void execute() throws JobExecutionException
    {
        if (busy)
        {
            logger.warn("Still busy ...");
            return;
        }
        
        busy = true;
        try
        {
            checkProperties();

            // run one job cycle
            generate();
        }
        catch (Throwable e)
        {
            // If the VM is shutting down, then ignore
            if (vmShutdownListener.isVmShuttingDown())
            {
                // Ignore
            }
            else
            {
                logger.error("Exception during generation of feeds", e);
            }
        }
        finally
        {
            busy = false;
        }
    }

    protected abstract boolean generate() throws Exception;
}
