/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.repo.policy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.namespace.QName;

/**
 * Policy scope.  
 * <p>
 * Helper often used by policies which require information
 * about a node to be gathered, for example onCopy or onCreateVersion.
 * 
 * @author Roy Wetherall
 */
public class PolicyScope extends AspectDetails
{
	/**
	 * The aspects
	 */
	protected Map<QName, AspectDetails> aspectCopyDetails = new HashMap<QName, AspectDetails>();
	
	/**
	 * Constructor
	 * 
	 * @param classRef  the class reference
	 */
	public PolicyScope(QName classRef)
	{
		super(classRef);
	}
	
	/**
	 * Add a property 
	 * 
	 * @param classRef  the class reference
	 * @param qName		the qualified name of the property
	 * @param value		the value of the property
	 */
	public void addProperty(QName classRef, QName qName, Serializable value) 
	{
		if (classRef.equals(this.classRef) == true)
		{
			addProperty(qName, value);
		}
		else
		{
			AspectDetails aspectDetails = this.aspectCopyDetails.get(classRef);
			if (aspectDetails == null)
			{
				// Add the aspect
				aspectDetails = addAspect(classRef);
			}
			aspectDetails.addProperty(qName, value);
		}
	}
	
	/**
	 * Removes a property from the list
	 * 
	 * @param classRef	the class reference
	 * @param qName		the qualified name
	 */
	public void removeProperty(QName classRef, QName qName) 
	{
		if (classRef.equals(this.classRef) == true)
		{
			removeProperty(qName);
		}
		else
		{
			AspectDetails aspectDetails = this.aspectCopyDetails.get(classRef);
			if (aspectDetails != null)
			{
				aspectDetails.removeProperty(qName);
			}				
		}
	}
	
	/**
	 * Get the properties
	 * 
	 * @param classRef  the class ref
	 * @return			the properties that should be copied
	 */
	public Map<QName, Serializable> getProperties(QName classRef)
	{
		Map<QName, Serializable> result = null;
		if (classRef.equals(this.classRef) == true)
		{
			result = getProperties();
		}
		else
		{
			AspectDetails aspectDetails = this.aspectCopyDetails.get(classRef);
			if (aspectDetails != null)
			{
				result = aspectDetails.getProperties();
			}
		}
		
		return result;
	}
	
	/**
	 * Adds a child association
	 * 
	 * @param classRef
	 * @param qname
	 * @param childAssocRef
	 */
	public void addChildAssociation(QName classRef, ChildAssociationRef childAssocRef) 
	{
		if (classRef.equals(this.classRef) == true)
		{
			addChildAssociation(childAssocRef);
		}
		else
		{
			AspectDetails aspectDetails = this.aspectCopyDetails.get(classRef);
			if (aspectDetails == null)
			{
				// Add the aspect
				aspectDetails = addAspect(classRef);
			}
			aspectDetails.addChildAssociation(childAssocRef);
		}
	}
	
	/**
	 * 
	 * @param classRef
	 * @param childAssocRef
	 * @param alwaysTraverseAssociation
	 */
	public void addChildAssociation(QName classRef, ChildAssociationRef childAssocRef, boolean alwaysTraverseAssociation) 
	{
		if (classRef.equals(this.classRef) == true)
		{
			addChildAssociation(childAssocRef, alwaysTraverseAssociation);
		}
		else
		{
			AspectDetails aspectDetails = this.aspectCopyDetails.get(classRef);
			if (aspectDetails == null)
			{
				// Add the aspect
				aspectDetails = addAspect(classRef);
			}
			aspectDetails.addChildAssociation(childAssocRef, alwaysTraverseAssociation);
		}
	}
	
	/**
	 * Get a child association
	 * 
	 * @param classRef
	 * @return
	 */
	public List<ChildAssociationRef> getChildAssociations(QName classRef) 
	{
		List<ChildAssociationRef> result = null;
		if (classRef.equals(this.classRef) == true)
		{
			result = getChildAssociations();
		}
		else
		{
			AspectDetails aspectDetails = this.aspectCopyDetails.get(classRef);
			if (aspectDetails != null)
			{
				result = aspectDetails.getChildAssociations();
			}
		}
		
		return result;
	}
	
	public boolean isChildAssociationRefAlwaysTraversed(QName classRef, ChildAssociationRef childAssocRef)
	{
		boolean result = false;
		if (classRef.equals(this.classRef) == true)
		{
			result = isChildAssociationRefAlwaysTraversed(childAssocRef);
		}
		else
		{
			AspectDetails aspectDetails = this.aspectCopyDetails.get(classRef);
			if (aspectDetails != null)
			{
				result = aspectDetails.isChildAssociationRefAlwaysTraversed(childAssocRef);
			}
		}
		
		return result;
	}
	
	/**
	 * Add an association
	 * 
	 * @param classRef
	 * @param qname
	 * @param nodeAssocRef
	 */
	public void addAssociation(QName classRef, AssociationRef nodeAssocRef)
	{
		if (classRef.equals(this.classRef) == true)
		{
			addAssociation(nodeAssocRef);
		}
		else
		{
			AspectDetails aspectDetails = this.aspectCopyDetails.get(classRef);
			if (aspectDetails == null)
			{
				// Add the aspect
				aspectDetails = addAspect(classRef);
			}
			aspectDetails.addAssociation(nodeAssocRef);
		}
	}
	

	
	/**
	 * Get associations
	 * 
	 * @param classRef
	 * @return
	 */
	public List<AssociationRef> getAssociations(QName classRef) 
	{
		List<AssociationRef> result = null;
		if (classRef.equals(this.classRef) == true)
		{
			result = getAssociations();
		}
		else
		{
			AspectDetails aspectDetails = this.aspectCopyDetails.get(classRef);
			if (aspectDetails != null)
			{
				result = aspectDetails.getAssociations();
			}
		}
		
		return result;
	}
	
	/**
	 * Add an aspect 
	 * 
	 * @param aspect	the aspect class reference
	 * @return			the apsect copy details (returned as a helper)
	 */
	public AspectDetails addAspect(QName aspect) 
	{
		AspectDetails result = new AspectDetails(aspect);
		this.aspectCopyDetails.put(aspect, result);
		return result;
	}
	
	/**
	 * Removes an aspect from the list 
	 * 
	 * @param aspect	the aspect class reference
	 */
	public void removeAspect(QName aspect) 
	{
		this.aspectCopyDetails.remove(aspect);
	}
	
	/**
	 * Gets a list of the aspects 
	 * 
	 * @return  a list of aspect to copy
	 */
	public Set<QName> getAspects()
	{
		return this.aspectCopyDetails.keySet();
	}		
}

/**
 * Aspect details class.  
 * <p>
 * Contains the details of an aspect this can be used for copying or versioning.
 * 
 * @author Roy Wetherall
 */
/*package*/ class AspectDetails
{
	/**
	 * The properties
	 */
	protected Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
	
	/**
	 * The child associations
	 */
	protected List<ChildAssociationRef> childAssocs = new ArrayList<ChildAssociationRef>();
	
	/**
	 * The target associations
	 */
	protected List<AssociationRef> targetAssocs = new ArrayList<AssociationRef>();
	
	/**
	 * The class ref of the aspect
	 */
	protected QName classRef;

	/**
	 * Map of assocs that will always be traversed
	 */
	protected Map<ChildAssociationRef, ChildAssociationRef> alwaysTraverseMap = new HashMap<ChildAssociationRef, ChildAssociationRef>();

	/**
	 * Constructor
	 * 
	 * @param classRef  the class ref
	 */
	public AspectDetails(QName classRef)
	{
		this.classRef = classRef;
	}
	
	/**
	 * Add a property to the list 
	 * 
	 * @param qName		the qualified name of the property
	 * @param value		the value of the property
	 */
	public void addProperty(QName qName, Serializable value) 
	{
		this.properties.put(qName, value);			
	}
	
	/**
	 * Remove a property from the list
	 * 
	 * @param qName		the qualified name of the property
	 */
	public void removeProperty(QName qName) 
	{
		this.properties.remove(qName);			
	}
	
	/**
	 * Gets the map of properties
	 * 
	 * @return  map of property names and values
	 */
	public Map<QName, Serializable> getProperties() 
	{
		return properties;
	}
	
	/**
	 * Add a child association 
	 * 
	 * @param childAssocRef the child association reference
	 */
	protected void addChildAssociation(ChildAssociationRef childAssocRef) 
	{
		this.childAssocs.add(childAssocRef);
	}
	
	/**
	 * Add a child association 
	 * 
	 * @param childAssocRef		the child assoc reference
	 * @param alwaysDeepCopy	indicates whether the assoc should always be traversed
	 */
	protected void addChildAssociation(ChildAssociationRef childAssocRef, boolean alwaysTraverseAssociation) 
	{
		addChildAssociation(childAssocRef);
		
		if (alwaysTraverseAssociation == true)
		{
			// Add to the list of deep copy child associations
			this.alwaysTraverseMap.put(childAssocRef, childAssocRef);
		}
	}
	
	/**
	 * Indicates whether a child association ref is always traversed or not
	 * 
	 * @param childAssocRef	the child association reference
	 * @return				true if the assoc is always traversed, false otherwise
	 */
	protected boolean isChildAssociationRefAlwaysTraversed(ChildAssociationRef childAssocRef)
	{
		return this.alwaysTraverseMap.containsKey(childAssocRef);
	}
	
	/**
	 * Gets the child associations to be copied
	 * 
	 * @return  map containing the child associations to be copied
	 */
	public List<ChildAssociationRef> getChildAssociations() 
	{
		return this.childAssocs;
	}
	
	/**
	 * Adds an association to be copied
	 * 
	 * @param qname			the qualified name of the association
	 * @param nodeAssocRef	the association reference
	 */
	protected void addAssociation(AssociationRef nodeAssocRef)
	{
		this.targetAssocs.add(nodeAssocRef);
	}
	
	/**
	 * Gets the map of associations to be copied
	 * 
	 * @return  a map conatining the associations to be copied
	 */
	public List<AssociationRef> getAssociations() 
	{
		return this.targetAssocs;
	}	
}