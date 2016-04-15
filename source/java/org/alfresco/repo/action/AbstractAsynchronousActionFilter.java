package org.alfresco.repo.action;

import java.util.Comparator;

/**
 * This class is the base filter class for asynchronous actions. These filters are used in identifying
 * 'equivalent' actions in the asynchronous action execution service. By registering
 * a subclass of this type, all actions of a given action-definition-name that are still pending
 * (i.e. currently executing or in the queue awaiting execution) will be compared to any new action
 * and if they are equal (as determined by the compare implementation defined herein) the newly
 * submitted action will not be added to the queue and will be dropped.
 * 
 * Concrete subclasses can be implemented and then dependency-injected using the spring-bean
 * baseActionFilter as their parent.
 * 
 * @author Neil McErlean
 */
public abstract class AbstractAsynchronousActionFilter implements Comparator<OngoingAsyncAction>
{
	private String name;
    private String actionDefinitionName;
    private AsynchronousActionExecutionQueueImpl asynchronousActionExecutionQueue;

    /**
     * Gets the name of this comparator.
     * @return String
     */
	public String getName()
	{
		return this.name;
	}

	/**
	 * Sets the name of this comparator.
	 * @param name String
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Gets the action definition name against which this comparator is registered.
	 * @return String
	 */
	public String getActionDefinitionName()
	{
		return this.actionDefinitionName;
	}

	public void setActionDefinitionName(String actionDefinitionName)
	{
		this.actionDefinitionName = actionDefinitionName;
	}

	public void setAsynchronousActionExecutionQueue(
			AsynchronousActionExecutionQueueImpl asynchronousActionExecutionQueue)
	{
		this.asynchronousActionExecutionQueue = asynchronousActionExecutionQueue;
	}
	
	public void init()
	{
		this.asynchronousActionExecutionQueue.registerActionFilter(this);
	}
}
