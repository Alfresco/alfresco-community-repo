/**
 * 
 */
package org.alfresco.repo.jscript;

import java.io.Serializable;

import org.alfresco.service.ServiceRegistry;
import org.mozilla.javascript.Scriptable;

/**
 * Object representing the behaviour information
 * 
 * @author Roy Wetherall
 */
public class Behaviour implements Scopeable, Serializable 
{
	/** Serial version UID **/
	private static final long serialVersionUID = 1936017361886646100L;
	
	/** Service registry **/
	private ServiceRegistry services;
	
	/** Script scope **/
	private Scriptable scope;
	
	/** The name of the policy that this behaviour is linked to **/
	private String name;
	
	/** The behaviour argument values **/
	private Object[] args;
	
	/** Cached js converted argument values **/
	private Serializable[] jsArgs;
	
	/**
	 * Constructor 
	 * 
	 * @param services	the service registry
	 * @param name		the name of the policy associated with this behaviour
	 * @param args		the argument values
	 */
	public Behaviour(ServiceRegistry services, String name, Object[] args)
	{
		this.services = services;
		this.name = name;
		this.args = args;
	}

	/**
	 * @see org.alfresco.repo.jscript.Scopeable#setScope(org.mozilla.javascript.Scriptable)
	 */
	public void setScope(Scriptable scope) 
	{
		this.scope = scope;
	}
	
	/**
	 * Get the policy name
	 * 
	 * @return	the name of the policy
	 */
	public String getName()
	{
		return this.name;
	}
	
	/**
	 * JS accessor method
	 * 
	 * @return	the name of the policy
	 */
	public String jsGet_name()
	{
		return getName();
	}
	
	/**
	 * The argument values
	 * 
	 * @return	array containing the argument values
	 */
	public Serializable[] getArgs()
	{
		if (this.jsArgs == null)
		{
			ValueConverter valueConverter = new ValueConverter();
			this.jsArgs = new Serializable[args.length];
			int index = 0;
			for (Object arg : this.args) 
			{
				this.jsArgs[index] = valueConverter.convertValueForScript(services, this.scope, null, (Serializable)arg);
				index ++;
			}
		}
		return this.jsArgs;
	}
	
	/**
	 * JS accessor method
	 * 
	 * @return	array containing the argument values
	 */
	public Serializable[] jsGet_args()
	{
		return getArgs();
	}
}
