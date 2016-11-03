/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.rest.api.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.People;
import org.alfresco.rest.api.Sites;
import org.alfresco.rest.api.model.Company;
import org.alfresco.rest.api.model.Person;
import org.alfresco.rest.api.model.PersonUpdate;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.NoSuchPersonException;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.alfresco.service.cmr.usage.ContentUsageService;
import org.alfresco.service.namespace.QName;

/**
 * Centralises access to people services and maps between representations.
 * 
 * @author steveglover
 * @since publicapi1.0
 */
public class PeopleImpl implements People
{
	protected Nodes nodes;
	protected Sites sites;

	protected SiteService siteService;
	protected NodeService nodeService;
    protected PersonService personService;
    protected AuthenticationService authenticationService;
    protected ContentUsageService contentUsageService;
    protected ContentService contentService;
    protected ThumbnailService thumbnailService;

	public void setSites(Sites sites)
	{
		this.sites = sites;
	}

	public void setSiteService(SiteService siteService)
	{
		this.siteService = siteService;
	}

	public void setNodes(Nodes nodes)
	{
		this.nodes = nodes;
	}

	public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
	
	public void setPersonService(PersonService personService)
    {
		this.personService = personService;
	}
	
	public void setAuthenticationService(AuthenticationService authenticationService)
    {
		this.authenticationService = authenticationService;
	}

	public void setContentUsageService(ContentUsageService contentUsageService)
    {
		this.contentUsageService = contentUsageService;
	}

	public void setContentService(ContentService contentService)
	{
		this.contentService = contentService;
	}
	
	public void setThumbnailService(ThumbnailService thumbnailService)
    {
		this.thumbnailService = thumbnailService;
	}
	
	public String validatePerson(String personId)
	{
		return validatePerson(personId, false);
	}

	public String validatePerson(String personId, boolean validateIsCurrentUser)
	{
		if(personId == null)
		{
			throw new InvalidArgumentException("personId is null.");
		}
        
    	if(personId.equalsIgnoreCase(DEFAULT_USER))
    	{
    		personId = AuthenticationUtil.getFullyAuthenticatedUser();
    	}

    	personId = personService.getUserIdentifier(personId);
		if(personId == null)
		{
            // "User " + personId + " does not exist"
            throw new EntityNotFoundException("personId is null.");
		}

		if(validateIsCurrentUser)
		{
			String currentUserId = AuthenticationUtil.getFullyAuthenticatedUser();
			if(!currentUserId.equalsIgnoreCase(personId))
			{
				throw new EntityNotFoundException(personId);
			}
		}

    	return personId;
	}

    protected void processPersonProperties(String userName, final Map<QName, Serializable> nodeProps)
    {
		if(!contentUsageService.getEnabled())
		{
			// quota used will always be 0 in this case so remove from the person properties
			nodeProps.remove(ContentModel.PROP_SIZE_QUOTA);
			nodeProps.remove(ContentModel.PROP_SIZE_CURRENT);
		}

		final ContentData personDescription = (ContentData)nodeProps.get(ContentModel.PROP_PERSONDESC);
		if(personDescription != null)
		{
			nodeProps.remove(ContentModel.PROP_PERSONDESC);

			AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
			{
				@Override
				public Void doWork() throws Exception
				{
					ContentReader reader = contentService.getRawReader(personDescription.getContentUrl());
					if(reader != null && reader.exists())
					{
						String description = reader.getContentString();
						nodeProps.put(Person.PROP_PERSON_DESCRIPTION, description);
					}

					return null;
				}
			});
		}
    }
    
    public boolean hasAvatar(NodeRef personNodeRef)
    {
    	if(personNodeRef != null)
    	{
			List<AssociationRef> avatorAssocs = nodeService.getTargetAssocs(personNodeRef, ContentModel.ASSOC_AVATAR);
			return(avatorAssocs.size() > 0);
    	}
    	else
    	{
    		return false;
    	}
    }

    public NodeRef getAvatar(String personId)
    {
    	NodeRef avatar = null;

    	personId = validatePerson(personId);
    	NodeRef personNode = personService.getPerson(personId);
    	if(personNode != null)
    	{
			List<AssociationRef> avatorAssocs = nodeService.getTargetAssocs(personNode, ContentModel.ASSOC_AVATAR);
			if(avatorAssocs.size() > 0)
			{
				AssociationRef ref = avatorAssocs.get(0);
				NodeRef thumbnailNodeRef = thumbnailService.getThumbnailByName(ref.getTargetRef(), ContentModel.PROP_CONTENT, "avatar");
				if(thumbnailNodeRef != null)
				{
					avatar = thumbnailNodeRef;
				}
				else
				{
		    		throw new EntityNotFoundException("avatar");
				}
			}
			else
			{
	    		throw new EntityNotFoundException("avatar");
			}
    	}
    	else
    	{
    		throw new EntityNotFoundException(personId);
    	}
    	
    	return avatar;
    }

    /**
     * 
     * @throws NoSuchPersonException if personId does not exist
     */
    public Person getPerson(String personId)
    {
    	Person person = null;

    	personId = validatePerson(personId);
    	NodeRef personNode = personService.getPerson(personId, false);
    	if (personNode != null) 
    	{
    		Map<QName, Serializable> nodeProps = nodeService.getProperties(personNode);
    		processPersonProperties(personId, nodeProps);
    		// TODO this needs to be run as admin but should we do this here?
    		final String pId = personId;
    		Boolean enabled = AuthenticationUtil.runAsSystem(new RunAsWork<Boolean>()
			{
    			public Boolean doWork() throws Exception
    			{
    				return authenticationService.getAuthenticationEnabled(pId);
    			}
			});
    		person = new Person(personNode, nodeProps, enabled);

    		// get avatar information
    		if(hasAvatar(personNode))
    		{
	    		try
	    		{
		    		NodeRef avatar = getAvatar(personId);
		    		person.setAvatarId(avatar);
	    		}
	    		catch(EntityNotFoundException e)
	    		{
	    			// shouldn't happen, but ok
	    		}
    		}
    	}
    	else
    	{
    		throw new EntityNotFoundException(personId);
    	}

        return person;
    }

	@Override
	public Person create(PersonUpdate person)
	{
		Map<QName, Serializable> props = person.toProperties();
		NodeRef nodeRef = personService.createPerson(props);

		// Return a fresh retrieval
		props = nodeService.getProperties(nodeRef);
		final boolean enabled = personService.isEnabled(person.getUserName());
		return new Person(nodeRef, props, enabled);

		// ...or
//		return getPerson(person.getUserName());
	}

	/**

    public Person updatePerson(String personId, final Person person)
    {
    	personId = validatePerson(personId);

    	final Map<QName, Serializable> properties = toProperties(person);

		final String pId = personId;
		AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
		{
			@Override
			public Void doWork() throws Exception
			{
		    	personService.setPersonProperties(pId, properties, false);
				return null;
			}
			
		});

    	return getPerson(personId);
    }
 */
}
