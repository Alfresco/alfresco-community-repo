/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.jscript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.UserNameGenerator;
import org.alfresco.repo.security.authority.AuthorityDAO;
import org.alfresco.repo.security.person.PersonServiceImpl;
import org.alfresco.repo.security.sync.UserRegistrySynchronizer;
import org.alfresco.repo.tenant.TenantDomainMismatchException;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.alfresco.service.cmr.usage.ContentUsageService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyMap;
import org.alfresco.util.ValueDerivingMapFactory;
import org.alfresco.util.ValueDerivingMapFactory.ValueDeriver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * Scripted People service for describing and executing actions against People & Groups.
 * 
 * @author davidc
 * @author kevinr
 */
public final class People extends BaseScopableProcessorExtension implements InitializingBean
{
    private static Log logger = LogFactory.getLog(People.class);
    
    /** Repository Service Registry */
    private ServiceRegistry services;
    private AuthorityDAO authorityDAO;
    private AuthorityService authorityService;
    private PersonService personService;
    private NamespaceService namespaceService;
    private MutableAuthenticationService authenticationService;
    private ContentUsageService contentUsageService;
    private TenantService tenantService;
    private UserNameGenerator usernameGenerator;
    private UserRegistrySynchronizer userRegistrySynchronizer;
    private StoreRef storeRef;
    private ValueDerivingMapFactory<ScriptNode, String, Boolean> valueDerivingMapFactory;
    private int numRetries = 10;

    
    public void afterPropertiesSet() throws Exception
    {
        Map <String, ValueDeriver<ScriptNode, Boolean>> capabilityTesters = new HashMap<String, ValueDeriver<ScriptNode, Boolean>>(5);
        capabilityTesters.put("isAdmin", new ValueDeriver<ScriptNode, Boolean>()
        {
            public Boolean deriveValue(ScriptNode source)
            {
                return isAdmin(source);
            }
        });
        capabilityTesters.put("isGuest", new ValueDeriver<ScriptNode, Boolean>()
        {
            public Boolean deriveValue(ScriptNode source)
            {
                return isGuest(source);
            }
        });
        capabilityTesters.put("isMutable", new ValueDeriver<ScriptNode, Boolean>()
        {
            public Boolean deriveValue(ScriptNode source)
            {
                // Check whether the account is mutable according to the authentication service
                String sourceUser = (String) source.getProperties().get(ContentModel.PROP_USERNAME);
                if (!authenticationService.isAuthenticationMutable(sourceUser))
                {
                    return false;
                }
                // Only allow non-admin users to mutate their own accounts
                String currentUser = authenticationService.getCurrentUserName();
                if (currentUser.equals(sourceUser) || authorityService.isAdminAuthority(currentUser))
                {
                    return true;
                }
                return false;
            }
        });
        this.valueDerivingMapFactory = new ValueDerivingMapFactory<ScriptNode, String, Boolean>(capabilityTesters);
    }
    
    /**
     * Set the default store reference
     * 
     * @param   storeRef the default store reference
     */
    public void setStoreUrl(String storeRef)
    {
        // ensure this is not set again by a script instance
        if (this.storeRef != null)
        {
            throw new IllegalStateException("Default store URL can only be set once.");
        }
        this.storeRef = new StoreRef(storeRef);
    }    

    /**
     * Sets the authentication service.
     * 
     * @param authenticationService
     *            the authentication service
     */
    public void setAuthenticationService(MutableAuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }

    /**
     * Set the service registry
     * 
     * @param serviceRegistry	the service registry
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
    	this.services = serviceRegistry;
    }

    /**
     * Set the authority DAO
     *
     * @param authorityDAO  authority dao
     */
    public void setAuthorityDAO(AuthorityDAO authorityDAO)
    {
        this.authorityDAO = authorityDAO;
    }
    
    /**
     * Set the authority service
     * 
     * @param authorityService The authorityService to set.
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }
    
    /**
     * Set the person service
     * 
     * @param personService The personService to set.
     */
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }
    
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    /**
     * @param contentUsageService   the ContentUsageService to set
     */
    public void setContentUsageService(ContentUsageService contentUsageService)
    {
        this.contentUsageService = contentUsageService;
    }
    
    /**
     * @param tenantService   the tenantService to set
     */
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
    
    /**
     * Set the user name generator service
     * 
     * @param userNameGenerator the user name generator 
     */
    public void setUserNameGenerator(UserNameGenerator userNameGenerator)
    {
        this.usernameGenerator = userNameGenerator;
    }
    
    /**
     * Set the UserRegistrySynchronizer
     * 
     * @param userRegistrySynchronizer
     */
    public void setUserRegistrySynchronizer(UserRegistrySynchronizer userRegistrySynchronizer)
    {
        this.userRegistrySynchronizer = userRegistrySynchronizer;
    }
    
    /**
     * Delete a Person with the given username
     * 
     * @param username the username of the person to delete
     */
    public void deletePerson(String username)
    {
        personService.deletePerson(username);
    }
    
    /**
     * Create a Person with an optionally generated user name. 
     * This version doesn't notify them.
     * 
     * @param userName userName or null for a generated user name
     * @param firstName firstName
     * @param lastName lastName
     * @param emailAddress emailAddress
     * @param password if not null creates a new authenticator with the given password.
     * @param setAccountEnabled
     *            set to 'true' to create enabled user account, or 'false' to
     *            create disabled user account for created person.
     * @return the person node (type cm:person) created or null if the person
     *         could not be created
     */
    public ScriptNode createPerson(String userName, String firstName, String lastName, String emailAddress, String password, boolean setAccountEnabled)
    {
        return createPerson(userName, firstName, lastName, emailAddress, password, setAccountEnabled, false);
    }
    
    /**
     * Create a Person with an optionally generated user name
     * 
     * @param userName userName or null for a generated user name
     * @param firstName firstName
     * @param lastName lastName
     * @param emailAddress emailAddress
     * @param password if not null creates a new authenticator with the given password.
     * @param setAccountEnabled
     *            set to 'true' to create enabled user account, or 'false' to
     *            create disabled user account for created person.
     * @param notifyByEmail
     *            set to 'true' to have the new user emailed to let them know
     *            their account details. Only applies if a username and 
     *            password were supplied.
     * @return the person node (type cm:person) created or null if the person
     *         could not be created
     */
    public ScriptNode createPerson(String userName, String firstName, String lastName, String emailAddress, 
            String password, boolean setAccountEnabled, boolean notifyByEmail)
    {
    	ParameterCheck.mandatory("firstName", firstName);
    	ParameterCheck.mandatory("emailAddress", emailAddress);
        
        ScriptNode person = null;
        
        // generate user name if not supplied
        if (userName == null)
        {
            for (int i=0; i < numRetries; i++)
            {
            	userName = usernameGenerator.generateUserName(firstName, lastName, emailAddress, i);
            	
            	// create person if user name does not already exist
            	if (!personService.personExists(userName))
            	{
            	    break;
            	}
            }
        }
        
        if (userName != null)
        {
            try
            {
                userName = PersonServiceImpl.updateUsernameForTenancy(userName, tenantService);
            }
            catch (TenantDomainMismatchException re)
            {
                throw new AuthenticationException("User must belong to same domain as admin: " + re.getTenantA());
            }
            
            person = createPerson(userName, firstName, lastName, emailAddress);
            
    		if (person != null && password != null)
    		{   			
    			// create account for person with the userName and password
    		    authenticationService.createAuthentication(userName, password.toCharArray());
    		    authenticationService.setAuthenticationEnabled(userName, setAccountEnabled);
    			
    			person.save();
    			
    			if(notifyByEmail)
    			{
    			    personService.notifyPerson(userName, password);
    			}
    		}
        }
        
        return person;
    }

    /**
     * Enable user account. Can only be called by an Admin authority.
     * 
     * @param userName      user name for which to enable user account
     */
    public void enableAccount(String userName)
    {
        if (this.authorityService.isAdminAuthority(AuthenticationUtil.getFullyAuthenticatedUser()))
        {
            this.authenticationService.setAuthenticationEnabled(userName, true);
        }
    }
    
    /**
     * Disable user account. Can only be called by an Admin authority.
     * 
     * @param userName      user name for which to disable user account
     */
    public void disableAccount(String userName)
    {
        if (this.authorityService.isAdminAuthority(AuthenticationUtil.getFullyAuthenticatedUser()))
        {
            this.authenticationService.setAuthenticationEnabled(userName, false);
        }
    }
    
    /**
     * Return true if the specified user account is enabled.
     *  
     * @param userName      user name to test account
     * 
     * @return true if account enabled, false if disabled
     */
    public boolean isAccountEnabled(String userName)
    {
        return this.authenticationService.getAuthenticationEnabled(userName);
    }
    
    /**
     * Change the password for the currently logged in user.
     * Old password must be supplied.
     *  
     * @param oldPassword       Old user password
     * @param newPassword       New user password
     */
    public void changePassword(String oldPassword, String newPassword)
    {
        ParameterCheck.mandatoryString("oldPassword", oldPassword);
        ParameterCheck.mandatoryString("newPassword", newPassword);
        
        this.services.getAuthenticationService().updateAuthentication(
                AuthenticationUtil.getFullyAuthenticatedUser(), oldPassword.toCharArray(), newPassword.toCharArray());
    }
    
    /**
     * Set a password for the given user. Note that only an administrator
     * can perform this action, otherwise it will be ignored.
     * 
     * @param userName          Username to change password for
     * @param password          Password to set
     */
    public void setPassword(String userName, String password)
    {
        ParameterCheck.mandatoryString("userName", userName);
        ParameterCheck.mandatoryString("password", password);
        
        MutableAuthenticationService authService = this.services.getAuthenticationService();
        if (this.authorityService.hasAdminAuthority() && (userName.equalsIgnoreCase(authService.getCurrentUserName()) == false))
        {
            authService.setAuthentication(userName, password.toCharArray());
        }
    }

    /**
     * Create a Person with the given user name
     * 
     * @param userName the user name of the person to create
     * @return the person node (type cm:person) created or null if the user name already exists
     */
    public ScriptNode createPerson(String userName)
    {
        ParameterCheck.mandatoryString("userName", userName);
        
        ScriptNode person = null;
        
        PropertyMap properties = new PropertyMap();
        properties.put(ContentModel.PROP_USERNAME, userName);
        
        if (!personService.personExists(userName))
        {
            NodeRef personRef = personService.createPerson(properties); 
            person = new ScriptNode(personRef, services, getScope()); 
        }
        
        return person;
    }
    
    /**
     * Create a Person with the given user name, firstName, lastName and emailAddress
     * 
     * @param userName the user name of the person to create
     * @return the person node (type cm:person) created or null if the user name already exists
     */
    public ScriptNode createPerson(String userName, String firstName, String lastName, String emailAddress)
    {
        ParameterCheck.mandatoryString("userName", userName);
        ParameterCheck.mandatoryString("firstName", firstName);
        ParameterCheck.mandatoryString("emailAddress", emailAddress);
        
        ScriptNode person = null;
        
        PropertyMap properties = new PropertyMap();
        properties.put(ContentModel.PROP_USERNAME, userName);
        properties.put(ContentModel.PROP_FIRSTNAME, firstName);
        properties.put(ContentModel.PROP_LASTNAME, lastName);
        properties.put(ContentModel.PROP_EMAIL, emailAddress);
        
        if (!personService.personExists(userName))
        {
            NodeRef personRef = personService.createPerson(properties);
            person = new ScriptNode(personRef, services, getScope()); 
        }
        
        return person;
    }
    
    /**
     * Set the content quota in bytes for a person.
     * Only the admin authority can set this value.
     * 
     * @param person    Person to set quota against.
     * @param quota     As a string, in bytes, a value of "-1" means no quota is set
     */
    public void setQuota(ScriptNode person, String quota)
    {
        if (this.authorityService.isAdminAuthority(AuthenticationUtil.getFullyAuthenticatedUser()))
        {
            this.contentUsageService.setUserQuota((String)person.getProperties().get(ContentModel.PROP_USERNAME), Long.parseLong(quota));
        }
    }
    
    /**
     * Get the collection of people stored in the repository.
     * An optional filter query may be provided by which to filter the people collection.
     * Space separate the query terms i.e. "john bob" will find all users who's first or
     * second names contain the strings "john" or "bob".
     * 
     * @param filter filter query string by which to filter the collection of people.
     *          If <pre>null</pre> then all people stored in the repository are returned
     *          
     * @deprecate see getPeople(filter, maxResults)
     *          
     * @return people collection as a JavaScript array
     */
    public Scriptable getPeople(String filter)
    {
        return getPeople(filter, 0);
    }
    
    /**
     * Get the collection of people stored in the repository.
     * An optional filter query may be provided by which to filter the people collection.
     * Space separate the query terms i.e. "john bob" will find all users who's first or
     * second names contain the strings "john" or "bob".
     * 
     * @param filter filter query string by which to filter the collection of people.
     *          If <pre>null</pre> then all people stored in the repository are returned
     * @param maxResults maximum results to return or all if <= 0
     * 
     * @return people collection as a JavaScript array
     */
    public Scriptable getPeople(String filter, int maxResults)
    {
        Object[] people = null;
        
        // TODO - remove open-ended query (eg cutoff at default/configurable max, eg. 5000 people)
        if (maxResults <= 0)
        {
            maxResults = Integer.MAX_VALUE;
        }
        
        if (filter == null || filter.length() == 0)
        {
            PagingRequest pagingRequest = new PagingRequest(maxResults, null);
            List<PersonInfo> persons = personService.getPeople(null, true, null, pagingRequest).getPage();
            people = new Object[persons.size()];
            for (int i=0; i<people.length; i++)
            {
                people[i] = persons.get(i).getNodeRef();
            }
        }
        else
        {
            filter = filter.trim();
            if (filter.length() != 0)
            {
                String term = filter.replace("\\", "").replace("\"", "");
                StringTokenizer t = new StringTokenizer(term, " ");
                int propIndex = term.indexOf(':');
                
                if ((t.countTokens() == 1) && (propIndex == -1))
                {
                    // simple non-FTS filter: firstname or lastname or username starting with term (ignoring case)
                    
                    String propVal = term;
                    
                    List<Pair<QName, String>> filterProps = new ArrayList<Pair<QName, String>>(3);
                    filterProps.add(new Pair<QName, String>(ContentModel.PROP_FIRSTNAME, propVal));
                    filterProps.add(new Pair<QName, String>(ContentModel.PROP_LASTNAME, propVal));
                    filterProps.add(new Pair<QName, String>(ContentModel.PROP_USERNAME, propVal));
                    
                    PagingRequest pagingRequest = new PagingRequest(maxResults, null);
                    List<PersonInfo> persons = personService.getPeople(filterProps, true, null, pagingRequest).getPage();
                    people = new Object[persons.size()];
                    for (int i=0; i<people.length; i++)
                    {
                        people[i] = persons.get(i).getNodeRef();
                    }
                 }
                 else
                 {
                     SearchParameters params = new SearchParameters();
                     
                     StringBuilder query = new StringBuilder(256);
                     
                     query.append("TYPE:\"").append(ContentModel.TYPE_PERSON).append("\" AND (");
                     
                     if (t.countTokens() == 1)
                     {
                        // fts-alfresco property search i.e. location:"maidenhead"
                        query.append(term.substring(0, propIndex+1))
                             .append('"')
                             .append(term.substring(propIndex+1))
                             .append('"');
                    }
                    else
                    {
                        // scan for non-fts-alfresco property search tokens
                        int nonFtsTokens = 0;
                        while (t.hasMoreTokens())
                        {
                            if (t.nextToken().indexOf(':') == -1) nonFtsTokens++;
                        }
                        t = new StringTokenizer(term, " ");
                        
                        // multiple terms supplied - look for first and second name etc.
                        // assume first term is first name, any more are second i.e. "Fraun van de Wiels"
                        // also allow fts-alfresco property search to reduce results
                        params.setDefaultOperator(SearchParameters.Operator.AND);
                        boolean firstToken = true;
                        boolean tokenSurname = false;
                        boolean propertySearch = false;
                        while (t.hasMoreTokens())
                        {
                            term = t.nextToken();
                            if (!propertySearch && term.indexOf(':') == -1)
                            {
                                if (nonFtsTokens == 1)
                                {
                                    // simple search: first name, last name and username starting with term
                                    query.append("(firstName:\"");
                                    query.append(term);
                                    query.append("*\" OR lastName:\"");
                                    query.append(term);
                                    query.append("*\" OR userName:\"");
                                    query.append(term);
                                    query.append("*\") ");
                                }
                                else
                                {
                                    if (firstToken)
                                    {
                                        query.append("firstName:\"");
                                        query.append(term);
                                        query.append("*\" ");
                                        
                                        firstToken = false;
                                    }
                                    else
                                    {
                                        if (tokenSurname)
                                        {
                                            query.append("OR ");
                                        }
                                        query.append("lastName:\"");
                                        query.append(term);
                                        query.append("*\" ");
                                        
                                        tokenSurname = true;
                                    }
                                }
                            }
                            else
                            {
                                // fts-alfresco property search i.e. "location:maidenhead"
                                propIndex = term.indexOf(':');
                                query.append(term.substring(0, propIndex+1))
                                     .append('"')
                                     .append(term.substring(propIndex+1))
                                     .append('"');
                                
                                propertySearch = true;
                            }
                        }
                    }
                    query.append(")");
                    
                    // define the search parameters
                    params.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
                    params.addStore(this.storeRef);
                    params.setQuery(query.toString());
                    if (maxResults > 0)
                    {
                        params.setLimitBy(LimitBy.FINAL_SIZE);
                        params.setLimit(maxResults);
                    }
                    
                    ResultSet results = null;
                    try
                    {
                        results = services.getSearchService().query(params);
                        people = results.getNodeRefs().toArray();
                    }
                    catch (Throwable err)
                    {
                        // hide query parse error from users
                        if (logger.isDebugEnabled())
                            logger.debug("Failed to execute people search: " + query.toString(), err);
                    }
                    finally
                    {
                        if (results != null)
                        {
                            results.close();
                        }
                    }
                 }
            }
        }
        
        if (people == null)
        {
            people = new Object[0];
        }
        
        return Context.getCurrentContext().newArray(getScope(), people);
    }
    
    /**
     * Gets the Person given the username
     * 
     * @param username  the username of the person to get
     * @return the person node (type cm:person) or null if no such person exists 
     */
    public ScriptNode getPerson(String username)
    {
        ParameterCheck.mandatoryString("Username", username);
        ScriptNode person = null;
        if (personService.personExists(username))
        {
            NodeRef personRef = personService.getPerson(username);
            person = new ScriptNode(personRef, services, getScope());
        }
        return person;
    }

    /**
     * Gets the Group given the group name
     * 
     * @param groupName  name of group to get
     * @return  the group node (type usr:authorityContainer) or null if no such group exists
     */
    public ScriptNode getGroup(String groupName)
    {
        ParameterCheck.mandatoryString("GroupName", groupName);
        ScriptNode group = null;
        NodeRef groupRef = authorityDAO.getAuthorityNodeRefOrNull(groupName);
        if (groupRef != null)
        {
            group = new ScriptNode(groupRef, services, getScope());
        }
        return group;
    }
    
    /**
     * Deletes a group from the system.
     * 
     * @param group     The group to delete
     */
    public void deleteGroup(ScriptNode group)
    {
        ParameterCheck.mandatory("Group", group);
        if (group.getQNameType().equals(ContentModel.TYPE_AUTHORITY_CONTAINER))
        {
            String groupName = (String)group.getProperties().get(ContentModel.PROP_AUTHORITY_NAME);
            authorityService.deleteAuthority(groupName);
        }
    }
    
    /**
     * Create a new root level group with the specified unique name
     * 
     * @param groupName     The unique group name to create - NOTE: do not prefix with "GROUP_"
     * 
     * @return the group reference if successful or null if failed
     */
    public ScriptNode createGroup(String groupName)
    {
        return createGroup(null, groupName);
    }
    
    /**
     * Create a new group with the specified unique name
     * 
     * @param parentGroup   The parent group node - can be null for a root level group
     * @param groupName     The unique group name to create - NOTE: do not prefix with "GROUP_"
     * 
     * @return the group reference if successful or null if failed
     */
    public ScriptNode createGroup(ScriptNode parentGroup, String groupName)
    {
        ParameterCheck.mandatoryString("GroupName", groupName);
        
        ScriptNode group = null;
        
        String actualName = services.getAuthorityService().getName(AuthorityType.GROUP, groupName);
        if (authorityService.authorityExists(actualName) == false)
        {
            String result = authorityService.createAuthority(AuthorityType.GROUP, groupName);
            if (parentGroup != null)
            {
                String parentGroupName = (String)parentGroup.getProperties().get(ContentModel.PROP_AUTHORITY_NAME);
                if (parentGroupName != null)
                {
                    authorityService.addAuthority(parentGroupName, actualName);
                }
            }
            group = getGroup(result);
        }
        
        return group;
    }
    
    /**
     * Add an authority (a user or group) to a group container as a new child
     * 
     * @param parentGroup   The parent container group
     * @param authority     The authority (user or group) to add
     */
    public void addAuthority(ScriptNode parentGroup, ScriptNode authority)
    {
        ParameterCheck.mandatory("Authority", authority);
        ParameterCheck.mandatory("ParentGroup", parentGroup);
        if (parentGroup.getQNameType().equals(ContentModel.TYPE_AUTHORITY_CONTAINER))
        {
            String parentGroupName = (String)parentGroup.getProperties().get(ContentModel.PROP_AUTHORITY_NAME);
            String authorityName;
            if (authority.getQNameType().equals(ContentModel.TYPE_AUTHORITY_CONTAINER))
            {
                authorityName = (String)authority.getProperties().get(ContentModel.PROP_AUTHORITY_NAME);
            }
            else
            {
                authorityName = (String)authority.getProperties().get(ContentModel.PROP_USERNAME);
            }
            authorityService.addAuthority(parentGroupName, authorityName);
        }
    }
    
    /**
     * Remove an authority (a user or group) from a group
     * 
     * @param parentGroup   The parent container group
     * @param authority     The authority (user or group) to remove
     */
    public void removeAuthority(ScriptNode parentGroup, ScriptNode authority)
    {
        ParameterCheck.mandatory("Authority", authority);
        ParameterCheck.mandatory("ParentGroup", parentGroup);
        if (parentGroup.getQNameType().equals(ContentModel.TYPE_AUTHORITY_CONTAINER))
        {
            String parentGroupName = (String)parentGroup.getProperties().get(ContentModel.PROP_AUTHORITY_NAME);
            String authorityName;
            if (authority.getQNameType().equals(ContentModel.TYPE_AUTHORITY_CONTAINER))
            {
                authorityName = (String)authority.getProperties().get(ContentModel.PROP_AUTHORITY_NAME);
            }
            else
            {
                authorityName = (String)authority.getProperties().get(ContentModel.PROP_USERNAME);
            }
            authorityService.removeAuthority(parentGroupName, authorityName);
        }
    }
    
    /**
     * Gets the members (people) of a group (including all sub-groups)
     * 
     * @param group        the group to retrieve members for
     * @param recurse      recurse into sub-groups
     * 
     * @return members of the group as a JavaScript array
     */
    public Scriptable getMembers(ScriptNode group)
    {
        ParameterCheck.mandatory("Group", group);
        Object[] members = getContainedAuthorities(group, AuthorityType.USER, true);
        return Context.getCurrentContext().newArray(getScope(), members);
    }

    /**
     * Gets the members (people) of a group
     * 
     * @param group        the group to retrieve members for
     * @param recurse      recurse into sub-groups
     * 
     * @return the members of the group as a JavaScript array
     */
    public Scriptable getMembers(ScriptNode group, boolean recurse)
    {
        ParameterCheck.mandatory("Group", group);
        Object[] members = getContainedAuthorities(group, AuthorityType.USER, recurse);
        return Context.getCurrentContext().newArray(getScope(), members);
    }
    
    /**
     * Gets the groups that contain the specified authority
     * 
     * @param person       the user (cm:person) to get the containing groups for
     * 
     * @return the containing groups as a JavaScript array
     */
    public Scriptable getContainerGroups(ScriptNode person)
    {
        ParameterCheck.mandatory("Person", person);
        Object[] parents = null;
        Set<String> authorities = this.authorityService.getContainingAuthorities(
                AuthorityType.GROUP,
                (String)person.getProperties().get(ContentModel.PROP_USERNAME),
                false);
        parents = new Object[authorities.size()];
        int i = 0;
        for (String authority : authorities)
        {
            ScriptNode group = getGroup(authority);
            if (group != null)
            {
                parents[i++] = group; 
            }
        }
        return Context.getCurrentContext().newArray(getScope(), parents);
    }
    
    /**
     * Return true if the specified user is an Administrator authority.
     * 
     * @param person to test
     * 
     * @return true if an admin, false otherwise
     */
    public boolean isAdmin(ScriptNode person)
    {
        ParameterCheck.mandatory("Person", person);
        return this.authorityService.isAdminAuthority((String)person.getProperties().get(ContentModel.PROP_USERNAME));
    }

    /**
     * Return true if the specified user is an guest authority.
     * 
     * @param person to test
     * 
     * @return true if an admin, false otherwise
     */
    public boolean isGuest(ScriptNode person)
    {
        ParameterCheck.mandatory("Person", person);
        return this.authorityService.isGuestAuthority((String) person.getProperties().get(ContentModel.PROP_USERNAME));
    }

    /**
     * Gets a map of capabilities (boolean assertions) for the given person.
     * 
     * @param person
     *            the person
     * @return the capability map
     */
    public Map<String, Boolean> getCapabilities(final ScriptNode person)
    {
        ParameterCheck.mandatory("Person", person);
        Map<String,Boolean> retVal = new ScriptableHashMap<String, Boolean>();
        retVal.putAll(this.valueDerivingMapFactory.getMap(person));
        return retVal;
    }
    
    /**
     * Return a map of the Person properties that are marked as immutable for the given user.
     * This enables a script to interogate which properties are dealt with by an external
     * system such as LDAP and should not be mutable in any client UI.
     * 
     * @param username
     * 
     * @return ScriptableHashMap
     */
    public ScriptableHashMap getImmutableProperties(String username)
    {
        Set<QName> props = userRegistrySynchronizer.getPersonMappedProperties(username);
        ScriptableHashMap propMap = new ScriptableHashMap();
        for (QName prop : props)
        {
            propMap.put(prop.toString(), Boolean.TRUE);
        }
        return propMap;
    }

    /**
     * Get Contained Authorities
     * 
     * @param container  authority containers
     * @param type       authority type to filter by
     * @param recurse    recurse into sub-containers
     * 
     * @return contained authorities
     */
    private Object[] getContainedAuthorities(ScriptNode container, AuthorityType type, boolean recurse)
    {
        Object[] members = null;
        
        if (container.getQNameType().equals(ContentModel.TYPE_AUTHORITY_CONTAINER))
        {
            String groupName = (String)container.getProperties().get(ContentModel.PROP_AUTHORITY_NAME);
            Set<String> authorities = authorityService.getContainedAuthorities(type, groupName, !recurse);
            members = new Object[authorities.size()];
            int i = 0;
            for (String authority : authorities)
            {
                AuthorityType authorityType = AuthorityType.getAuthorityType(authority);
                if (authorityType.equals(AuthorityType.GROUP))
                {
                    ScriptNode group = getGroup(authority);
                    if (group != null)
                    {
                        members[i++] = group; 
                    }
                }
                else if (authorityType.equals(AuthorityType.USER))
                {
                    ScriptNode person = getPerson(authority);
                    if (person != null)
                    {
                        members[i++] = person; 
                    }
                }
            }
        }
        
        return members != null ? members : new Object[0];
    }
}
