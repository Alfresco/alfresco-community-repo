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
import org.alfresco.repo.transaction.TransactionComponent;
import org.alfresco.repo.transaction.TransactionUtil;
import org.alfresco.repo.transaction.TransactionUtil.TransactionWork;
import org.alfresco.repo.webservice.AbstractWebService;
import org.alfresco.repo.webservice.action.ActionFault;
import org.alfresco.repo.webservice.types.NamedValue;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
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
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.webservice.administration.AdministrationServiceSoapPort#queryUsers(org.alfresco.repo.webservice.administration.UserFilter)
     */
    public UserQueryResults queryUsers(UserFilter filter)
            throws RemoteException, AdministrationFault
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.webservice.administration.AdministrationServiceSoapPort#fetchMoreUsers(java.lang.String)
     */
    public UserQueryResults fetchMoreUsers(String querySession)
            throws RemoteException, AdministrationFault
    {
        // TODO Auto-generated method stub
        return null;
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
                namedValues.add(new NamedValue(entry.getKey().toString(), value));
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
            this.personService.deletePerson(userName);
        }        
    }
}
