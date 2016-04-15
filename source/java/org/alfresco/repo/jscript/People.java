/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.jscript;

import java.io.Serializable;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.UserNameGenerator;
import org.alfresco.repo.security.authority.AuthorityDAO;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.security.person.PersonServiceImpl;
import org.alfresco.repo.security.sync.UserRegistrySynchronizer;
import org.alfresco.repo.tenant.TenantDomainMismatchException;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.PermissionEvaluationMode;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.security.PersonService.PersonInfo;
import org.alfresco.service.cmr.usage.ContentUsageService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyMap;
import org.alfresco.util.ScriptPagingDetails;
import org.alfresco.util.ValueDerivingMapFactory;
import org.alfresco.util.ValueDerivingMapFactory.ValueDeriver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * Scripted People service for describing and executing actions against People & Groups.
 * 
 * @author davidc
 * @author kevinr
 */
public class People extends BaseScopableProcessorExtension implements InitializingBean
{
    private static Log logger = LogFactory.getLog(People.class);
    
    /** Repository Service Registry */
    private ServiceRegistry services;
    private AuthorityDAO authorityDAO;
    private AuthorityService authorityService;
    private PersonService personService;
    private MutableAuthenticationService authenticationService;
    private ContentUsageService contentUsageService;
    private UserNameGenerator usernameGenerator;
    private UserRegistrySynchronizer userRegistrySynchronizer;
    protected TenantService tenantService;
    
    private StoreRef storeRef;
    private ValueDerivingMapFactory<ScriptNode, String, Boolean> valueDerivingMapFactory;
    private int numRetries = 10;
    
    private int defaultListMaxResults = 5000;
    private boolean honorHintUseCQ = true;
    
    protected static final String HINT_CQ_SUFFIX = " [hint:useCQ]";
    
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
     * @param serviceRegistry   the service registry
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
     * @param userRegistrySynchronizer UserRegistrySynchronizer
     */
    public void setUserRegistrySynchronizer(UserRegistrySynchronizer userRegistrySynchronizer)
    {
        this.userRegistrySynchronizer = userRegistrySynchronizer;
    }
    
    public void setDefaultListMaxResults(int defaultListMaxResults)
    {
        this.defaultListMaxResults = defaultListMaxResults;
    }
    
    /**
     * Allows customers to choose to use Solr or Lucene rather than a canned query in
     * {@link #getPeople(String, int, String, boolean)} when
     * {@code " [hint:useCQ]"} is appended to the search term (currently Share's
     * User Console does this). The down side is that new users may not appear as they
     * will not have been indexed. This is similar to what happened in 4.1.1 prior to
     * MNT-7548 (4.1.2 and 4.1.1.1). The down side of using a canned query at the moment
     * is that there is a bug, so that it is impossible to search for names such as
     * {@code "Carlos Allende García"} where the first or last names may contain spaces.
     * See MNT-9719 for more details. The alfresco global property
     * {@code people.search.honor.hint.useCQ} is used to set this value (default is true).
     */
    public void setHonorHintUseCQ(boolean honorHintUseCQ)
    {
        this.honorHintUseCQ = honorHintUseCQ;
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
     * @deprecated recated see getPeople(filter, maxResults)
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
        return getPeople(filter, maxResults, null, true);
    }
    
    /**
     * Get the collection of people stored in the repository.
     * An optional filter query may be provided by which to filter the people collection.
     * Space separate the query terms i.e. "john bob" will find all users who's first or
     * second names contain the strings "john" or "bob".
     * Method supports sorting by specifying sortBy and sortAsc params.
     * 
     * @param filter filter query string by which to filter the collection of people.
     *          If <pre>null</pre> then all people stored in the repository are returned
     * @param maxResults maximum results to return or all if <= 0
     * @param sortBy field for sorting
     * @param sortAsc sort ascending or not
     * 
     * @return people collection as a JavaScript array
     */
    public Scriptable getPeople(String filter, int maxResults, String sortBy, boolean sortAsc)
    {
        return getPeoplePaging(filter, new ScriptPagingDetails(maxResults, 0), sortBy, Boolean.valueOf(sortAsc));
    }
    
    public Scriptable getPeoplePaging(String filter, ScriptPagingDetails pagingRequest, String sortBy, Boolean sortAsc)
    {
        List<PersonInfo> persons = getPeopleImpl(filter, pagingRequest, sortBy, sortAsc);
        
        Object[] peopleRefs = new Object[persons.size()];
        for (int i = 0; i < peopleRefs.length; i++)
        {
            peopleRefs[i] = persons.get(i).getNodeRef();
        }
        
        return Context.getCurrentContext().newArray(getScope(), peopleRefs);
    }
    
    protected List<PersonInfo> getPeopleImpl(String filter, ScriptPagingDetails pagingRequest, String sortBy, Boolean sortAsc)
    {
        ParameterCheck.mandatory("pagingRequest", pagingRequest);
        
        boolean useCQ = false;
        if (filter != null)
        {
            if (filter.endsWith(HINT_CQ_SUFFIX))
            {
                useCQ = honorHintUseCQ;
                filter = filter.substring(0, filter.length()-HINT_CQ_SUFFIX.length());
            }
        }
        else
        {
            filter = "*";
        }
        
        List<PersonInfo> persons = null;
        
        int maxResults = pagingRequest.getMaxItems();
        if ((maxResults <= 0) || (maxResults > defaultListMaxResults))
        {
            // remove open-ended query (eg cutoff at default/configurable max, eg. 5000 people)
            maxResults = defaultListMaxResults;
            pagingRequest.setMaxItems(maxResults);
        }
        
        // In order to use a SOLR/Lucene search, we must have a non-empty filter string - see ALF-18876
        if ((filter == null || filter.trim().isEmpty()) || useCQ)
        {
            persons = getPeopleImplDB(filter, pagingRequest, sortBy, sortAsc);
        }
        else
        {
            filter = filter.trim();
            
            String term = filter.replace("\"", "");
            String[] tokens = term.split("(?<!\\\\) ");
            int propIndex = term.lastIndexOf(':');
            int wildPosition = term.indexOf('*');
            
            // simple filter - can use CQ if search fails
            useCQ = ((tokens.length == 1) && (propIndex == -1) && ((wildPosition == -1) || (wildPosition == (term.length() - 1))));
            
            try
            {
                // FTS
                List<NodeRef> personRefs = getPeopleImplSearch(term, tokens, pagingRequest, sortBy, sortAsc);
                
                if (personRefs != null)
                {
                    persons = new ArrayList<PersonInfo>(personRefs.size());
                    for (NodeRef personRef : personRefs)
                    {
                        persons.add(personService.getPerson(personRef));
                    }
                }
            }
            catch (Throwable err)
            {
                if (useCQ)
                {
                    // search unavailable and/or parser exception - try CQ instead
                    // simple non-FTS filter: firstname or lastname or username starting with term (ignoring case)
                    persons = getPeopleImplDB(filter, pagingRequest, sortBy, sortAsc);
                }
            }
        }
        
        return (persons != null ? persons : new ArrayList<PersonInfo>(0));
    }
    
    // canned query
    protected List<PersonInfo> getPeopleImplDB(String filter, ScriptPagingDetails pagingRequest, String sortBy, Boolean sortAsc)
    {
        List<QName> filterProps = null;
        
        if ((filter != null) && (filter.length() > 0))
        {
            filter = filter.trim();
            if (! filter.equals("*"))
            {
                filter = filter.replace("\\", "").replace("\"", "");
                
                // simple non-FTS filter: firstname or lastname or username starting with term (ignoring case)
                
                filterProps = new ArrayList<QName>(3);
                filterProps.add(ContentModel.PROP_FIRSTNAME);
                filterProps.add(ContentModel.PROP_LASTNAME);
                filterProps.add(ContentModel.PROP_USERNAME);
            }
        }
        
        // Build the sorting. The user controls the primary sort, we supply
        // additional ones automatically
        List<Pair<QName,Boolean>> sort = new ArrayList<Pair<QName,Boolean>>();
        if ("lastName".equals(sortBy))
        {
           sort.add(new Pair<QName, Boolean>(ContentModel.PROP_LASTNAME, sortAsc));
           sort.add(new Pair<QName, Boolean>(ContentModel.PROP_FIRSTNAME, sortAsc));
           sort.add(new Pair<QName, Boolean>(ContentModel.PROP_USERNAME, sortAsc));
        }
        else if ("firstName".equals(sortBy))
        {
           sort.add(new Pair<QName, Boolean>(ContentModel.PROP_FIRSTNAME, sortAsc));
           sort.add(new Pair<QName, Boolean>(ContentModel.PROP_LASTNAME, sortAsc));
           sort.add(new Pair<QName, Boolean>(ContentModel.PROP_USERNAME, sortAsc));
        }
        else
        {
           sort.add(new Pair<QName, Boolean>(ContentModel.PROP_USERNAME, sortAsc));
           sort.add(new Pair<QName, Boolean>(ContentModel.PROP_FIRSTNAME, sortAsc));
           sort.add(new Pair<QName, Boolean>(ContentModel.PROP_LASTNAME, sortAsc));
        }
        
        return personService.getPeople(filter, filterProps, sort, pagingRequest).getPage();
    }
    
    // search query
    protected List<NodeRef> getPeopleImplSearch(String term, String[] tokens, ScriptPagingDetails pagingRequest, String sortBy, Boolean sortAsc) throws Throwable
    {
        List<NodeRef> personRefs = null;
        
        Long start = (logger.isDebugEnabled() ? System.currentTimeMillis() : null);
        
        int propIndex = term.indexOf(':');
        
        int maxResults = pagingRequest.getMaxItems();
        int skipCount = pagingRequest.getSkipCount();
        
        SearchParameters params = new SearchParameters();
        params.addQueryTemplate("_PERSON", "|%firstName OR |%lastName OR |%userName");
        params.setDefaultFieldName("_PERSON");
        params.setExcludeTenantFilter(getExcludeTenantFilter());
        params.setPermissionEvaluation(getPermissionEvaluationMode());
        
        StringBuilder query = new StringBuilder(256);
        
        query.append("TYPE:\"").append(ContentModel.TYPE_PERSON).append("\" AND (");
        
        if (tokens.length == 1)
        {
            // single word with no field will go against _PERSON and expand

            // fts-alfresco property search i.e. location:"maidenhead"
            query.append(term.substring(0, propIndex + 1)).append('"');
            if (propIndex < 0)
            {
                query.append('*');
            }
            query.append(term.substring(propIndex + 1));
            if (propIndex > 0)
            {
                query.append('"');
            }
            else
            {
                query.append("*\"");
            }
        }
        else
        {
            // scan for non-fts-alfresco property search tokens
            int nonFtsTokens = 0;
            for (String token : tokens)
            {
                if (token.indexOf(':') == -1) nonFtsTokens++;
            }
            tokens = term.split("(?<!\\\\) ");

            // multiple terms supplied - look for first and second name etc.
            // also allow fts-alfresco property search to reduce results
            params.setDefaultOperator(SearchParameters.Operator.AND);
            boolean propertySearch = false;
            StringBuilder multiPartNames = new StringBuilder(tokens.length);
            boolean firstToken = true;
            for (String token : tokens)
            {
                if (!propertySearch && token.indexOf(':') == -1)
                {
                    if (nonFtsTokens == 1)
                    {
                        // simple search: first name, last name and username
                        // starting with term
                        query.append("_PERSON:\"*");
                        query.append(token);
                        query.append("*\" ");
                    }
                    else
                    {
                        // ALF-11311, in order to support multi-part firstNames/lastNames,
                        // we need to use the whole tokenized term for both
                        // firstName and lastName
                        if (token.endsWith("*"))
                        {
                            token = token.substring(0, token.lastIndexOf("*"));
                        }
                        multiPartNames.append("\"*");
                        multiPartNames.append(token);
                        multiPartNames.append("*\"");
                        if (firstToken)
                        {
                            multiPartNames.append(' ');
                        }
                        firstToken = false;
                    }
                }
                else
                {
                    // fts-alfresco property search i.e. "location:maidenhead"
                    propIndex = token.lastIndexOf(':');
                    query.append(token.substring(0, propIndex + 1)).append('"')
                                .append(token.substring(propIndex + 1)).append('"').append(' ');

                    propertySearch = true;
                }
            }
            // ALF-11311, in order to support multi-part firstNames/lastNames,
            // we need to use the whole tokenized term for both firstName and lastName.
            // e.g. "john junior lewis martinez", where "john junior" is the first
            // name and "lewis martinez" is the last name.
            if (multiPartNames.length() > 0)
            {
                query.append("firstName:");
                query.append(multiPartNames);
                query.append(" OR lastName:");
                query.append(multiPartNames);
            }
        }
        query.append(")");
       
       // define the search parameters
       params.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
       params.addStore(this.storeRef);
       params.setQuery(query.toString());
       
       if (logger.isDebugEnabled())
       {
           if ((sortBy != null) && (! sortBy.isEmpty()))
           {
               logger.debug("getPeopleImplSearch: ignoring sortBy ("+sortBy+")- not yet supported by model for search");
           }
       }
       
       /* not yet supported (default property index tokenisation mode = true)
       if ("lastName".equals(sortBy))
       {
           params.addSort("@{http://www.alfresco.org/model/content/1.0}lastName", sortAsc);
           params.addSort("@{http://www.alfresco.org/model/content/1.0}firstName", sortAsc);
           params.addSort("@{http://www.alfresco.org/model/content/1.0}userName", sortAsc);
       }
       else if ("firstName".equals(sortBy))
       {
           params.addSort("@{http://www.alfresco.org/model/content/1.0}firstName", sortAsc);
           params.addSort("@{http://www.alfresco.org/model/content/1.0}lastName", sortAsc);
           params.addSort("@{http://www.alfresco.org/model/content/1.0}userName", sortAsc);
       }
       else
       {
           params.addSort("@{http://www.alfresco.org/model/content/1.0}userName", sortAsc);
           params.addSort("@{http://www.alfresco.org/model/content/1.0}firstName", sortAsc);
           params.addSort("@{http://www.alfresco.org/model/content/1.0}userName", sortAsc);
       }
       */
       
       if (maxResults > 0)
       {
           params.setLimitBy(LimitBy.FINAL_SIZE);
           params.setLimit(maxResults);
       }
       
       if (skipCount > 0)
       {
           params.setSkipCount(skipCount);
       }
       
       ResultSet results = null;
       try
       {
           results = services.getSearchService().query(params);
           
           personRefs = getSortedPeopleObjects(results.getNodeRefs(), sortBy, sortAsc);
           
           if (start != null)
           {
               logger.debug("getPeople: search - "+personRefs.size()+" items (in "+(System.currentTimeMillis()-start)+" msecs)");
           }
       }
       catch (Throwable err)
       {
           if (logger.isDebugEnabled())
           {
               logger.debug("Failed to execute people search: " + query.toString(), err);
           }
           
           throw err;
       }
       finally
       {
           if (results != null)
           {
               results.close();
           }
       }
       
       return personRefs;
    }
    
    private List<NodeRef> getSortedPeopleObjects(List<NodeRef> peopleRefs, final String sortBy, Boolean sortAsc)
    {
        if (sortBy == null)
        {
            return peopleRefs;
        }
        
        //make copy of peopleRefs because it can be unmodifiable list.
        List<NodeRef> sortedPeopleRefs = new ArrayList<NodeRef>(peopleRefs);
        final Collator col = Collator.getInstance(I18NUtil.getLocale());
        final NodeService nodeService = services.getNodeService();
        final int orderMultiplicator = ((sortAsc == null) || sortAsc)  ? 1 : -1;
        Collections.sort(sortedPeopleRefs, new Comparator<NodeRef>()
        {
            @Override
            public int compare(NodeRef n1, NodeRef n2)
            {
                Serializable  p1 = getProperty(n1);
                Serializable  p2 = getProperty(n2);

                if ((p1 instanceof Long) && (p2 instanceof Long))
                {
                    return Long.compare((Long)p1, (Long)p2) * orderMultiplicator;
                }

                return col.compare(p1.toString(), p2) * orderMultiplicator;
            }

            public Serializable getProperty(NodeRef nodeRef)
            {
                Serializable result;

                if ("fullName".equalsIgnoreCase(sortBy))
                {
                    String firstName = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_FIRSTNAME);
                    String lastName = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_LASTNAME);
                    String fullName = firstName;
                    if (lastName != null && lastName.length() > 0)
                    {
                        fullName = fullName + " " + lastName;
                    }

                    result = fullName;
                }
                else if ("jobtitle".equalsIgnoreCase(sortBy))
                {
                    result = nodeService.getProperty(nodeRef, ContentModel.PROP_JOBTITLE);
                }
                else if ("email".equalsIgnoreCase(sortBy))
                {
                    result = nodeService.getProperty(nodeRef, ContentModel.PROP_EMAIL);
                }
                else if ("usage".equalsIgnoreCase(sortBy))
                {
                    result = nodeService.getProperty(nodeRef, ContentModel.PROP_SIZE_CURRENT);
                }
                else if ("quota".equalsIgnoreCase(sortBy))
                {
                    result = nodeService.getProperty(nodeRef, ContentModel.PROP_SIZE_QUOTA);
                }
                else
                {
                    // Default
                    result = nodeService.getProperty(nodeRef, ContentModel.PROP_USERNAME);
                }

                if (result == null)
                {
                    result = "";
                }

                return result;
            }

        });
        
        return sortedPeopleRefs;
    }
    
    /**
     * Gets the Person given the username
     * 
     * @param username  the username of the person to get
     * @return the person node (type cm:person) or null if no such person exists 
     */
    public ScriptNode getPerson(final String username)
    {
    	NodeRef personRef = null;

        ParameterCheck.mandatory("Username", username);
        try
        {
	        personRef = personService.getPersonOrNull(username);
        }
        catch(AccessDeniedException e)
        {
        	// ok, just return null to indicate not found
        }

        return personRef == null ? null : new ScriptNode(personRef, services, getScope());
    }
    
    /**
     * Faster helper when the script just wants to build the Full name for a person.
     * Avoids complete getProperties() retrieval for a cm:person.
     * 
     * @param username  the username of the person to get Full name for
     * @return full name for a person or null if the user does not exist in the system.
     */
    public String getPersonFullName(final String username)
    {
        String name = null;
        ParameterCheck.mandatoryString("Username", username);
        final NodeRef personRef = personService.getPersonOrNull(username);
        if (personRef != null)
        {
            final NodeService nodeService = services.getNodeService();
            final String firstName = (String)nodeService.getProperty(personRef, ContentModel.PROP_FIRSTNAME);
            final String lastName = (String)nodeService.getProperty(personRef, ContentModel.PROP_LASTNAME);
            name = (firstName != null ? firstName + " " : "") + (lastName != null ? lastName : "");
        }
        return name;
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
        Set<String> authorities = this.authorityService.getContainingAuthoritiesInZone(
                AuthorityType.GROUP,
                (String)person.getProperties().get(ContentModel.PROP_USERNAME),
                AuthorityService.ZONE_APP_DEFAULT, null, 1000);
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
     * @param username String
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
    
    public boolean getExcludeTenantFilter()
    {
        return false;
    }
    
    public PermissionEvaluationMode getPermissionEvaluationMode()
    {
        return PermissionEvaluationMode.EAGER;
    }
}
