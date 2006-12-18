/**
 * 
 */
package org.alfresco.repo.policy;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.util.ParameterCheck;

/**
 * Base behaviour implementation
 * 
 * @author Roy Wetherall
 */
public abstract class BaseBehaviour implements Behaviour 
{
	/** The notification frequency */
	protected NotificationFrequency frequency = NotificationFrequency.EVERY_EVENT;
	
	/** Disabled stack **/
	private StackThreadLocal disabled = new StackThreadLocal();
	
	/** Proxies **/
	protected Map<Class, Object> proxies = new HashMap<Class, Object>();

	/**
	 * Default constructor
	 */
	public BaseBehaviour()
	{
		// Default constructor
	}
	
	/**
	 * Constructor
	 * 
	 * @param frequency		the notification frequency
	 */
	public BaseBehaviour(NotificationFrequency frequency)
	{
		ParameterCheck.mandatory("Frequency", frequency);
		this.frequency = frequency;
	}
	
	public void setNotificationFrequency(NotificationFrequency frequency)
	{
		this.frequency = frequency;
	}

	/**
	 * Disable this behaviour for the curent thread
	 */
	public void disable() 
	{
	    Stack<Integer> stack = disabled.get();
	    stack.push(hashCode());
	}

	/**
	 * Enable this behaviour for the current thread
	 */
	public void enable() 
	{
	    Stack<Integer> stack = disabled.get();
	    if (stack.peek().equals(hashCode()) == false)
	    {
	        throw new PolicyException("Cannot enable " + this.toString() + " at this time - mismatched with disable calls");
	    }
	    stack.pop();
	}

	/**
	 * Indicates whether the this behaviour is current enabled or not
	 * 
	 * @return	true if the behaviour is enabled, false otherwise
	 */
	public boolean isEnabled() 
	{
	    Stack<Integer> stack = disabled.get();
	    return stack.search(hashCode()) == -1;
	}

	/**
	 * Get the notification frequency
	 * 
	 * @return	the notification frequency
	 */
	public NotificationFrequency getNotificationFrequency() 
	{
	    return frequency;
	}
	
	/**
     * Stack specific Thread Local
     * 
     * @author David Caruana
     */
    class StackThreadLocal extends ThreadLocal<Stack<Integer>>
    {
        @Override
        protected Stack<Integer> initialValue()
        {
            return new Stack<Integer>();
        }
    }
}
