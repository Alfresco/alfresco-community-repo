/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.service.cmr.security;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.security.permissions.PermissionCheckValue;
import org.alfresco.service.Auditable;
import org.alfresco.service.NotAuditable;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

/**
 * This service encapsulates the management of people and groups.
 * <p>
 * <p>
 * People and groups may be managed entirely in the repository or entirely in
 * some other implementation such as LDAP or via NTLM. Some properties may in
 * the repository and some in another store. Individual properties may or may
 * not be mutable.
 * <p>
 * 
 * @author Andy Hind
 */
@AlfrescoPublicApi
public interface PersonService
{
    /**
     * Get a person by userName. The person is store in the repository. The
     * person may be created as a side effect of this call, depending on the
     * setting of
     * {@link #setCreateMissingPeople(boolean) to create missing people or not}.
     * The home folder will also be created as a side effect if it does not exist.
     * 
     * @param userName -
     *            the userName key to find the person
     * @return Returns the person node, either existing or new
     * @throws NoSuchPersonException
     *             if the user doesn't exist and could not be created
     *             automatically
     * 
     * @see #setCreateMissingPeople(boolean)
     * @see #createMissingPeople()
     */
    @Auditable(parameters = {"userName"})
    public NodeRef getPerson(String userName);
    
    /**
     * Get a person by userName. The person is store in the repository. No missing
     * person objects will be created as a side effect of this call. If the person
     * is missing from the repository null will be returned.
     * 
     * @param userName -
     *            the userName key to find the person
     * @return Returns the existing person node, or null if does not exist.
     * 
     * @see #createMissingPeople()
     */
    @Auditable(parameters = {"userName"})
    public NodeRef getPersonOrNull(String userName);

    /**
     * Retrieve the person NodeRef for a {@code username}, optionally creating
     * the home folder if it does not exist and optionally creating the person
     * if they don't exist AND the PersonService is configured to allow the
     * creation of missing persons {@see #setCreateMissingPeople(boolean)}.
     * 
     * If not allowed to create missing persons and the person does not exist
     * a {@code NoSuchPersonException} exception will be thrown.
     * 
     * @param userName
     *            of the person NodeRef to retrieve
     * @param autoCreateHomeFolderAndMissingPersonIfAllowed
     *            If the person exits:
     *               should we create the home folder if it does not exist?
     *            If the person exists AND the creation of missing persons is allowed
     *               should we create both the person and home folder.
     * @return NodeRef of the person as specified by the username
     * @throws NoSuchPersonException
     *             if the person doesn't exist and can't be created
     */
    @Auditable(parameters = {"userName", "autoCreate"})
    public NodeRef getPerson(final String userName, final boolean autoCreateHomeFolderAndMissingPersonIfAllowed);
    
    /**
     * Retrieve the person info for an existing {@code person NodeRef}
     * 
     * @param person NodeRef
     * @return PersonInfo (username, firstname, lastname)
     * @throws NoSuchPersonException if the person doesn't exist
     */
    @Auditable(parameters = {"personRef"})
    public PersonInfo getPerson(NodeRef personRef) throws NoSuchPersonException;
    
    /**
     * Check if a person exists.
     * 
     * @param userName
     *            the user name
     * @return Returns true if the user exists, otherwise false
     */
    @Auditable(parameters = {"userName"})
    public boolean personExists(String userName);

    /**
     * Does this service create people on demand if they are missing. If this is
     * true, a call to getPerson() will create a person if they are missing.
     * 
     * @return true if people are created on demand and false otherwise.
     */
    @Auditable
    public boolean createMissingPeople();

    /**
     * Set if missing people should be created.
     * 
     * @param createMissing
     *            set to true to create people
     * 
     * @see #getPerson(String)
     */
    @Auditable(parameters = {"createMissing"})
    public void setCreateMissingPeople(boolean createMissing);

    /**
     * Get the list of properties that are mutable. Some service may only allow
     * a limited list of properties to be changed. This may be those persisted
     * in the repository or those that can be changed in some other
     * implementation such as LDAP.
     * 
     * @return A set of QNames that identify properties that can be changed
     */
    @Auditable
    public Set<QName> getMutableProperties();

    /**
     * Set the properties on a person - some of these may be persisted in
     * different locations - <b>the home folder is created if it doesn't exist</b>
     * 
     * @param userName -
     *            the user for which the properties should be set.
     * @param properties -
     *            the map of properties to set (as the NodeService)
     */
    @Auditable(parameters = {"userName", "properties"})
    public void setPersonProperties(String userName, Map<QName, Serializable> properties);


    /**
     * Set the properties on a person - some of these may be persisted in different locations.
     * 
     * @param userName
     *            - the user for which the properties should be set.
     * @param properties
     *            - the map of properties to set (as the NodeService)
     * @param autoCreateHomeFolder
     *            should we auto-create the home folder if it doesn't exist.
     */
    @Auditable(parameters = {"userName", "properties", "autoCreate"})
    public void setPersonProperties(String userName, Map<QName, Serializable> properties, boolean autoCreateHomeFolder);
    
    /**
     * Can this service create, delete and update person information?
     * 
     * @return true if this service allows mutation to people.
     */
    @Auditable
    public boolean isMutable();

    /**
     * Create a new person with the given properties. The userName is one of the
     * properties. Users with duplicate userNames are not allowed.
     * 
     * @param properties
     * @return
     */
    @Auditable(parameters = {"properties"})
    public NodeRef createPerson(Map<QName, Serializable> properties);

    /**
     * Create a new person with the given properties, recording them against the given zone name (usually identifying an
     * external user registry from which the details were obtained). The userName is one of the properties. Users with
     * duplicate userNames are not allowed.
     * 
     * @param properties
     *            the properties
     * @param zones
     *            a set if zones including the identifier for the external user registry owning the person information, or <code>null</code> or an empty set
     * @return the node ref
     */
    @Auditable(parameters = {"properties", "zones"})
    public NodeRef createPerson(Map<QName, Serializable> properties, Set<String> zones);

    /**
     * Notifies a user by email that their account has been created, and the details of it.
     * Normally called after {@link #createPerson(Map)} or {@link #createPerson(Map, Set)}
     *  where email notifications are required.
     * 
     * @param userName
     *            of the person to notify
     * @param password
     *            of the person to notify           
     * @throws NoSuchPersonException
     *             if the person doesn't exist
     */
    @Auditable(parameters = {"userName"})
    public void notifyPerson(final String userName, final String password);
    
    /**
     * Delete the person identified by the given user name.
     * 
     * @param userName
     */
    @Auditable(parameters = {"userName"})
    public void deletePerson(String userName);
    
    /**
     * Delete the person identified by the given ref.
     * 
     * @param personRef
     */
    @Auditable(parameters = {"personRef"})
    public void deletePerson(NodeRef personRef);

    /**
     * Delete the person identified by the given ref, and optionally delete
     * the associated authentication, if one.
     * 
     * @param personRef
     * @param deleteAuthentication
     */
    @Auditable(parameters = {"personRef", "deleteAuthentication"})
    public void deletePerson(NodeRef personRef, boolean deleteAuthentication);
    
    /**
     * Get all the people we know about.
     * 
     * @return a set of people in no specific order.
     * 
     * @deprecated see {@link #getPeople(List, boolean, List, PagingRequest)}
     */
    @Auditable
    public Set<NodeRef> getAllPeople();
    
    /**
     * Data pojo to carry common person information
     *
     * @author janv
     * @since 4.0
     */
    @AlfrescoPublicApi
    public class PersonInfo implements PermissionCheckValue
    {
        private final NodeRef nodeRef;
        private final String userName;
        private final String firstName;
        private final String lastName;
        
        public PersonInfo(NodeRef nodeRef, String userName, String firstName, String lastName)
        {
            this.nodeRef = nodeRef;
            this.userName = userName;
            this.firstName = firstName;
            this.lastName = lastName;
        }
        
        @Override
        public NodeRef getNodeRef()
        {
            return nodeRef;
        }
        
        public String getUserName()
        {
            return userName;
        }
        
        public String getFirstName()
        {
            return firstName;
        }
        
        public String getLastName()
        {
            return lastName;
        }
    }
    
    /**
     * Get paged list of people optionally filtered and/or sorted
     * 
     * Note: the pattern is applied to filter props (0 to 3) as startsWithIgnoreCase, which are OR'ed together, for example: cm:userName or cm:firstName or cm:lastName
     * 
     * @param pattern         pattern to apply to filter props - "startsWith" and "ignoreCase"
     * @param filterProps     list of filter properties (these are OR'ed)
     * @param sortProps       sort property, eg. cm:username ascending
     * @param pagingRequest   skip, max + optional query execution id
     * @return
     * 
     * @author janv
     * @since 4.1.2
     */
    @Auditable(parameters = {"pattern", "filterProps", "sortProps", "pagingRequest"})
    public PagingResults<PersonInfo> getPeople(String pattern, List<QName> filterProps, List<Pair<QName, Boolean>> sortProps, PagingRequest pagingRequest);
    
    
    /**
     * Get paged list of people optionally filtered and/or sorted
     *
     * @param filterProps       list of filter properties (with "startsWith" values), eg. cm:username "al" might match "alex", "alice", ...
     * @param filterIgnoreCase  true to ignore case when filtering, false to be case-sensitive when filtering
     * @param sortProps         sort property, eg. cm:username ascending
     * @param pagingRequest     skip, max + optional query execution id
     * 
     * @author janv
     * @since 4.0
     * @deprecated see getPeople(String pattern, List<QName> filterProps, List<Pair<QName, Boolean>> sortProps, PagingRequest pagingRequest)
     */
    @Auditable(parameters = {"stringPropFilters", "filterIgnoreCase", "sortProps", "pagingRequest"})
    public PagingResults<PersonInfo> getPeople(List<Pair<QName,String>> stringPropFilters, boolean filterIgnoreCase, List<Pair<QName, Boolean>> sortProps, PagingRequest pagingRequest);
    
    /**
     * Get paged list of people optionally filtered and/or sorted
     *
     * @param filterProps            list of filter properties (with "startsWith" values), eg. cm:username "al" might match "alex", "alice", ...
     * @param filterIgnoreCase       true to ignore case when filtering, false to be case-sensitive when filtering
     * @param inclusiveAspects       if set, filter out any people that don't have one of these aspects
     * @param exclusiveAspects       if set, filter out any people that do have one of these aspects
     * @param includeAdministrators  true to include administrators in the results.
     * @param sortProps              sort property, eg. cm:username ascending
     * @param pagingRequest          skip, max + optional query execution id
     * 
     * @author Alex Miller
     * @since 4.0
     */
    @Auditable(parameters = {"stringPropFilters", "filterIgnoreCase", "inclusiveAspect", "exclusiveAspects", "sortProps", "pagingRequest"})
    public PagingResults<PersonInfo> getPeople(String pattern, List<QName> filterStringProps, Set<QName> inclusiveAspects, Set<QName> exclusiveAspects, boolean includeAdministraotrs, List<Pair<QName, Boolean>> sortProps, PagingRequest pagingRequest);
    /**
     * Get people filtered by the given property name/value pair.
     * <p/>
     * In order to get paging, use {@link #getPeople(List, boolean, List, PagingRequest)}
     * 
     * @param propertyKey       property key of property to filter people by 
     * @param propertyValue     property value of property to filter people by
     * @param count             the number of results to retrieve, up to a maximum of 1000
     * @return                  people filtered by the given property name/value pair
     * 
     * @see #getPeople(List, boolean, List, PagingRequest)
     */
    @Auditable
    public Set<NodeRef> getPeopleFilteredByProperty(QName propertyKey, Serializable propertyValue, int count);

    /**
     * Return the container that stores people.
     * 
     * @return
     */
    @Auditable
    public NodeRef getPeopleContainer();
    
    /**
     * Are user names case sensitive?
     * 
     * @return
     */
    @Auditable
    public boolean getUserNamesAreCaseSensitive();

    /**
     * Given the case sensitive user name find the approriate identifier from the person service.
     * If the system is case sensitive it will return the same string.
     * If case insentive it will return the common object.
     * If the user does not exist it will return null;
     * 
     * @param caseSensitiveUserName
     * @return
     */
    @NotAuditable
    public String getUserIdentifier(String caseSensitiveUserName);

    /**
     * Counts the number of persons registered with the system.
     * 
     * @return
     */
    @NotAuditable
    public int countPeople();
    
    /**
     * Is the specified user, enabled
     * @throws NoSuchPersonException
     *             if the user doesn't exist 
     * @return true = enabled.
     */
    @NotAuditable
    public boolean isEnabled(final String userName);
}
