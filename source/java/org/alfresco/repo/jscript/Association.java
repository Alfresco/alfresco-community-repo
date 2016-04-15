/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.jscript;

import java.io.Serializable;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.ParameterCheck;
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
	
	public QName getTypeQName()
	{
	    return assocRef.getTypeQName();
	}
	
	public ScriptNode getSource()
	{
		return (ScriptNode)new ValueConverter().convertValueForScript(this.services, this.scope, null, assocRef.getSourceRef());
	}
	
	public ScriptNode getTarget()
	{
		return (ScriptNode)new ValueConverter().convertValueForScript(this.services, this.scope, null, assocRef.getTargetRef());
	}
}
