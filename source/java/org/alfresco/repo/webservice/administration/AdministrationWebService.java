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

import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.transaction.TransactionComponent;
import org.alfresco.repo.transaction.TransactionUtil;
import org.alfresco.repo.transaction.TransactionUtil.TransactionWork;
import org.alfresco.repo.webservice.AbstractWebService;
import org.alfresco.repo.webservice.Utils;
import org.alfresco.repo.webservice.action.ActionFault;
import org.alfresco.repo.webservice.types.NamedValue;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
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
    private TransactionComponent transactionService = null;
    
    /** A set of ignored properties */
    private static Set<QName> ignoredProperties = new HashSet<QName>(3);
    
    /** Simple cache used to store user query sessions */
    private SimpleCache<String, UserQuerySession> querySessionCache;
    
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
    public void setTransactionService(TransactionComponent transactionService)
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
     * Sets the instance of the SimpleCache to be used
     * 
     * @param querySessionCache
     *            The SimpleCache
     */
    public void setQuerySessionCache(
            SimpleCache<String, UserQuerySession> querySessionCache)
    {
        this.querySessionCache = querySessionCache;
    }
    
    /**
     * @see org.alfresco.repo.webservice.administration.AdministrationServiceSoapPort#queryUsers(org.alfresco.repo.webservice.administration.UserFilter)
     */
    public UserQueryResults queryUsers(final UserFilter filter)
            throws RemoteException, AdministrationFault
    {
        try
        {
            return TransactionUtil.executeInUserTransaction(this.transactionService, new TransactionWork<UserQueryResults>()
            {
                public UserQueryResults doWork() throws Exception
                {
                    return queryUsersImpl(filter);
                }
            });
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
        
        // Create a user query session
        UserQuerySession userQuerySession = new UserQuerySession(Utils.getBatchSize(msgContext), filter);
        UserQueryResults userQueryResults = userQuerySession.getNextBatch();

        // add the session to the cache if there are more results to come
        if (userQueryResults.getQuerySession() != null)
        {
            // this.querySessionCache.putQuerySession(querySession);
            this.querySessionCache.put(userQueryResults.getQuerySession(), userQuerySession);
        }
        
        return userQueryResults;
    }    

    /**
     *  @see org.alfresco.repo.webservice.administration.AdministrationServiceSoapPort#fetchMoreUsers(java.lang.String)
     */
    public UserQueryResults fetchMoreUsers(final String querySession)
            throws RemoteException, AdministrationFault
    {
        try
        {
            return TransactionUtil.executeInUserTransaction(this.transactionService, new TransactionWork<UserQueryResults>()
            {
                public UserQueryResults doWork() throws Exception
                {
                    return fetchMoreUsersImpl(querySession);
                }
            });
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
     * @param querySession
     * @return
     */
    private UserQueryResults fetchMoreUsersImpl(String querySession)
    {
        UserQueryResults queryResult = null;
        UserQuerySession session = this.querySessionCache.get(querySession);
        
        if (session != null)
        {
            queryResult = session.getNextBatch();
            if (queryResult.getQuerySession() == null)
            {
                this.querySessionCache.remove(querySession);
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
            return TransactionUtil.executeInUserTransaction(this.transactionService, new TransactionWork<UserDetails>()
            {
                public UserDetails doWork() throws Exception
                {
                    return getUserImpl(userName);
                }
            });
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
        UserDetails userDetails = null;
        
        if (this.personService.personExists(userName) == true)
        {
            NodeRef nodeRef = this.personService.getPerson(userName);            
            userDetails = createUserDetails(userName, nodeRef);
        }
        else
        {
            // Throw an exception to indicate that the user does not exist
            throw new RuntimeException(MessageFormat.format("The user with name {0} does not exist.", new Object[]{userName}));
        }
        
        return userDetails;
    }

    /**
     * Given a valid person node reference will create a user details object
     * 
     * @param nodeRef   the node reference
     * @return          the user details object populated with the appropriate property values
     */
    private UserDetails createUserDetails(String userName, NodeRef nodeRef)
    {
        // Create the user details object
        UserDetails userDetails = new UserDetails();
        
        // Set the user name
        userDetails.setUserName(userName);
        
        // Set the various property values
        Map<QName, Serializable> properties = this.nodeService.getProperties(nodeRef);
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
            return TransactionUtil.executeInUserTransaction(this.transactionService, new TransactionWork<UserDetails[]>()
            {
                public UserDetails[] doWork() throws Exception
                {
                    return createUsersImpl(newUsers);
                }
            });
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
            userDetails[index] = createUserDetails(newUser.getUserName(), personNodeRef);
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
            return TransactionUtil.executeInUserTransaction(this.transactionService, new TransactionWork<UserDetails[]>()
            {
                public UserDetails[] doWork() throws Exception
                {
                    return updateUsersImpl(users);
                }
            });
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
            userDetails[index] = createUserDetails(user.getUserName(), nodeRef);
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
            TransactionUtil.executeInUserTransaction(this.transactionService, new TransactionWork<Object>()
            {
                public Object doWork() throws Exception
                {
                    changePasswordImpl(userName, oldPassword, newPassword);
                    return null;
                }
            });
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
        this.authenticationService.updateAuthentication(userName, oldPassword.toCharArray(), newPassword.toCharArray());        
    }

    /**
     * @see org.alfresco.repo.webservice.administration.AdministrationServiceSoapPort#deleteUsers(java.lang.String[])
     */
    public void deleteUsers(final String[] userNames) throws RemoteException,
            AdministrationFault
    {
        try
        {
            TransactionUtil.executeInUserTransaction(this.transactionService, new TransactionWork<Object>()
            {
                public Object doWork() throws Exception
                {
                    deleteUsersImpl(userNames);
                    return null;
                }
            });
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
    
    /**
     * User query session used to support batched user query
     * 
     * @author Roy Wetherall
     */
    private class UserQuerySession implements Serializable
    {
        private static final long serialVersionUID = -2960711874297744356L;
        
        private int batchSize = -1;        
        private UserFilter filter;
        protected int position = 0;        
        private String id;
        
        /** 
         * Constructor
         * 
         * @param batchSize
         * @param filter
         */
        public UserQuerySession(int batchSize, UserFilter filter)
        {
            this.batchSize = batchSize;
            this.filter = filter;
            this.id = GUID.generate();
        }
                
        /**
         * @see org.alfresco.repo.webservice.repository.QuerySession#getId()
         */
        public String getId()
        {
           return this.id;
        }
      
        /**
         * Calculates the index of the last row to retrieve. 
         * 
         * @param totalRowCount The total number of rows in the results
         * @return The index of the last row to return
         */
        protected int calculateLastRowIndex(int totalRowCount)
        {
           int lastRowIndex = totalRowCount;
           
           // set the last row index if there are more results available 
           // than the batch size
           if ((this.batchSize != -1) && ((this.position + this.batchSize) < totalRowCount))
           {
              lastRowIndex = this.position + this.batchSize;
           }
           
           return lastRowIndex;
        }
        
        /**
         * Calculates the value of the next position.
         * If the end of the result set is reached the position is set to -1
         * 
         * @param totalRowCount The total number of rows in the results
         * @param queryResult The QueryResult object being returned to the client,
         * if there are no more results the id is removed from the QueryResult instance
         */
        protected void updatePosition(int totalRowCount, UserQueryResults queryResult)
        {
           if (this.batchSize == -1)
           {
               this.position = -1;
               queryResult.setQuerySession(null);
           }
           else
           {
               this.position += this.batchSize;
               if (this.position >= totalRowCount)
               {
                  // signify that there are no more results 
                  this.position = -1;
                  queryResult.setQuerySession(null);
               }
           }
        }
        
        /**
         * Gets the next batch of user details
         * 
         * @return  user query results
         */
        public UserQueryResults getNextBatch()
        {
            UserQueryResults queryResult = null;
            
            if (this.position != -1)
            {
               if (logger.isDebugEnabled())
                  logger.debug("Before getNextBatch: " + toString());
               
               Set<NodeRef> nodeRefs = AdministrationWebService.this.personService.getAllPeople();
               
               // TODO do the filter of the resulting list here ....
               List<NodeRef> filteredNodeRefs = new ArrayList<NodeRef>(nodeRefs);
               
               int totalRows = filteredNodeRefs.size();
               int lastRow = calculateLastRowIndex(totalRows);
               int currentBatchSize = lastRow - this.position;
               
               if (logger.isDebugEnabled())
                  logger.debug("Total rows = " + totalRows + ", current batch size = " + currentBatchSize);
               
               List<UserDetails> userDetailsList = new ArrayList<UserDetails>(currentBatchSize);
               
               for (int x = this.position; x < lastRow; x++)
               {
                  NodeRef nodeRef = (NodeRef)filteredNodeRefs.get(x);
                  String userName = (String)AdministrationWebService.this.nodeService.getProperty(nodeRef, ContentModel.PROP_USERNAME);
                  UserDetails userDetails = AdministrationWebService.this.createUserDetails(userName, nodeRef);
                  userDetailsList.add(userDetails);
               }
               
               queryResult = new UserQueryResults(getId(), (UserDetails[])userDetailsList.toArray(new UserDetails[userDetailsList.size()]));
               
               // move the position on
               updatePosition(totalRows, queryResult);
               
               if (logger.isDebugEnabled())
                  logger.debug("After getNextBatch: " + toString());
            }
            
            return queryResult;
        }
    }
}
