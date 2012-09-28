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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.alfresco.service.cmr.action.Action;

/**
 * Responsible for monitoring running actions and accumulating statistics on actions that have been run.
 *
 * @author Alex Miller
 */
public class ActionServiceMonitor
{
    private ConcurrentHashMap<UUID, RunningAction> runningActions = new ConcurrentHashMap<UUID, RunningAction>();
    private ConcurrentHashMap<String, ActionStatistics> actionStatistics = new ConcurrentHashMap<String, ActionStatistics>();
    
    /**
     * Called by the {@link ActionServiceImpl} when an action is started.
     * 
     * Adds the action to the list of currently running actions.
     * 
     * @param action The action being started
     * @return A {@link RunningAction} object used to track the status of the running action.
     */
    public RunningAction actionStarted(Action action)
    {
        RunningAction runningAction = new RunningAction(action);
        
        this.runningActions.put(runningAction.getId(), runningAction);
    
        return runningAction;
    }
    
    /**
     * Called by the {@link ActionServiceImpl} when sn action completes.
     * 
     * Removes the actions from the list of currently running actions, and updated the accumulated statistics for that action.
     * 
     * @param action The {@link RunningAction} object returned by actionStatred.
     */
    public void actionCompleted(RunningAction action)
    {
        runningActions.remove(action.getId());
        updateActionStatisitcis(action);
    }

    private void updateActionStatisitcis(RunningAction action)
    {
        String actionName = action.getActionName();
        ActionStatistics actionStats = actionStatistics.get(actionName);
        if (actionStats == null)
        {
            actionStatistics.putIfAbsent(actionName, new ActionStatistics(actionName));
            actionStats = actionStatistics.get(actionName);
        }
        
        actionStats.addAction(action);        
    }

    /**
     * @return The list of currently running actions.
     */
    public List<RunningAction> getRunningActions()
    {
        return Collections.unmodifiableList(new ArrayList<RunningAction>(runningActions.values()));
    }
    
    /**
     * @return a count of the currently running actions
     */
    public int getRunningActionCount()
    {
        return runningActions.size();
    }

    /**
     * @return a list of the accumulated action statistics.
     */
    public List<ActionStatistics> getActionStatisitcs()
    {
        return Collections.unmodifiableList(new ArrayList<ActionStatistics>(actionStatistics.values()));
    }
}
