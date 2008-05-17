package org.alfresco.repo.person;

import java.net.URL;
import java.util.List;

/**
 * Person service API.
 * <p>
 * This service API is designed to support the public facing Person API
 * 
 * @author Glen Johnson
 */
public interface PersonService
{    
    /**
     * Create a new person.
     *
     * @param userName      unique identifier for person
     * @param title         person's title
     * @param firstName     person's first name
     * @param lastName      person's last name
     * @param organisation  organisation to whom the person belongs
     * @param jobTitle      person's job title
     * @param emailAddress  person's email address
     * @param bio           person's biography
     * @param avatarUrl     person's avatar URL 
     */
    PersonDetails createPerson(String userName, String title, String firstName, String lastName, 
            String organisation, String jobTitle, String emailAddress, String bio, URL avatarUrl);
    
    /**
     * List the available people.  This list can optionally be filtered by User Name and/or preset person filter name.
     * 
     * @param userNameFilter        user name filter
     * @param personPresetFilter    person preset filter
     * @return List<PersonDetails>  list of people
     */
    List<PersonDetails> listPeople(String userNameFilter, String personPresetFilter);
    
    /**
     * Gets person's details based on User Name.
     * <p>
     * Returns null if the User Name cannot be found.
     * 
     * @param userName      the person's User Name
     * @return details      the person's details
     */
    PersonDetails getPerson(String userName);
    
    /**
     * Update a person's details
     * <P>
     * Note that the User Name cannot be updated once the person has been created.
     * 
     * @param details  person's details
     */
    void updatePerson(PersonDetails personDetails);
    
    /**
     * Delete the person.
     * 
     * @param userName     person's User Name
     */
    void deletePerson(String userName);        
}
