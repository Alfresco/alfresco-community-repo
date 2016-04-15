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
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.springframework.extensions.surf.util.ParameterCheck;
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
	 * @param services ServiceRegistry
	 * @param childAssocRef ChildAssociationRef
	 * @param scope Scriptable
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
	
	public String getName()
	{
		return childAssocRef.getQName().toString();
	}
	
	public ScriptNode getParent()
	{
		return (ScriptNode)new ValueConverter().convertValueForScript(this.services, this.scope, null, childAssocRef.getParentRef());
	}
	
	public ScriptNode getChild()
	{
		return (ScriptNode)new ValueConverter().convertValueForScript(this.services, this.scope, null, childAssocRef.getChildRef());
	}
	
	public boolean isPrimary()
	{
		return this.childAssocRef.isPrimary();
	}
	
	public int getNthSibling()
	{
		return this.childAssocRef.getNthSibling();
	}
}
