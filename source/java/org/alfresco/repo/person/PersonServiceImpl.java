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
package org.alfresco.repo.person;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO9075;
import org.alfresco.util.PropertyMap;

/**
 * Provides the implementation for the Person Service
 * 
 * @author Glen Johnson
 */
public class PersonServiceImpl implements PersonService
{
    private NodeService nodeService;
    private SearchService searchService;
    
    // Random number generator to create random number for User Name
    private static final Random RANDOMIZER = new Random();
    
    // Max number for User Name
    private static final int MAX_USER_NAME_INT = 999999;
    private static final int USER_NAME_LENGTH = new Integer(MAX_USER_NAME_INT).toString().length(); 
    
    public static final StoreRef PERSON_DM_STORE = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "PersonStore");
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }
    
    public PersonDetails createPerson(String userName, String title, String firstName, String lastName, 
            String organisation, String jobTitle, String emailAddress, String bio, URL avatarUrl)
    {
        // If User Name not specified then create User Name from random integer between 0 and 999999
        if (userName == null)
        {
            userName = new Integer(RANDOMIZER.nextInt(MAX_USER_NAME_INT + 1)).toString();
            for (int i=userName.length(); i < USER_NAME_LENGTH;  i++)
            {
                userName = "0" + userName;
            }
        }
        
        // TODO glen.johnson@alfresco.com Check that User Name does not already exist
        
        // TODO glen.johnson@alfresco.com set value for personParent node reference
        NodeRef personParent = null;
        
        // TODO glen.johnson@alfresco.com generate password and create account per person 
        
        // Create the person node
        PropertyMap properties = new PropertyMap(9);
        properties.put(ContentModel.PROP_NAME, userName);
        properties.put(PersonModel.PROP_PERSON_TITLE, title);
        properties.put(PersonModel.PROP_PERSON_FIRST_NAME, firstName);
        properties.put(PersonModel.PROP_PERSON_LAST_NAME, lastName);
        properties.put(PersonModel.PROP_PERSON_ORGANISATION, organisation);
        properties.put(PersonModel.PROP_PERSON_JOB_TITLE, jobTitle);
        properties.put(PersonModel.PROP_PERSON_EMAIL, emailAddress);
        properties.put(PersonModel.PROP_PERSON_BIO, bio);
        properties.put(PersonModel.PROP_PERSON_AVATAR_URL, avatarUrl);
        NodeRef personNodeRef = this.nodeService.createNode(
                personParent, 
                ContentModel.ASSOC_CONTAINS, 
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, userName), 
                PersonModel.TYPE_PERSON,
                properties).getChildRef();
        
       // Return created person details
       PersonDetails personDetails = new PersonDetails(userName, title, firstName, lastName, organisation,
               jobTitle, emailAddress, bio, avatarUrl);
       return personDetails;
    }
    
    public List<PersonDetails> listPeople(String userNameFilter, String personPresetFilter)
    {
        // TODO glen.johnson@alfresco.com Look into how to do this and implement this method 
        List<PersonDetails> people = new ArrayList<PersonDetails>();
        return people;
    }
  
    private PersonDetails createPersonDetails(NodeRef personNodeRef)
    {
        // Get the properties
        Map<QName, Serializable> properties = this.nodeService.getProperties(personNodeRef);
        String userName = (String)properties.get(ContentModel.PROP_NAME);
        String title = (String)properties.get(PersonModel.PROP_PERSON_TITLE);
        String firstName = (String)properties.get(PersonModel.PROP_PERSON_FIRST_NAME);
        String lastName = (String)properties.get(PersonModel.PROP_PERSON_LAST_NAME);
        String organisation = (String)properties.get(PersonModel.PROP_PERSON_ORGANISATION);
        String jobTitle = (String)properties.get(PersonModel.PROP_PERSON_JOB_TITLE);
        String emailAddress = (String)properties.get(PersonModel.PROP_PERSON_EMAIL);
        String bio = (String)properties.get(PersonModel.PROP_PERSON_BIO);
        URL avatarUrl;
        try
        {
            avatarUrl = new URL((String)properties.get(PersonModel.PROP_PERSON_AVATAR_URL));
        }
        catch (Exception e)
        // TODO glen.johnson@alfresco.com Throw this exception with properly defined msg ID
        {
            throw new AlfrescoRuntimeException("MALFORMED_PERSON_AVATAR_URL", e);
        }
        
        // Create and return the person details
        PersonDetails personDetails = new PersonDetails(userName, title, firstName, lastName, organisation,
            jobTitle, emailAddress, bio, avatarUrl);
        return personDetails;
    }   
    
    /**
     * @see org.alfresco.repo.person.PersonService#getPerson(java.lang.String)
     */
    public PersonDetails getPerson(String userName)
    {
        PersonDetails result = null;
        
        // Get the person node
        NodeRef personNodeRef = getPersonNodeRef(userName);
        if (personNodeRef != null)
        {
            // Create the person details
            result = createPersonDetails(personNodeRef);
        }
        
        // Return the person details
        return result;
    }
    
    private NodeRef getPersonNodeRef(String userName)
    {
        NodeRef result = null;
        ResultSet resultSet = this.searchService.query(
                PERSON_DM_STORE, SearchService.LANGUAGE_LUCENE, "PATH:\"cm:people/cm:" + ISO9075.encode(userName) + "\"");
        if (resultSet.length() == 1)
        {
            result = resultSet.getNodeRef(0);
        }
        return result;
    }

    public void updatePerson(PersonDetails personDetails)
    {
        NodeRef personNodeRef = getPersonNodeRef(personDetails.getUserName());
        if (personNodeRef == null)
        {
            throw new AlfrescoRuntimeException("Can not update person " + personDetails.getUserName() + " because he/she does not exist.");
        }
        
        // Note: the user name cannot be updated
        
        // Update the properties of the person
        Map<QName, Serializable> properties = this.nodeService.getProperties(personNodeRef);
        properties.put(ContentModel.PROP_NAME, personDetails.getUserName());
        properties.put(PersonModel.PROP_PERSON_TITLE, personDetails.getTitle());
        properties.put(PersonModel.PROP_PERSON_FIRST_NAME, personDetails.getFirstName());
        properties.put(PersonModel.PROP_PERSON_LAST_NAME, personDetails.getLastName());
        properties.put(PersonModel.PROP_PERSON_ORGANISATION, personDetails.getOrganisation());
        properties.put(PersonModel.PROP_PERSON_JOB_TITLE, personDetails.getJobTitle());
        properties.put(PersonModel.PROP_PERSON_EMAIL, personDetails.getEmailAddress());
        properties.put(PersonModel.PROP_PERSON_BIO, personDetails.getBio());
        properties.put(PersonModel.PROP_PERSON_AVATAR_URL, personDetails.getAvatarUrl());
        this.nodeService.setProperties(personNodeRef, properties);
    }
    
    /**
     * @see org.alfresco.repo.person.PersonService#deletePerson(java.lang.String)
     */
    public void deletePerson(String userName)
    {
        NodeRef personNodeRef = getPersonNodeRef(userName);
        if (personNodeRef == null)
        {
            throw new AlfrescoRuntimeException("Can not delete person with User Name: " + userName + " because he/she does not exist.");
        }
        
        this.nodeService.deleteNode(personNodeRef);        
    }
}
