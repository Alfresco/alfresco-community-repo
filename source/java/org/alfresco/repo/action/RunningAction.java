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
