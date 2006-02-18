/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.service.cmr.security;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

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
public interface PersonService
{
    /**
     * Get a person by userName. The person is store in the repository. The
     * person may be created as a side effect of this call, depending on
     * the setting to {@link #setCreateMissingPeople(boolean) create missing people or not}.
     * 
     * @param userName - the userName key to find the person
     * @return Returns the person node, either existing or new
     * @throws NoSuchPersonException if the user doesn't exist and could not be created automatically
     * 
     * @see #setCreateMissingPeople(boolean)
     * @see #createMissingPeople()
     */
    public NodeRef getPerson(String userName);

    /**
     * Check if a person exists.
     * 
     * @param userName the user name
     * @return Returns true if the user exists, otherwise false
     */
    public boolean personExists(String userName);
    
    /**
     * Does this service create people on demand if they are missing. If this is
     * true, a call to getPerson() will create a person if they are missing.
     * 
     * @return true if people are created on demand and false otherwise.
     */
    public boolean createMissingPeople();

    /**
     * Set if missing people should be created.
     * 
     * @param createMissing set to true to create people
     * 
     * @see #getPerson(String)
     */
    public void setCreateMissingPeople(boolean createMissing);
    
    /**
     * Get the list of properties that are mutable. Some service may only allow
     * a limited list of properties to be changed. This may be those persisted
     * in the repository or those that can be changed in some other
     * implementation such as LDAP.
     * 
     * @return A set of QNames that identify properties that can be changed
     */
    public Set<QName> getMutableProperties();

    /**
     * Set the properties on a person - some of these may be persisted in
     * different locations.
     * 
     * @param userName - the user for which the properties should be set.
     * @param properties - the map of properties to set (as the NodeService)
     */
    public void setPersonProperties(String userName, Map<QName, Serializable> properties);

    /**
     * Can this service create, delete and update person information?
     * 
     * @return true if this service allows mutation to people.
     */
    public boolean isMutable();

    /**
     * Create a new person with the given properties.
     * The userName is one of the properties.
     * Users with duplicate userNames are not allowed.
     * 
     * @param properties
     * @return
     */
    public NodeRef createPerson(Map<QName, Serializable> properties);

    /**
     * Delete the person identified by the given user name.
     * 
     * @param userName
     */
    public void deletePerson(String userName);
    
    /**
     * Get all the people we know about.
     * 
     * @return a set of people in no specific order. 
     */
    public Set<NodeRef> getAllPeople();
    
    /**
     * Return the container that stores people.
     * 
     * @return
     */
    public NodeRef getPeopleContainer();
    
    /**
     * Are user names case sensitive?
     * 
     * @return
     */
    public boolean getUserNamesAreCaseSensitive();
}
