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
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.util.ParameterCheck;
import org.mozilla.javascript.Scriptable;

/**
 * Object representing an association
 * 
 * @author Roy Wetherall
 */
public class Association implements Scopeable, Serializable 
{
	/** Serial version UUID*/
	private static final long serialVersionUID = 897788515655487131L;

	/** Service registry **/
	private ServiceRegistry services;
	
	/** Script scope **/
	private Scriptable scope;
	
	/** Association reference **/
	private AssociationRef assocRef;

	public Association(ServiceRegistry services, AssociationRef assocRef)
	{
		this(services, assocRef, null);
	}
	
	public Association(ServiceRegistry services, AssociationRef assocRef, Scriptable scope)
	{
		ParameterCheck.mandatory("Service registry", services);
		ParameterCheck.mandatory("Association reference", assocRef);		
		this.services = services;
		this.assocRef = assocRef;
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
	
	public AssociationRef getAssociationRef()
	{
		return this.assocRef;
	}
	
	public String getType()
	{
		return assocRef.getTypeQName().toString();
	}
	
	public String jsGet_type()
	{
		return getType();
	}
	
	public Node getSource()
	{
		return (Node)new ValueConverter().convertValueForScript(this.services, this.scope, null, assocRef.getSourceRef());
	}
	
	public Node jsGet_source()
	{
		return getSource();
	}
	
	public Node getTarget()
	{
		return (Node)new ValueConverter().convertValueForScript(this.services, this.scope, null, assocRef.getTargetRef());
	}
	
	public Node jsGet_target()
	{
		return getTarget();
	}
}
