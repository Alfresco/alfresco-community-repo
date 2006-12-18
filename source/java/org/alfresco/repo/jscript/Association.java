/**
 * 
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
