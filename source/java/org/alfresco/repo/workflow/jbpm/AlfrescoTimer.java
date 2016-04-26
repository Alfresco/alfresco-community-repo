package org.alfresco.repo.workflow.jbpm;

import org.jbpm.JbpmContext;
import org.jbpm.graph.exe.Token;
import org.jbpm.job.Timer;
import org.jbpm.taskmgmt.exe.TaskInstance;

/**
 * Extended JBPM Timer that provides Alfresco context.
 * 
 * NOTE: The action triggered by the timer is executed as the user assigned
 *       to the task associated with the timer.  If not associated with a
 *       task, the timer is executed unauthenticated.
 *       
 * @author davidc
 * @author Nick Smith
 */
public class AlfrescoTimer extends Timer
{
    private static final long serialVersionUID = -6618486175822866286L;

    /**
     * Construct
     */
    public AlfrescoTimer()
    {
        super();
    }

    /**
     * Construct
     * 
     * @param token Token
     */
    public AlfrescoTimer(Token token)
    {
        super(token);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean execute(final JbpmContext jbpmContext)
        throws Exception
    {
        // establish authentication context
        final TaskInstance taskInstance = getTaskInstance();
        
        // execute timer
        boolean deleteTimer = AlfrescoTimer.super.execute(jbpmContext);
        
        // End the task if timer does not repeat.
        // Note the order is a little odd here as the task will be ended
        // after the token has been signalled to move to the next node.
        if (deleteTimer
            && taskInstance != null 
            && taskInstance.isOpen())
        {
            taskInstance.setSignalling(false);
        	String transitionName = getTransitionName();
        	if (transitionName==null)
        	{
        	taskInstance.end();
        	}
        	else
        	{
        	    taskInstance.end(transitionName);
        	}
        }
        
        return deleteTimer;
    }
}
