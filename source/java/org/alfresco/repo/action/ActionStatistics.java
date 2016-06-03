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
