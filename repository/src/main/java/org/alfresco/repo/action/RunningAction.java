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
package org.alfresco.repo.action;

import java.util.Date;
import java.util.UUID;

import org.alfresco.service.cmr.action.Action;

/**
 * Responsible for tracking the invocation of an action.
 *
 * @author Alex Miller
 */
public class RunningAction
{
    private UUID id = UUID.randomUUID();
    
    private String name;
    private Thread thread;

    private Date started;

    private boolean exceptionThrown = false;

    /**
     * @param action The action being run
     */
    public RunningAction(Action action)
    {
        this.name = action.getActionDefinitionName();
        this.started = new Date(); 
        this.thread = Thread.currentThread();
    }


    /**
     * @return The name of the action this object is tracking
     */
    public String getActionName()
    {
        return name;
    }
    
    /**
     * @return The name of thread the action is being run on
     */
    public String getThread()
    {
        return thread.toString();
    }


    /**
     * @return The generated id for the action invocation
     */
    public UUID getId()
    {
        return id;
    }
    
    /**
     * @return The time since the action was started
     */
    public long getElapsedTime()
    {
        return System.currentTimeMillis() - started.getTime();
    }


    /**
     * Called by the {@link ActionServiceImpl} if the action generates an exception during invocation.
     */
    public void setException(Throwable e)
    {
        this.exceptionThrown  = true;
    }


    /**
     * @return true, if setException was called
     */
    public boolean hasError()
    {
        return exceptionThrown;
    }
}
