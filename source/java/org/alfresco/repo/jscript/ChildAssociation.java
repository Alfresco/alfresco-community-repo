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
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.util.ParameterCheck;
import org.mozilla.javascript.Scriptable;

/**
 * Object representing a child association
 * 
 * @author Roy Wetherall
 */
public class ChildAssociation implements Scopeable, Serializable 
{
	/** Serial version UUID **/
	private static final long serialVersionUID = -2122640697340663213L;

	/** Service registry **/
	private ServiceRegistry services;
	
	/** Script scope **/
	private Scriptable scope;
	
	/** Child association reference **/
	private ChildAssociationRef childAssocRef;

	public ChildAssociation(ServiceRegistry services, ChildAssociationRef childAssocRef)
	{
		this(services, childAssocRef, null);
	}
	
	/**
	 * Constructor
	 * 
	 * @param services
	 * @param childAssocRef
	 */
	public ChildAssociation(ServiceRegistry services, ChildAssociationRef childAssocRef, Scriptable scope)
	{
		ParameterCheck.mandatory("Service registry", services);
		ParameterCheck.mandatory("Child association reference", childAssocRef);		
		this.services = services;
		this.childAssocRef = childAssocRef;
		if (scope != null)
		{
			this.scope = scope;
		}
	}

	/**
	 * @see org.alfresco.repo.jscript.Scopeable#setScope(org.mozilla.javascript.Scriptable)
	 */
	public void setScope(Scriptable scope) 
	{
		this.scope = scope;
	}
	
	public ChildAssociationRef getChildAssociationRef()
	{
		return this.childAssocRef;
	}
	
	public String getType()
	{
		return childAssocRef.getTypeQName().toString();
	}
	
	public String jsGet_type()
	{
		return getType();
	}
	
	public String getName()
	{
		return childAssocRef.getQName().toString();
	}
	
	public String jsGet_name()
	{
		return getName();
	}
	
	public Node getParent()
	{
		return (Node)new ValueConverter().convertValueForScript(this.services, this.scope, null, childAssocRef.getParentRef());
	}
	
	public Node jsGet_parent()
	{
		return getParent();
	}
	
	public Node getChild()
	{
		return (Node)new ValueConverter().convertValueForScript(this.services, this.scope, null, childAssocRef.getChildRef());
	}
	
	public Node jsGet_child()
	{
		return getChild();
	}
	
	public boolean isPrimary()
	{
		return this.childAssocRef.isPrimary();
	}
	
	public int getNthSibling()
	{
		return this.childAssocRef.getNthSibling();
	}
	
	public int jsGet_nthSibling()
	{
		return getNthSibling();
	}
}
