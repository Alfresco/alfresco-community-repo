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

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.People;
import org.alfresco.rest.api.Sites;
import org.alfresco.rest.api.model.Person;
import org.alfresco.rest.framework.core.exceptions.ConstraintViolatedException;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.NoSuchPersonException;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.alfresco.service.cmr.usage.ContentUsageService;
import org.alfresco.service.namespace.QName;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    protected void processPersonProperties(final Map<QName, Serializable> nodeProps)
    {
		if(!contentUsageService.getEnabled())
		{
			// quota used will always be 0 in this case so remove from the person properties
			nodeProps.remove(ContentModel.PROP_SIZE_QUOTA);
			nodeProps.remove(ContentModel.PROP_SIZE_CURRENT);
		}

		// The person description is located in a separate content file located at cm:persondescription
		// "Inline" this data, by removing the cm:persondescription property and adding a temporary property
		// (Person.PROP_PERSON_DESCRIPTION) containing the actual content as a string.
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
    		processPersonProperties(nodeProps);
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
	public Person create(Person person)
	{
		validateCreatePersonData(person);

		// TODO: check, is this transaction safe?
		// Unfortunately PersonService.createPerson(...) only throws an AlfrescoRuntimeException
		// rather than a more specific exception and does not use a message ID either, so there's
		// no sensible way to know that it was thrown due to the user already existing - hence this check here.
		if (personService.personExists(person.getUserName()))
		{
			throw new ConstraintViolatedException("Person '"+person.getUserName()+"' already exists.");
		}
		Map<QName, Serializable> props = person.toProperties();

		MutableAuthenticationService mas = (MutableAuthenticationService) authenticationService;
		mas.createAuthentication(person.getUserName(), person.getPassword().toCharArray());
		mas.setAuthenticationEnabled(person.getUserName(), person.isEnabled());
		NodeRef nodeRef = personService.createPerson(props);

        // Write the contents of PersonUpdate.getDescription() text to a content file
        // and store the content URL in ContentModel.PROP_PERSONDESC
        if (person.getDescription() != null)
        {
            savePersonDescription(person.getDescription(), nodeRef);
        }

        // Return a fresh retrieval
        return getPerson(person.getUserName());
    }

    /**
     * Write the description to a content file and store the content URL in
     * ContentModel.PROP_PERSONDESC
     * 
     * @param description
     * @param nodeRef
     */
    private void savePersonDescription(final String description, final NodeRef nodeRef)
    {
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_PERSONDESC, true);
                writer.putContent(description);
                return null;
            }
        });
    }

	private void validateCreatePersonData(Person person)
	{
		checkRequiredField("id", person.getUserName());
		checkRequiredField("firstName", person.getFirstName());
		checkRequiredField("email", person.getEmail());
		checkRequiredField("enabled", person.isEnabled());
		checkRequiredField("password", person.getPassword());
	}
	
	private void checkRequiredField(String fieldName, Object fieldValue)
	{
		if (fieldValue == null)
		{
			throw new InvalidArgumentException("Field '"+fieldName+"' is null, but is required.");
		}
	}

    public Person update(String personId, final Person person)
    {
        MutableAuthenticationService mutableAuthenticationService = (MutableAuthenticationService) authenticationService;

        final String personIdToUpdate = validatePerson(personId);
        final Map<QName, Serializable> properties = person.toProperties();

        if (person.getPassword() != null && !person.getPassword().isEmpty())
        {
            // an Admin user can update without knowing the original pass - but
            // must know their own!
            mutableAuthenticationService.setAuthentication(personIdToUpdate, person.getPassword().toCharArray());
        }

        if (person.isEnabled() != null)
        {
            mutableAuthenticationService.setAuthenticationEnabled(personIdToUpdate, person.isEnabled());
        }
        
        if (person.getDescription() != null)
        {
            // Remove person description from saved properties
            properties.remove(ContentModel.PROP_PERSONDESC);

            // Custom save for person description.
            NodeRef personNodeRef = personService.getPerson(personIdToUpdate, false);
            savePersonDescription(person.getDescription(), personNodeRef);
        }

        personService.setPersonProperties(personIdToUpdate, properties, false);

        return getPerson(personId);
    }
}
