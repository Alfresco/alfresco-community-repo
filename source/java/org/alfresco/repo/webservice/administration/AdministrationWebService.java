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
package org.alfresco.repo.webservice.administration;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.webservice.AbstractWebService;
import org.alfresco.repo.webservice.Utils;
import org.alfresco.repo.webservice.action.ActionFault;
import org.alfresco.repo.webservice.repository.RepositoryFault;
import org.alfresco.repo.webservice.types.NamedValue;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.axis.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Roy Wetherall
 */
public class AdministrationWebService extends AbstractWebService implements
        AdministrationServiceSoapPort
{
    /** Log */
    private static Log logger = LogFactory.getLog(AdministrationWebService.class);
    
    /** The person service */
    private PersonService personService = null;
    
    /** The authentication service */
    private AuthenticationService authenticationService = null;
    
    /** The transaction service */
    private TransactionService transactionService = null;
    
    /** A set of ignored properties */
    private static Set<QName> ignoredProperties = new HashSet<QName>(3);
    
    /**
     * Constructor
     */
    public AdministrationWebService()
    {
        // Set properties to ignore
        AdministrationWebService.ignoredProperties.add(ContentModel.PROP_STORE_PROTOCOL);
        AdministrationWebService.ignoredProperties.add(ContentModel.PROP_STORE_IDENTIFIER);
        AdministrationWebService.ignoredProperties.add(ContentModel.PROP_NODE_UUID);
    }
    
    /**
     * Set the transaction service
     * 
     * @param transactionService    the transaction service
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }
    
    /**
     * Set the person service
     * 
     * @param personService     sets the person service
     */
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }
    
    /**
     * Set the authentication service
     * 
     * @param authenticationService     the authentication service
     */
    public void setAuthenticationService(AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }
    
    /**
     * @see org.alfresco.repo.webservice.administration.AdministrationServiceSoapPort#queryUsers(org.alfresco.repo.webservice.administration.UserFilter)
     */
    public UserQueryResults queryUsers(final UserFilter filter)
            throws RemoteException, AdministrationFault
    {
        try
        {
            RetryingTransactionCallback<UserQueryResults> callback = new RetryingTransactionCallback<UserQueryResults>()
            {
                public UserQueryResults execute() throws Exception
                {
                    return queryUsersImpl(filter);
                }
            };
            return transactionService.getRetryingTransactionHelper().doInTransaction(callback);
        }
        catch (Throwable exception)
        {
            if (logger.isDebugEnabled())
            {
                logger.error("Unexpected error occurred", exception);
            }
            
            throw new ActionFault(0, exception.getMessage());
        }
    }

    /**
     * Query users, batch by set size
     * 
     * @param filter    used to filter results
     * @return          user query results, optionally batched
     */
    private UserQueryResults queryUsersImpl(UserFilter filter)
    {
        MessageContext msgContext = MessageContext.getCurrentContext();
        
        // Create the query
        UserQuery query = new UserQuery(filter);
        
        // Create a user query session
        UserQuerySession userQuerySession = new UserQuerySession(Long.MAX_VALUE, Utils.getBatchSize(msgContext), query);
        
        // Get the next batch of results
        UserQueryResults userQueryResults = userQuerySession.getNextResults(serviceRegistry);

        String querySessionId = userQuerySession.getId();
        // add the session to the cache if there are more results to come
        boolean haveMoreResults = userQuerySession.haveMoreResults();
        if (haveMoreResults)
        {
            querySessionCache.put(querySessionId, userQuerySession);
        }
        
        // Construct the return value
        // TODO: http://issues.alfresco.com/browse/AR-1689
        // This looks odd, but I've chosen to be specific about when the ID is set on the return
        // results and when it isn't.
        UserQueryResults result = new UserQueryResults(
                haveMoreResults ? querySessionId : null,
                        userQueryResults.getUserDetails());
        
        // Done
        return result;
    }    

    /**
     *  @see org.alfresco.repo.webservice.administration.AdministrationServiceSoapPort#fetchMoreUsers(java.lang.String)
     */
    public UserQueryResults fetchMoreUsers(final String querySession)
            throws RemoteException, AdministrationFault
    {
        try
        {
            RetryingTransactionCallback<UserQueryResults> callback = new RetryingTransactionCallback<UserQueryResults>()
            {
                public UserQueryResults execute() throws Exception
                {
                    return fetchMoreUsersImpl(querySession);
                }
            };
            return transactionService.getRetryingTransactionHelper().doInTransaction(callback);
        }
        catch (Throwable exception)
        {
            if (logger.isDebugEnabled())
            {
                logger.error("Unexpected error occurred", exception);
            }
            
            throw new ActionFault(0, exception.getMessage());
        }
    }

    /**
     * 
     * @param querySessionId
     * @return
     */
    private UserQueryResults fetchMoreUsersImpl(String querySessionId) throws RepositoryFault
    {
        UserQuerySession session = null;
        try
        {
            session = (UserQuerySession) querySessionCache.get(querySessionId);
        }
        catch (ClassCastException e)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Query session was not generated by the AdministrationWebService: " + querySessionId);
            }
            throw new RepositoryFault(
                    4,
                    "querySession with id '" + querySessionId + "' is invalid");
        }
        
        UserQueryResults queryResult = null;
        if (session != null)
        {
            queryResult = session.getNextResults(serviceRegistry);
            if (!session.haveMoreResults())
            {
                this.querySessionCache.remove(querySessionId);
            }
        }
        
        return queryResult;
    }

    /**
     * @see org.alfresco.repo.webservice.administration.AdministrationServiceSoapPort#getUser(java.lang.String)
     */
    public UserDetails getUser(final String userName) throws RemoteException, AdministrationFault
    {
        try
        {
            RetryingTransactionCallback<UserDetails> callback = new RetryingTransactionCallback<UserDetails>()
            {
                public UserDetails execute() throws Exception
                {
                    return getUserImpl(userName);
                }
            };
            return transactionService.getRetryingTransactionHelper().doInTransaction(callback);
        }
        catch (Throwable exception)
        {
            if (logger.isDebugEnabled())
            {
                logger.error("Unexpected error occurred", exception);
            }
            
            throw new ActionFault(0, exception.getMessage());
        }
    }
    
    /**
     * Get the user details
     * 
     * @param userName              the user name
     * @return                      the user details object
     * @throws RemoteException
     * @throws AdministrationFault
     */
    private UserDetails getUserImpl(String userName)
    {
        NodeService nodeService = serviceRegistry.getNodeService();
        UserDetails userDetails = null;
        
        if (this.personService.personExists(userName) == true)
        {
            NodeRef nodeRef = this.personService.getPerson(userName);            
            userDetails = createUserDetails(nodeService, userName, nodeRef);
        }
        else
        {
            // Throw an exception to indicate that the user does not exist
            throw new AlfrescoRuntimeException(MessageFormat.format("The user with name {0} does not exist.", new Object[]{userName}));
        }
        
        return userDetails;
    }

    /**
     * Given a valid person node reference will create a user details object
     * 
     * @param nodeRef   the node reference
     * @return          the user details object populated with the appropriate property values
     */
    /* package */ static UserDetails createUserDetails(NodeService nodeService, String userName, NodeRef nodeRef)
    {
        // Create the user details object
        UserDetails userDetails = new UserDetails();
        
        // Set the user name
        userDetails.setUserName(userName);
        
        // Set the various property values
        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
        List<NamedValue> namedValues = new ArrayList<NamedValue>(properties.size());
        for (Map.Entry<QName, Serializable> entry : properties.entrySet())
        {
            if (AdministrationWebService.ignoredProperties.contains(entry.getKey()) == false)
            {
                String value = null;
                try
                {
                    value = DefaultTypeConverter.INSTANCE.convert(String.class, entry.getValue());
                } 
                catch (Throwable exception)
                {
                    value = entry.getValue().toString();
                } 
                NamedValue namedValue = new NamedValue();
                namedValue.setName(entry.getKey().toString());
                namedValue.setIsMultiValue(false);
                namedValue.setValue(value);
                namedValues.add(namedValue);
            }
        }
        userDetails.setProperties((NamedValue[])namedValues.toArray(new NamedValue[namedValues.size()]));
        
        return userDetails;        
    }

    /**
     * @see org.alfresco.repo.webservice.administration.AdministrationServiceSoapPort#createUsers(org.alfresco.repo.webservice.administration.NewUserDetails[])
     */
    public UserDetails[] createUsers(final NewUserDetails[] newUsers) throws RemoteException, AdministrationFault
    {
        try
        {
            RetryingTransactionCallback<UserDetails[]> callback = new RetryingTransactionCallback<UserDetails[]>()
            {
                public UserDetails[] execute() throws Exception
                {
                    return createUsersImpl(newUsers);
                }
            };
            return transactionService.getRetryingTransactionHelper().doInTransaction(callback);
        }
        catch (Throwable exception)
        {
            if (logger.isDebugEnabled())
            {
                logger.error("Unexpected error occurred", exception);
            }
            
            throw new ActionFault(0, exception.getMessage());
        }
    }
    
    /**
     * Create the new users
     * 
     * @param newUsers          the new users detail
     * @return                  the details of the created users
     * @throws RemoteException
     * @throws AdministrationFault
     */
    private UserDetails[] createUsersImpl(NewUserDetails[] newUsers)
    {
        NodeService nodeService = serviceRegistry.getNodeService();
        UserDetails[] userDetails = new UserDetails[newUsers.length];
        
        int index = 0;
        for (NewUserDetails newUser : newUsers)
        {
            // Create a new authentication
            this.authenticationService.createAuthentication(newUser.getUserName(), newUser.getPassword().toCharArray());
            
            // Create a new person
            Map<QName, Serializable> properties = new HashMap<QName, Serializable>(7);
            properties.put(ContentModel.PROP_USERNAME, newUser.getUserName());
            for (NamedValue namedValue : newUser.getProperties())
            {
                properties.put(QName.createQName(namedValue.getName()), namedValue.getValue());
            }
            NodeRef personNodeRef = this.personService.createPerson(properties);
            
            // Add the details to the result
            userDetails[index] = createUserDetails(nodeService, newUser.getUserName(), personNodeRef);
            index++;
        }
                
        return userDetails;
    }

    /**
     * @see org.alfresco.repo.webservice.administration.AdministrationServiceSoapPort#updateUsers(org.alfresco.repo.webservice.administration.UserDetails[])
     */
    public UserDetails[] updateUsers(final UserDetails[] users) throws RemoteException, AdministrationFault
    {
        try
        {
            RetryingTransactionCallback<UserDetails[]> callback = new RetryingTransactionCallback<UserDetails[]>()
            {
                public UserDetails[] execute() throws Exception
                {
                    return updateUsersImpl(users);
                }
            };
            return transactionService.getRetryingTransactionHelper().doInTransaction(callback);
        }
        catch (Throwable exception)
        {
            if (logger.isDebugEnabled())
            {
                logger.error("Unexpected error occurred", exception);
            }
            
            throw new ActionFault(0, exception.getMessage());
        }
    }
    
    /**
     * Update the users details
     * 
     * @param users     the user details to update
     * @return          the updated user details
     */
    private UserDetails[] updateUsersImpl(UserDetails[] users)
    {
        NodeService nodeService = serviceRegistry.getNodeService();
        UserDetails[] userDetails = new UserDetails[users.length];
        
        int index = 0;
        for (UserDetails user : users)
        {
            // Build the property map
            Map<QName, Serializable> properties = new HashMap<QName, Serializable>(7);
            properties.put(ContentModel.PROP_USERNAME, user.getUserName());
            for (NamedValue namedValue : user.getProperties())
            {
                properties.put(QName.createQName(namedValue.getName()), namedValue.getValue());
            }
            
            // Update the properties of the person
            this.personService.setPersonProperties(user.getUserName(), properties);
            
            // Add the details to the result
            NodeRef nodeRef = this.personService.getPerson(user.getUserName());
            userDetails[index] = createUserDetails(nodeService, user.getUserName(), nodeRef);
            index++;
        }
                
        return userDetails;
    }

    /**
     * @see org.alfresco.repo.webservice.administration.AdministrationServiceSoapPort#changePassword(java.lang.String, java.lang.String, java.lang.String)
     */
    public void changePassword(final String userName, final String oldPassword, final String newPassword) throws RemoteException, AdministrationFault
    {
        try
        {
            RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
            {
                public Object execute() throws Exception
                {
                    changePasswordImpl(userName, oldPassword, newPassword);
                    return null;
                }
            };
            transactionService.getRetryingTransactionHelper().doInTransaction(callback);
        }
        catch (Throwable exception)
        {
            if (logger.isDebugEnabled())
            {
                logger.error("Unexpected error occurred", exception);
            }
            
            throw new ActionFault(0, exception.getMessage());
        }
    }

    /**
     * Change the current password of the user
     * 
     * @param userName      the user name
     * @param oldPassword   the old (current) password
     * @param newPassword   the new password
     */
    private void changePasswordImpl(String userName, String oldPassword, String newPassword)
    {
        // Update the authentication details
    	if (this.authenticationService.getCurrentUserName().equals("admin") == true)
    	{
    		this.authenticationService.setAuthentication(userName, newPassword.toCharArray());
    	}
    	else
    	{
    		this.authenticationService.updateAuthentication(userName, oldPassword.toCharArray(), newPassword.toCharArray());
    	}
    }

    /**
     * @see org.alfresco.repo.webservice.administration.AdministrationServiceSoapPort#deleteUsers(java.lang.String[])
     */
    public void deleteUsers(final String[] userNames) throws RemoteException,
            AdministrationFault
    {
        try
        {
            RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>()
            {
                public Object execute() throws Exception
                {
                    deleteUsersImpl(userNames);
                    return null;
                }
            };
            transactionService.getRetryingTransactionHelper().doInTransaction(callback);
        }
        catch (Throwable exception)
        {
            if (logger.isDebugEnabled())
            {
                logger.error("Unexpected error occurred", exception);
            }
            
            throw new ActionFault(0, exception.getMessage());
        }
    }

    /**
     * Delete users
     * 
     * @param userNames     the names of the users to delete
     */
    private void deleteUsersImpl(String[] userNames)
    {
        for (String userName : userNames)
        {
            this.authenticationService.deleteAuthentication(userName);
            this.personService.deletePerson(userName);
        }        
    }
}
