/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.authentication.ResetPasswordService;
import org.alfresco.repo.security.authentication.ResetPasswordServiceImpl.ResetPasswordDetails;
import org.alfresco.repo.security.authentication.ResetPasswordServiceImpl.ResetPasswordWorkflowException;
import org.alfresco.repo.security.authentication.ResetPasswordServiceImpl.ResetPasswordWorkflowInvalidUserException;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.People;
import org.alfresco.rest.api.Renditions;
import org.alfresco.rest.api.Sites;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.api.model.PasswordReset;
import org.alfresco.rest.api.model.Person;
import org.alfresco.rest.api.model.Rendition;
import org.alfresco.rest.framework.core.exceptions.ConstraintViolatedException;
import org.alfresco.rest.framework.core.exceptions.DisabledServiceException;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
import org.alfresco.rest.framework.resource.content.BasicContentInfo;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.resource.parameters.SortColumn;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.*;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.alfresco.service.cmr.usage.ContentUsageService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Centralises access to people services and maps between representations.
 * 
 * @author steveglover
 * @since publicapi1.0
 */
public class PeopleImpl implements People
{
    private static final Log LOGGER = LogFactory.getLog(PeopleImpl.class);

    private static final List<String> EXCLUDED_NS = Arrays.asList(
            NamespaceService.SYSTEM_MODEL_1_0_URI,
            "http://www.alfresco.org/model/user/1.0",
            NamespaceService.CONTENT_MODEL_1_0_URI);
    private static final List<QName> EXCLUDED_ASPECTS = Arrays.asList();
    private static final List<QName> EXCLUDED_PROPS = Arrays.asList();
    private static final int USERNAME_MAXLENGTH = 100;
    private static final String[] RESERVED_AUTHORITY_PREFIXES =
    {
            PermissionService.GROUP_PREFIX,
            PermissionService.ROLE_PREFIX
    };
    private static final char[] illegalCharacters = {'/', '\\', '\r', '\n'};

    protected Nodes nodes;
	protected Sites sites;
	protected SiteService siteService;
	protected NodeService nodeService;
    protected PersonService personService;
    protected AuthenticationService authenticationService;
    protected AuthorityService authorityService;
    protected ContentUsageService contentUsageService;
    protected ContentService contentService;
    protected ThumbnailService thumbnailService;
    protected ResetPasswordService resetPasswordService;
    protected Renditions renditions;

    private final static Map<String, QName> sort_params_to_qnames;
    static
    {
        Map<String, QName> aMap = new HashMap<>(3);
        aMap.put(PARAM_FIRST_NAME, ContentModel.PROP_FIRSTNAME);
        aMap.put(PARAM_LAST_NAME, ContentModel.PROP_LASTNAME);
        aMap.put(PARAM_ID, ContentModel.PROP_USERNAME);
        sort_params_to_qnames = Collections.unmodifiableMap(aMap);
    }

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

    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
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

    public void setResetPasswordService(ResetPasswordService resetPasswordService)
    {
        this.resetPasswordService = resetPasswordService;
    }

    public void setRenditions(Renditions renditions)
    {
        this.renditions = renditions;
    }


    /**
     * Validate, perform -me- substitution and canonicalize the person ID.
     * 
     * @param personId
     * @return The validated and processed ID.
     */
    @Override
	public String validatePerson(String personId)
	{
		return validatePerson(personId, false);
	}

    @Override
	public String validatePerson(final String requestedPersonId, boolean validateIsCurrentUser)
	{
        String personId = requestedPersonId;
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
            // Could not find canonical user ID by case-sensitive ID.
            throw new EntityNotFoundException(requestedPersonId);
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
    	return (getAvatarOriginal(personNodeRef) != null);
    }

    @Override
    public NodeRef getAvatar(String personId)
    {
    	NodeRef avatar = null;
    	personId = validatePerson(personId);
    	NodeRef personNode = personService.getPerson(personId);
    	if(personNode != null)
    	{
            NodeRef avatarOrig = getAvatarOriginal(personNode);
            avatar = thumbnailService.getThumbnailByName(avatarOrig, ContentModel.PROP_CONTENT, "avatar");
    	}

    	if (avatar == null)
    	{
    		throw new EntityNotFoundException(personId);
    	}

    	return avatar;
    }

    private NodeRef getAvatarOriginal(NodeRef personNode)
    {
        NodeRef avatarOrigNodeRef = null;
        List<ChildAssociationRef> avatarChildAssocs = nodeService.getChildAssocs(personNode, Collections.singleton(ContentModel.ASSOC_PREFERENCE_IMAGE));
        if (avatarChildAssocs.size() > 0)
        {
            ChildAssociationRef ref = avatarChildAssocs.get(0);
            avatarOrigNodeRef = ref.getChildRef();
        }
        else
        {
            // TODO do we still need this ? - backward compatible with JSF web-client avatar
            List<AssociationRef> avatorAssocs = nodeService.getTargetAssocs(personNode, ContentModel.ASSOC_AVATAR);
            if (avatorAssocs.size() > 0)
            {
                AssociationRef ref = avatorAssocs.get(0);
                avatarOrigNodeRef = ref.getTargetRef();
            }
        }
        return avatarOrigNodeRef;
    }

    @Override
    public BinaryResource downloadAvatarContent(String personId, Parameters parameters)
    {
        personId = validatePerson(personId);
        NodeRef personNode = personService.getPerson(personId);
        NodeRef avatarNodeRef = getAvatarOriginal(personNode);

        return renditions.getContentNoValidation(avatarNodeRef, "avatar", parameters);
    }

    @Override
    public Person uploadAvatarContent(String personId, BasicContentInfo contentInfo, InputStream stream, Parameters parameters)
    {
        if (!thumbnailService.getThumbnailsEnabled())
        {
            throw new DisabledServiceException("Thumbnail generation has been disabled.");
        }

        personId = validatePerson(personId);
        checkCurrentUserOrAdmin(personId);

        NodeRef personNode = personService.getPerson(personId);
        NodeRef avatarOrigNodeRef = getAvatarOriginal(personNode);

        if (avatarOrigNodeRef != null)
        {
            deleteAvatar(avatarOrigNodeRef);
        }

        QName origAvatarQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "origAvatar");
        nodeService.addAspect(personNode, ContentModel.ASPECT_PREFERENCES, null);
        ChildAssociationRef assoc = nodeService.createNode(personNode, ContentModel.ASSOC_PREFERENCE_IMAGE, origAvatarQName,
                ContentModel.TYPE_CONTENT);
        NodeRef avatar = assoc.getChildRef();
        String avatarOriginalNodeId = avatar.getId();

        // TODO do we still need this ? - backward compatible with JSF web-client avatar
        nodeService.createAssociation(personNode, avatar, ContentModel.ASSOC_AVATAR);

        Node n = nodes.updateContent(avatarOriginalNodeId, contentInfo, stream, parameters);
        String mimeType = n.getContent().getMimeType();

        if (mimeType.indexOf("image/") != 0)
        {
            throw new InvalidArgumentException(
                    "Uploaded content must be an image (content type determined to be '"+mimeType+"')");
        }

        // create thumbnail synchronously
        Rendition avatarR = new Rendition();
        avatarR.setId("avatar");
        renditions.createRendition(avatar, avatarR, false, parameters);

        List<String> include = Arrays.asList(
                PARAM_INCLUDE_ASPECTNAMES,
                PARAM_INCLUDE_PROPERTIES);

        return getPersonWithProperties(personId, include);
    }

    @Override
    public void deleteAvatarContent(String personId)
    {
        personId = validatePerson(personId);
        checkCurrentUserOrAdmin(personId);

        NodeRef personNode = personService.getPerson(personId);
        NodeRef avatarOrigNodeRef = getAvatarOriginal(personNode);
        if (avatarOrigNodeRef != null)
        {
            deleteAvatar(avatarOrigNodeRef);
        }
    }

    private void deleteAvatar(NodeRef avatarOrigNodeRef)
    {
        // Set as temporary to permanently delete node (instead of archiving)
        nodeService.addAspect(avatarOrigNodeRef, ContentModel.ASPECT_TEMPORARY, null);

        nodeService.deleteNode(avatarOrigNodeRef);
    }


    /**
     * Get a full representation of a person.
     * 
     * @throws NoSuchPersonException
     *             if personId does not exist
     */
    @Override
    public Person getPerson(String personId)
    {
        personId = validatePerson(personId);
        List<String> include = Arrays.asList(
                PARAM_INCLUDE_ASPECTNAMES,
                PARAM_INCLUDE_PROPERTIES,
                PARAM_INCLUDE_CAPABILITIES);
        Person person = getPersonWithProperties(personId, include);

        return person;
    }
    
    public Person getPerson(String personId, List<String> include)
    {
        personId = validatePerson(personId);
        Person person = getPersonWithProperties(personId, include);

        return person;
    }

    @Override
    public CollectionWithPagingInfo<Person> getPeople(final Parameters parameters)
    {
        Paging paging = parameters.getPaging();
        PagingRequest pagingRequest = Util.getPagingRequest(paging);

        List<Pair<QName, Boolean>> sortProps = getSortProps(parameters);

        // For now the results are not filtered
        // please see REPO-555
        final PagingResults<PersonService.PersonInfo> pagingResult = personService.getPeople(null, null, sortProps, pagingRequest);

        final List<PersonService.PersonInfo> page = pagingResult.getPage();
        int totalItems = pagingResult.getTotalResultCount().getFirst();
        final String personId = AuthenticationUtil.getFullyAuthenticatedUser();
        List<Person> people = new AbstractList<Person>()
        {
            @Override
            public Person get(int index)
            {
                PersonService.PersonInfo personInfo = page.get(index);
                Person person = getPersonWithProperties(personInfo.getUserName(), parameters.getInclude());
                return person;
            }

            @Override
            public int size()
            {
                return page.size();
            }
        };

        return CollectionWithPagingInfo.asPaged(paging, people, pagingResult.hasMoreItems(), totalItems);
    }

    private List<Pair<QName, Boolean>> getSortProps(Parameters parameters)
    {
        List<Pair<QName, Boolean>> sortProps = new ArrayList<>();
        List<SortColumn> sortCols = parameters.getSorting();
        if ((sortCols != null) && (sortCols.size() > 0))
        {
            for (SortColumn sortCol : sortCols)
            {
                QName sortPropQName = sort_params_to_qnames.get(sortCol.column);
                if (sortPropQName == null)
                {
                    throw new InvalidArgumentException("Invalid sort field: " + sortCol.column);
                }
                sortProps.add(new Pair<>(sortPropQName, (sortCol.asc ? Boolean.TRUE : Boolean.FALSE)));
            }
        }
        else
        {
            // default sort order
            sortProps.add(new Pair<>(ContentModel.PROP_USERNAME, Boolean.TRUE));
        }
        return sortProps;
    }

    private Person getPersonWithProperties(String personId, List<String> include)
    {
        Person person = null;
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

            // Remove the temporary property used to help inline the person description content property.
            // It may be accessed from the person object (person.getDescription()).
            nodeProps.remove(Person.PROP_PERSON_DESCRIPTION);

            // Expose properties
            if (include.contains(PARAM_INCLUDE_PROPERTIES))
            {
                // Note that custProps may be null.
                Map<String, Object> custProps = nodes.mapFromNodeProperties(nodeProps, new ArrayList<>(), new HashMap<>(), EXCLUDED_NS, EXCLUDED_PROPS);
                person.setProperties(custProps);
            }
            if (include.contains(PARAM_INCLUDE_ASPECTNAMES))
            {
                // Expose aspect names
                Set<QName> aspects = nodeService.getAspects(personNode);
                person.setAspectNames(nodes.mapFromNodeAspects(aspects, EXCLUDED_NS, EXCLUDED_ASPECTS));
            }
            if (include.contains(PARAM_INCLUDE_CAPABILITIES))
            {
                // Expose capabilities
                Map<String, Boolean> capabilities = new HashMap<>(3);
                capabilities.put("isAdmin", isAdminAuthority(personId));
                capabilities.put("isGuest", isGuestAuthority(personId));
                capabilities.put("isMutable", isMutableAuthority(personId));
                person.setCapabilities(capabilities);               
            }
            
            // get avatar information
            if (hasAvatar(personNode))
            {
                try
                {
                    NodeRef avatar = getAvatar(personId);
                    person.setAvatarId(avatar);
                }
                catch (EntityNotFoundException e)
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

        if (! isAdminAuthority())
        {
            // note: do an explict check for admin here (since personExists does not throw 403 unlike createPerson,
            // hence next block would cause 409 to be returned)
            throw new PermissionDeniedException();
        }

        // Unfortunately PersonService.createPerson(...) only throws an AlfrescoRuntimeException
        // rather than a more specific exception and does not use a message ID either, so there's
        // no sensible way to know that it was thrown due to the user already existing - hence this check here.
        if (personService.personExists(person.getUserName()))
        {
            throw new ConstraintViolatedException("Person '" + person.getUserName() + "' already exists.");
        }

        // set enabled default value true
        if (person.isEnabled() == null)
        {
            person.setEnabled(true);
        }

        Map<QName, Serializable> props = person.toProperties();

		MutableAuthenticationService mas = (MutableAuthenticationService) authenticationService;
		mas.createAuthentication(person.getUserName(), person.getPassword().toCharArray());
		mas.setAuthenticationEnabled(person.getUserName(), person.isEnabled());

		// Add custom properties
		if (person.getProperties() != null)
		{
			Map<String, Object> customProps = person.getProperties();
			props.putAll(nodes.mapToNodeProperties(customProps));
		}
		
		NodeRef nodeRef = personService.createPerson(props);
		
		// Add custom aspects
		nodes.addCustomAspects(nodeRef, person.getAspectNames(), EXCLUDED_ASPECTS);
		
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
                if (description != null)
                {
                    ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_PERSONDESC, true);
                    writer.putContent(description);
                }
                else
                {
                    nodeService.setProperty(nodeRef, ContentModel.PROP_PERSONDESC, null);
                }
                return null;
            }
        });
    }

    private void validateCreatePersonData(Person person)
    {
        // Mandatory field checks first
        checkRequiredField("id", person.getUserName());
        checkRequiredField("firstName", person.getFirstName());
        checkRequiredField("email", person.getEmail());
        checkRequiredField("password", person.getPassword());

        validateUsername(person.getUserName());
        nodes.validateAspects(person.getAspectNames(), EXCLUDED_NS, EXCLUDED_ASPECTS);
        nodes.validateProperties(person.getProperties(), EXCLUDED_NS, EXCLUDED_PROPS);
    }

    private void validateUsername(String username)
    {
        if (username.length() > 100)
        {
            throw new InvalidArgumentException("Username exceeds max length of " + USERNAME_MAXLENGTH + " characters.");
        }

        for (char illegalCharacter : illegalCharacters)
        {
            if (username.indexOf(illegalCharacter) != -1)
            {
                throw new IllegalArgumentException("Username contains characters that are not permitted: "+username.charAt(username.indexOf(illegalCharacter)));
            }
        }

        for (String prefix : RESERVED_AUTHORITY_PREFIXES)
        {
            if (username.toUpperCase().startsWith(prefix))
            {
                throw new IllegalArgumentException("Username cannot start with the reserved prefix '"+prefix+"'.");
            }
        }
    }

    private void checkRequiredField(String fieldName, Object fieldValue)
	{
		if (fieldValue == null)
		{
			throw new InvalidArgumentException("Field '"+fieldName+"' is null, but is required.");
		}

        // belts-and-braces - note: should not see empty string (since converted to null via custom json deserializer)
        if ((fieldValue instanceof String) && ((String)fieldValue).isEmpty())
        {
            throw new InvalidArgumentException("Field '"+fieldName+"' is empty, but is required.");
        }
	}

    @Override
    public Person update(String personId, final Person person)
    {
        // Validate, perform -me- substitution and canonicalize the person ID.
        personId = validatePerson(personId);
        validateUpdatePersonData(person);

        // Check if user updating *their own* details or is an admin
        boolean isAdmin = checkCurrentUserOrAdmin(personId);

        final String personIdToUpdate = validatePerson(personId);
        final Map<QName, Serializable> properties = person.toProperties();

        // if requested, update password
        updatePassword(isAdmin, personIdToUpdate, person);

        if (person.isEnabled() != null)
        {
            if (isAdminAuthority(personIdToUpdate))
            {
                throw new PermissionDeniedException("Admin authority cannot be disabled.");
            }

            // note: if current user is not an admin then permission denied exception is thrown
            MutableAuthenticationService mutableAuthenticationService = (MutableAuthenticationService) authenticationService;
            mutableAuthenticationService.setAuthenticationEnabled(personIdToUpdate, person.isEnabled());
        }

		NodeRef personNodeRef = personService.getPerson(personIdToUpdate, false);
		if (person.wasSet(Person.PROP_PERSON_DESCRIPTION))
        {
			// Remove person description from saved properties
			properties.remove(ContentModel.PROP_PERSONDESC);

			// Custom save for person description.
            savePersonDescription(person.getDescription(), personNodeRef);
        }

        // Update custom aspects - do this *before* adding custom properties. The
        // addition of custom properties may result in the auto-addition of aspects
        // and we don't want to remove them during the update of explicitly specified aspects.
        nodes.updateCustomAspects(personNodeRef, person.getAspectNames(), EXCLUDED_ASPECTS);
        
		// Add custom properties
		if (person.getProperties() != null)
		{
			Map<String, Object> customProps = person.getProperties();
			properties.putAll(nodes.mapToNodeProperties(customProps));
		}

        // The person service only allows admin users to set the properties by default.
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                personService.setPersonProperties(personIdToUpdate, properties, false);
                return null;
            }
        });
		
        return getPerson(personId);
    }

    private boolean checkCurrentUserOrAdmin(String personId)
    {
        boolean isAdmin = isAdminAuthority();

        String currentUserId = AuthenticationUtil.getFullyAuthenticatedUser();
        if (!isAdmin && !currentUserId.equalsIgnoreCase(personId))
        {
            throw new PermissionDeniedException();
        }

        return isAdmin;
    }

    private void validateUpdatePersonData(Person person)
    {
        nodes.validateAspects(person.getAspectNames(), EXCLUDED_NS, EXCLUDED_ASPECTS);
        nodes.validateProperties(person.getProperties(), EXCLUDED_NS, EXCLUDED_PROPS);
        
        if (person.wasSet(ContentModel.PROP_FIRSTNAME))
        {
            checkRequiredField("firstName", person.getFirstName());
        }

        if (person.wasSet(ContentModel.PROP_EMAIL))
        {
            checkRequiredField("email", person.getEmail());
        }

        if (person.wasSet(ContentModel.PROP_ENABLED) && (person.isEnabled() == null))
        {
            throw new IllegalArgumentException("'enabled' field cannot be empty.");
        }

        if (person.wasSet(ContentModel.PROP_EMAIL_FEED_DISABLED) && (person.isEmailNotificationsEnabled() == null))
        {
            throw new IllegalArgumentException("'emailNotificationsEnabled' field cannot be empty.");
        }
    }

    private void updatePassword(boolean isAdmin, String personIdToUpdate, Person person)
    {
        MutableAuthenticationService mutableAuthenticationService = (MutableAuthenticationService) authenticationService;

        boolean isOldPassword = person.wasSet(Person.PROP_PERSON_OLDPASSWORD);
        boolean isPassword = person.wasSet(Person.PROP_PERSON_PASSWORD);

        if (isPassword || isOldPassword)
        {
            if (isOldPassword && ((person.getOldPassword() == null) || (person.getOldPassword().isEmpty())))
            {
                throw new IllegalArgumentException("'oldPassword' field cannot be empty.");
            }

            if (!isPassword || (person.getPassword() == null) || (person.getPassword().isEmpty()))
            {
                throw new IllegalArgumentException("password' field cannot be empty.");
            }

            char[] newPassword = person.getPassword().toCharArray();

            if (!isAdmin)
            {
                // Non-admin users can update their own password, but must provide their current password.
                if (!isOldPassword)
                {
                    throw new IllegalArgumentException("To change password, both 'oldPassword' and 'password' fields are required.");
                }

                char[] oldPassword = person.getOldPassword().toCharArray();
                try
                {
                    mutableAuthenticationService.updateAuthentication(personIdToUpdate, oldPassword, newPassword);
                }
                catch (AuthenticationException e)
                {
                    throw new PermissionDeniedException("Incorrect password.");
                }
            }
            else
            {
                // An admin user can update without knowing the original pass - but must know their own!
                // note: is it reasonable to silently ignore oldPassword if supplied ?

                mutableAuthenticationService.setAuthentication(personIdToUpdate, newPassword);
            }
        }
    }

    private boolean isAdminAuthority()
    {
        return authorityService.hasAdminAuthority();
    }

    private boolean isAdminAuthority(String authorityName)
    {
        return authorityService.isAdminAuthority(authorityName);
    }

    @Override
    public void requestPasswordReset(String userId, String client)
    {
        // Validate the userId and the client
        checkRequiredField("userId", userId);
        checkRequiredField("client", client);

        // This is an un-authenticated API call so we wrap it to run as System
        AuthenticationUtil.runAsSystem(() -> {
            try
            {
                resetPasswordService.requestReset(userId, client);
            }
            catch (ResetPasswordWorkflowInvalidUserException ex)
            {
                // we don't throw an exception.
                // For security reason (prevent the attackers to determine that userId exists in the system or not),
                // the endpoint returns a 202 response if the userId does not exist or
                // if the user is disabled by an Administrator.
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("Invalid user. " + ex.getMessage());
                }
            }

            return null;
        });
    }

    @Override
    public void resetPassword(String personId, final PasswordReset passwordReset)
    {
        checkResetPasswordData(passwordReset);
        checkRequiredField("personId", personId);

        ResetPasswordDetails resetDetails = new ResetPasswordDetails()
                    .setUserId(personId)
                    .setPassword(passwordReset.getPassword())
                    .setWorkflowId(passwordReset.getId())
                    .setWorkflowKey(passwordReset.getKey());
        try
        {
            // This is an un-authenticated API call so we wrap it to run as System
            AuthenticationUtil.runAsSystem(() -> {
                resetPasswordService.initiateResetPassword(resetDetails);

                return null;
            });

        }
        catch (ResetPasswordWorkflowException ex)
        {
            // we don't throw an exception.
            // For security reason, the endpoint returns a 202 response
            // See APPSREPO-35 acceptance criteria
            if (LOGGER.isWarnEnabled())
            {
                LOGGER.warn(ex.getMessage());
            }
        }
    }

    private void checkResetPasswordData(PasswordReset data)
    {
        checkRequiredField("password", data.getPassword());
        checkRequiredField("id", data.getId());
        checkRequiredField("key", data.getKey());
    }

    private boolean isGuestAuthority(String authorityName)
    {
        return authorityService.isGuestAuthority(authorityName);
    }

    private boolean isMutableAuthority(String authorityName)
    {
        MutableAuthenticationService mutableAuthenticationService = (MutableAuthenticationService) authenticationService;
        // Check whether the account is mutable according to the authentication service
        if (!mutableAuthenticationService.isAuthenticationMutable(authorityName))
        {
            return false;
        }
        // Only allow non-admin users to mutate their own accounts
        String currentUser = mutableAuthenticationService.getCurrentUserName();
        if (currentUser.equals(authorityName) || authorityService.isAdminAuthority(currentUser))
        {
            return true;
        }
        return false;
    }
}
