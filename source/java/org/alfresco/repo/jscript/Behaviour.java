/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
