/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.action;

/**
 * Responsible for accumulating and providing statistics on the invocations of a particualr action.
 *
 * @author Alex Miller
 */
public class ActionStatistics
{
    private String actionName;
    
    long invocationCount = 0;
    long errorCount = 0;
    long totalTime = 0;
    
    /**
     * @param actionName The name of the action this object will provide statistics for.
     */
    public ActionStatistics(String actionName)
    {
        this.actionName = actionName;
    }

    /**
     * Accumulate statistics from action.
     */
    public synchronized void addAction(RunningAction action)
    {
        invocationCount = invocationCount + 1;
        if (action.hasError() == true)
        {
            errorCount = errorCount +1;
        }
        totalTime = totalTime + action.getElapsedTime();
    }
    
    /**
     * @return The name of the actions this object has statistics for
     */
    public String getActionName() 
    {
        return actionName;
    }
    
    /**
     * @return The number of times the action has been invoked 
     */
    public long getInvocationCount()
    {
        return invocationCount;
    }
    
    /**
     * @return The number of time the invocation of this action has resulted in an exception
     */
    public long getErrorCount()
    {
        return errorCount;
    }
    
    /**
     * @return The average time for the invocation of this action
     */
    public long getAverageTime()
    {
        return totalTime / invocationCount;
    }

}
