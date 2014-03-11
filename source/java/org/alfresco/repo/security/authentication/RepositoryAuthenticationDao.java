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
package org.alfresco.repo.security.authentication;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.acegisecurity.GrantedAuthority;
import net.sf.acegisecurity.GrantedAuthorityImpl;
import net.sf.acegisecurity.UserDetails;
import net.sf.acegisecurity.providers.dao.User;
import net.sf.acegisecurity.providers.dao.UsernameNotFoundException;
import net.sf.acegisecurity.providers.encoding.PasswordEncoder;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.cache.TransactionalCache;
import org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;

/**
 * Component to provide authentication using native Alfresco authentication
 * 
 * @since 1.2
 */
public class RepositoryAuthenticationDao implements MutableAuthenticationDao, InitializingBean, OnUpdatePropertiesPolicy, BeforeDeleteNodePolicy
{
    private static final StoreRef STOREREF_USERS = new StoreRef("user", "alfrescoUserStore");
    
    private static Log logger = LogFactory.getLog(RepositoryAuthenticationDao.class);

    protected AuthorityService authorityService;
    protected NodeService nodeService;
    protected TenantService tenantService;
    protected NamespacePrefixResolver namespacePrefixResolver;
    protected PasswordEncoder passwordEncoder;
    protected PasswordEncoder sha256PasswordEncoder;
    protected PolicyComponent policyComponent;
    
    private TransactionService transactionService;

    // note: cache is tenant-aware (if using TransctionalCache impl)
    
    private SimpleCache<String, NodeRef> singletonCache; // eg. for user folder nodeRef
    private final String KEY_USERFOLDER_NODEREF = "key.userfolder.noderef";
    
    private SimpleCache<String, CacheEntry> authenticationCache;
    
    public RepositoryAuthenticationDao()
    {
        super();
    }
    
    public void setNamespaceService(NamespacePrefixResolver namespacePrefixResolver)
    {
        this.namespacePrefixResolver = namespacePrefixResolver;
    }

    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
    
    public void setSingletonCache(SimpleCache<String, NodeRef> singletonCache)
    {
        this.singletonCache = singletonCache;
    }
    
    public void setPasswordEncoder(PasswordEncoder passwordEncoder)
    {
        this.passwordEncoder = passwordEncoder;
    }

    public void setSha256PasswordEncoder(PasswordEncoder passwordEncoder)
    {
        this.sha256PasswordEncoder = passwordEncoder;
    }

    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }
    
    public void setAuthenticationCache(SimpleCache<String, CacheEntry> authenticationCache)
    {
        this.authenticationCache = authenticationCache;
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public void afterPropertiesSet() throws Exception
    {
        this.policyComponent.bindClassBehaviour(
                OnUpdatePropertiesPolicy.QNAME,
                ContentModel.TYPE_PERSON,
                new JavaBehaviour(this, "onUpdateProperties"));
        this.policyComponent.bindClassBehaviour(
                BeforeDeleteNodePolicy.QNAME,
                ContentModel.TYPE_USER,
                new JavaBehaviour(this, "beforeDeleteNode"));
        this.policyComponent.bindClassBehaviour(
        		OnUpdatePropertiesPolicy.QNAME,
                ContentModel.TYPE_USER,
                new JavaBehaviour(this, "onUpdateUserProperties"));
    }

    @Override
    public UserDetails loadUserByUsername(String incomingUserName) throws UsernameNotFoundException, DataAccessException
    {
        CacheEntry userEntry = getUserEntryOrNull(incomingUserName);
        if (userEntry == null)
        {
            throw new UsernameNotFoundException("Could not find user by userName: " + incomingUserName);
        }
        UserDetails userDetails = userEntry.userDetails;
        if (userEntry.credentialExpiryDate == null || userEntry.credentialExpiryDate.getTime() >= System.currentTimeMillis())
        {
            return userDetails;
        }
        // If the credentials have expired, we must return a copy with the flag set
        return new User(userDetails.getUsername(), userDetails.getPassword(), userDetails.isEnabled(),
                userDetails.isAccountNonExpired(), false,
                userDetails.isAccountNonLocked(), userDetails.getAuthorities());
    }
    
    /**
     * @param caseSensitiveSearchUserName case sensitive user name
     * @return the user's authentication node ref or null
     */
    public NodeRef getUserOrNull(String caseSensitiveSearchUserName)
    {
        CacheEntry userEntry = getUserEntryOrNull(caseSensitiveSearchUserName);
        return userEntry == null ? null: userEntry.nodeRef;
    }
    
    /**
     * Get the cache entry (or cache it) if the user exists
     * 
     * @param caseSensitiveSearchUserName           the username to search for
     * @return                                      the user's data
     */
    private CacheEntry getUserEntryOrNull(final String caseSensitiveSearchUserName)
    {
        if (caseSensitiveSearchUserName == null || caseSensitiveSearchUserName.length() == 0)
        {
            return null;
        }
        
        class SearchUserNameCallback implements RetryingTransactionCallback<CacheEntry>
        {
            @Override
            public CacheEntry execute() throws Throwable
            {
                CacheEntry cacheEntry = authenticationCache.get(caseSensitiveSearchUserName);
                
                // Check the cache entry if it exists
                if (cacheEntry != null && !nodeService.exists(cacheEntry.nodeRef))
                {
                    logger.warn("Detected state cache entry for '" + caseSensitiveSearchUserName + "'. Node does not exist: " + cacheEntry);
                    // We were about to give out a stale node.  Something went wrong with the cache.
                    // The removal is guaranteed whether we commit or rollback.
                    removeAuthenticationFromCache(caseSensitiveSearchUserName);
                    cacheEntry = null;
                }
                // Check again
                if (cacheEntry != null)
                {
                    // We found what we wanted
                    return cacheEntry;
                }
                
                // Not found, so query
                List<ChildAssociationRef> results = nodeService.getChildAssocs(
                        getUserFolderLocation(caseSensitiveSearchUserName),
                        ContentModel.ASSOC_CHILDREN,
                        QName.createQName(ContentModel.USER_MODEL_URI, caseSensitiveSearchUserName));
                if (!results.isEmpty())
                {
                    // Extract values from the query results
                    NodeRef userRef = tenantService.getName(results.get(0).getChildRef());
                    Map<QName, Serializable> properties = nodeService.getProperties(userRef);
                    String password = DefaultTypeConverter.INSTANCE.convert(String.class,
                            properties.get(ContentModel.PROP_PASSWORD));

                    // Report back the user name as stored on the user
                    String userName = DefaultTypeConverter.INSTANCE.convert(String.class,
                            properties.get(ContentModel.PROP_USER_USERNAME));

                    GrantedAuthority[] gas = new GrantedAuthority[1];
                    gas[0] = new GrantedAuthorityImpl("ROLE_AUTHENTICATED");
        
                    boolean isAdminAuthority = authorityService.isAdminAuthority(userName);
                    
                    Date credentialsExpiryDate = getCredentialsExpiryDate(userName, properties, isAdminAuthority);
                    boolean credentialsHaveNotExpired = (credentialsExpiryDate == null || credentialsExpiryDate.getTime() >= System.currentTimeMillis());
                    
                    UserDetails ud = new User(
                            userName,
                            password,
                            getEnabled(userName, properties, isAdminAuthority),
                            !getHasExpired(userName, properties, isAdminAuthority),
                            credentialsHaveNotExpired,
                            !getLocked(userName, properties, isAdminAuthority),
                            gas);
                    
                    cacheEntry = new CacheEntry(userRef, ud, credentialsExpiryDate);
                    // Only cache positive results
                    authenticationCache.put(caseSensitiveSearchUserName, cacheEntry);
                }
                return cacheEntry;
            }
        }
        
        // Always use a transaction
        return transactionService.getRetryingTransactionHelper().doInTransaction(new SearchUserNameCallback(), true);
    }

    @Override
    public void createUser(String caseSensitiveUserName, char[] rawPassword) throws AuthenticationException
    {
        tenantService.checkDomainUser(caseSensitiveUserName);

        NodeRef userRef = getUserOrNull(caseSensitiveUserName);
        if (userRef != null)
        {
            throw new AuthenticationException("User already exists: " + caseSensitiveUserName);
        }
        NodeRef typesNode = getUserFolderLocation(caseSensitiveUserName);
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(ContentModel.PROP_USER_USERNAME, caseSensitiveUserName);
        String salt = GUID.generate();
        properties.put(ContentModel.PROP_SALT, salt);
        properties.put(ContentModel.PROP_PASSWORD, passwordEncoder.encodePassword(new String(rawPassword), null));
        properties.put(ContentModel.PROP_PASSWORD_SHA256, sha256PasswordEncoder.encodePassword(new String(rawPassword), salt));
        properties.put(ContentModel.PROP_ACCOUNT_EXPIRES, Boolean.valueOf(false));
        properties.put(ContentModel.PROP_CREDENTIALS_EXPIRE, Boolean.valueOf(false));
        properties.put(ContentModel.PROP_ENABLED, Boolean.valueOf(true));
        properties.put(ContentModel.PROP_ACCOUNT_LOCKED, Boolean.valueOf(false));
        nodeService.createNode(typesNode, ContentModel.ASSOC_CHILDREN, QName.createQName(ContentModel.USER_MODEL_URI,
                caseSensitiveUserName), ContentModel.TYPE_USER, properties);
    }
    
    private NodeRef getUserFolderLocation(String caseSensitiveUserName)
    {
        NodeRef userNodeRef = singletonCache.get(KEY_USERFOLDER_NODEREF);
        if (userNodeRef == null)
        {
            QName qnameAssocSystem = QName.createQName("sys", "system", namespacePrefixResolver);
            QName qnameAssocUsers = QName.createQName("sys", "people", namespacePrefixResolver);
            
            //StoreRef userStoreRef = tenantService.getName(caseSensitiveUserName, new StoreRef(STOREREF_USERS.getProtocol(), STOREREF_USERS.getIdentifier()));
            StoreRef userStoreRef = new StoreRef(STOREREF_USERS.getProtocol(), STOREREF_USERS.getIdentifier());
            
            // AR-527
            NodeRef rootNode = nodeService.getRootNode(userStoreRef);
            List<ChildAssociationRef> results = nodeService.getChildAssocs(rootNode, RegexQNamePattern.MATCH_ALL, qnameAssocSystem);
            NodeRef sysNodeRef = null;
            if (results.size() == 0)
            {
                throw new AlfrescoRuntimeException("Required authority system folder path not found: " + qnameAssocSystem);
            }
            else
            {
                sysNodeRef = results.get(0).getChildRef();
            }
            results = nodeService.getChildAssocs(sysNodeRef, RegexQNamePattern.MATCH_ALL, qnameAssocUsers);
            if (results.size() == 0)
            {
                throw new AlfrescoRuntimeException("Required user folder path not found: " + qnameAssocUsers);
            }
            else
            {
                userNodeRef = tenantService.getName(results.get(0).getChildRef());
            }
            singletonCache.put(KEY_USERFOLDER_NODEREF, userNodeRef);
        }
        return userNodeRef;
    }

    @Override
    public void updateUser(String userName, char[] rawPassword) throws AuthenticationException
    {
        NodeRef userRef = getUserOrNull(userName);
        if (userRef == null)
        {
            throw new AuthenticationException("User name does not exist: " + userName);
        }
        Map<QName, Serializable> properties = nodeService.getProperties(userRef);
        String salt = GUID.generate();
        properties.remove(ContentModel.PROP_SALT);
        properties.put(ContentModel.PROP_SALT, salt);
        properties.remove(ContentModel.PROP_PASSWORD);
        properties.put(ContentModel.PROP_PASSWORD, passwordEncoder.encodePassword(new String(rawPassword), null));
        properties.remove(ContentModel.PROP_PASSWORD_SHA256);
        properties.put(ContentModel.PROP_PASSWORD_SHA256, sha256PasswordEncoder.encodePassword(new String(rawPassword), salt));
        nodeService.setProperties(userRef, properties);
    }

    @Override
    public void deleteUser(String userName) throws AuthenticationException
    {
        NodeRef userRef = getUserOrNull(userName);
        if (userRef == null)
        {
            throw new AuthenticationException("User name does not exist: " + userName);
        }
        nodeService.deleteNode(userRef);
    }

    @Override
    public Object getSalt(UserDetails userDetails)
    {
        return null;
    }

    @Override
    public boolean userExists(String userName)
    {
        return (getUserOrNull(userName) != null);
    }
    
    /**
     * @return                  Returns the user properties or <tt>null</tt> if there are none
     */
    protected Map<QName, Serializable> getUserProperties(String userName)
    {
        NodeRef userNodeRef = getUserOrNull(userName);
        if (userNodeRef == null)
        {
            return null;
        }
        return nodeService.getProperties(userNodeRef);
    }

    @Override
    public boolean getAccountExpires(String userName)
    {
        if (authorityService.isAdminAuthority(userName))
        {
            return false;            // Admin never expires
        }
        NodeRef userNode = getUserOrNull(userName);
        if (userNode == null)
        {
            return false;
        }
        Serializable ser = nodeService.getProperty(userNode, ContentModel.PROP_ACCOUNT_EXPIRES);
        if (ser == null)
        {
            return false;
        }
        else
        {
            return DefaultTypeConverter.INSTANCE.booleanValue(ser);
        }
    }

    @Override
    public Date getAccountExpiryDate(String userName)
    {
        NodeRef userNode = getUserOrNull(userName);
        if (userNode == null)
        {
            return null;
        }
        if (DefaultTypeConverter.INSTANCE.booleanValue(nodeService.getProperty(userNode, ContentModel.PROP_ACCOUNT_EXPIRES)))
        {
            return DefaultTypeConverter.INSTANCE.convert(Date.class, nodeService.getProperty(userNode, ContentModel.PROP_ACCOUNT_EXPIRY_DATE));
        }
        else
        {
            return null;
        }
    }

    @Override
    public boolean getAccountHasExpired(String userName)
    {
        return getHasExpired(userName, null, null);
    }

    /**
     * @param userName          the username
     * @param properties        user properties or <tt>null</tt> to fetch them
     */
    protected boolean getHasExpired(String userName, Map<QName, Serializable> properties, Boolean isAdminAuthority)
    {
        if (isAdminAuthority == null)
        {
            isAdminAuthority = authorityService.isAdminAuthority(userName);
        }
        if (isAdminAuthority)
        {
            return false;            // Admin never expires
        }
        if (properties == null)
        {
            properties = getUserProperties(userName);
        }
        if (properties == null)
        {
            return false;
        }
        if (DefaultTypeConverter.INSTANCE.booleanValue(properties.get(ContentModel.PROP_ACCOUNT_EXPIRES)))
        {
            Date date = DefaultTypeConverter.INSTANCE.convert(Date.class, properties.get(ContentModel.PROP_ACCOUNT_EXPIRY_DATE));
            if (date == null)
            {
                return false;
            }
            else
            {
                return (date.getTime() < System.currentTimeMillis());
            }
        }
        else
        {
            return false;
        }
    }
    
    @Override
    public boolean getLocked(String userName)
    {
        return getLocked(userName, null, null);
    }
    
    @Override
    public boolean getAccountlocked(String userName)
    {
        return getLocked(userName, null, null);
    }
    
    /**
     * @param userName          the username
     * @param properties        user properties or <tt>null</tt> to fetch them
     */
    protected boolean getLocked(String userName, Map<QName, Serializable> properties, Boolean isAdminAuthority)
    {
        if (isAdminAuthority == null)
        {
            isAdminAuthority = authorityService.isAdminAuthority(userName);
        }
        if (isAdminAuthority)
        {
            return false;            // Admin is never locked
        }
        if (properties == null)
        {
            properties = getUserProperties(userName);
        }
        if (properties == null)
        {
            return false;
        }
        Serializable ser = properties.get(ContentModel.PROP_ACCOUNT_LOCKED);
        if (ser == null)
        {
            return false;
        }
        else
        {
            return DefaultTypeConverter.INSTANCE.booleanValue(ser);
        }
    }

    @Override
    public boolean getCredentialsExpire(String userName)
    {
        return getCredentialsExpire(userName, null);
    }

    /**
     * @param userName          the username
     * @param properties        user properties or <tt>null</tt> to fetch them
     */
    protected boolean getCredentialsExpire(String userName, Map<QName, Serializable> properties)
    {
        if (authorityService.isAdminAuthority(userName))
        {
            return false;            // Admin never expires
        }
        if (properties == null)
        {
            properties = getUserProperties(userName);
        }
        if (properties == null)
        {
            return false;
        }
        Serializable ser = properties.get(ContentModel.PROP_CREDENTIALS_EXPIRE);
        if (ser == null)
        {
            return false;
        }
        else
        {
            return DefaultTypeConverter.INSTANCE.booleanValue(ser);
        }
    }

    @Override
    public Date getCredentialsExpiryDate(String userName)
    {
        NodeRef userNode = getUserOrNull(userName);
        if (userNode == null)
        {
            return null;
        }
        if (DefaultTypeConverter.INSTANCE.booleanValue(nodeService.getProperty(userNode, ContentModel.PROP_CREDENTIALS_EXPIRE)))
        {
            return DefaultTypeConverter.INSTANCE.convert(Date.class, nodeService.getProperty(userNode, ContentModel.PROP_CREDENTIALS_EXPIRY_DATE));
        }
        else
        {
            return null;
        }
    }

    @Override
    public boolean getCredentialsHaveExpired(String userName)
    {
        return !loadUserByUsername(userName).isCredentialsNonExpired();
    }
    
    /**
     * @param userName              the username (never <tt>null</tt>
     * @param properties            the properties associated with the user or <tt>null</tt> to get them
     * @param isAdminAuthority      is admin authority
     * @return                      <tt>true</tt> if the user account has expired
     */
    protected boolean getCredentialsHaveExpired(String userName, Map<QName, Serializable> properties, Boolean isAdminAuthority)
    {
        Date credentialsExpiryDate = getCredentialsExpiryDate(userName, properties, isAdminAuthority);
        boolean credentialsHaveNotExpired = (credentialsExpiryDate == null || credentialsExpiryDate.getTime() >= System.currentTimeMillis());
        return (! credentialsHaveNotExpired);
    }
    
    /**
     * @param userName              the username (never <tt>null</tt>
     * @param properties            the properties associated with the user or <tt>null</tt> to get them
     * @param isAdminAuthority      is admin authority
     * @return                      Date on which the credentials expire or <tt>null</tt> if they never expire
     */
    private Date getCredentialsExpiryDate(String userName, Map<QName, Serializable> properties, Boolean isAdminAuthority)
    {
        if (isAdminAuthority == null)
        {
            isAdminAuthority = authorityService.isAdminAuthority(userName);
        }
        if (isAdminAuthority)
        {
            return null;            // Admin never expires
        }
        if (properties == null)
        {
            properties = getUserProperties(userName);
        }
        if (DefaultTypeConverter.INSTANCE.booleanValue(properties.get(ContentModel.PROP_CREDENTIALS_EXPIRE)))
        {
            return DefaultTypeConverter.INSTANCE.convert(Date.class, properties.get(ContentModel.PROP_CREDENTIALS_EXPIRY_DATE));
        }
        return null;
    }
    
    @Override
    public boolean getEnabled(String userName)
    {
        return getEnabled(userName, null, null);
    }
    
    /**
     * @param userName              the username
     * @param properties            the user's properties or <tt>null</tt>
     */
    protected boolean getEnabled(String userName, Map<QName, Serializable> properties, Boolean isAdminAuthority)
    {
        if (isAdminAuthority == null)
        {
            isAdminAuthority = authorityService.isAdminAuthority(userName);
        }
        if (isAdminAuthority)
        {
            return true;            // Admin is always enabled
        }
        if (properties == null)
        {
            properties = getUserProperties(userName);
        }
        if (properties == null)
        {
            return false;
        }
        Serializable ser = properties.get(ContentModel.PROP_ENABLED);
        if (ser == null)
        {
            return true;
        }
        else
        {
            return DefaultTypeConverter.INSTANCE.booleanValue(ser);
        }
    }

    @Override
    public void setAccountExpires(String userName, boolean expires)
    {
        NodeRef userNode = getUserOrNull(userName);
        if (userNode == null)
        {
            throw new AuthenticationException("User not found: " + userName);
        }
        nodeService.setProperty(userNode, ContentModel.PROP_ACCOUNT_EXPIRES, Boolean.valueOf(expires));
    }

    @Override
    public void setAccountExpiryDate(String userName, Date exipryDate)
    {
        NodeRef userNode = getUserOrNull(userName);
        if (userNode == null)
        {
            throw new AuthenticationException("User not found: " + userName);
        }
        nodeService.setProperty(userNode, ContentModel.PROP_ACCOUNT_EXPIRY_DATE, exipryDate);

    }

    @Override
    public void setCredentialsExpire(String userName, boolean expires)
    {
        NodeRef userNode = getUserOrNull(userName);
        if (userNode == null)
        {
            throw new AuthenticationException("User not found: " + userName);
        }
        nodeService.setProperty(userNode, ContentModel.PROP_CREDENTIALS_EXPIRE, Boolean.valueOf(expires));
    }

    @Override
    public void setCredentialsExpiryDate(String userName, Date exipryDate)
    {
        NodeRef userNode = getUserOrNull(userName);
        if (userNode == null)
        {
            throw new AuthenticationException("User not found: " + userName);
        }
        nodeService.setProperty(userNode, ContentModel.PROP_CREDENTIALS_EXPIRY_DATE, exipryDate);

    }

    @Override
    public void setEnabled(String userName, boolean enabled)
    {
        if (!enabled && authorityService.isAdminAuthority(userName))
        {
            // Ignore this
            return;
        }
        NodeRef userNode = getUserOrNull(userName);
        if (userNode == null)
        {
            throw new AuthenticationException("User not found: " + userName);
        }
        nodeService.setProperty(userNode, ContentModel.PROP_ENABLED, Boolean.valueOf(enabled));
    }

    @Override
    public void setLocked(String userName, boolean locked)
    {
        NodeRef userNode = getUserOrNull(userName);
        if (userNode == null)
        {
            throw new AuthenticationException("User not found: " + userName);
        }
        nodeService.setProperty(userNode, ContentModel.PROP_ACCOUNT_LOCKED, Boolean.valueOf(locked));
    }

    @Override
    public String getMD4HashedPassword(String userName)
    {
        NodeRef userNode = getUserOrNull(userName);
        if (userNode == null)
        {
            return null;
        }
        else
        {
            String password = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(userNode, ContentModel.PROP_PASSWORD));
            return password;
        }
    }

    @Override
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
    	String uidBefore = DefaultTypeConverter.INSTANCE.convert(String.class, before.get(ContentModel.PROP_USERNAME));
        String uidAfter = DefaultTypeConverter.INSTANCE.convert(String.class, after.get(ContentModel.PROP_USERNAME));
        if (uidBefore != null && !EqualsHelper.nullSafeEquals(uidBefore, uidAfter))
        {
            NodeRef userNode = getUserOrNull(uidBefore);
            if (userNode != null)
            {
                nodeService.setProperty(userNode, ContentModel.PROP_USER_USERNAME, uidAfter);
                nodeService.moveNode(userNode, nodeService.getPrimaryParent(userNode).getParentRef(),
                        ContentModel.ASSOC_CHILDREN, QName.createQName(ContentModel.USER_MODEL_URI, uidAfter));
                removeAuthenticationFromCache(uidBefore);
            }
        }
        removeAuthenticationFromCache(uidAfter);
    }
    
    public void onUpdateUserProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        String uidBefore = DefaultTypeConverter.INSTANCE.convert(String.class, before.get(ContentModel.PROP_USER_USERNAME));
        if (uidBefore != null)
        {
            removeAuthenticationFromCache(uidBefore);
        }
    }

    @Override
    public void beforeDeleteNode(NodeRef nodeRef)
    {
        String userName = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_USER_USERNAME);
        if (userName != null)
        {
            removeAuthenticationFromCache(userName);
        }
    }        
    
    /**
     * Remove from the cache and lock the value for the transaction
     * @param key
     * @param lock
     */
    private void removeAuthenticationFromCache(String key)
    {
        authenticationCache.remove(key);
        if (authenticationCache instanceof TransactionalCache)
        {
            TransactionalCache<String, CacheEntry> authenticationCacheTxn = (TransactionalCache<String, CacheEntry>) authenticationCache;
            authenticationCacheTxn.lockValue(key);
        }
    }
    
    static class CacheEntry implements Serializable
    {
        private static final long serialVersionUID = 1L;
        public NodeRef nodeRef;
        public UserDetails userDetails;
        public Date credentialExpiryDate;

        public CacheEntry(NodeRef nodeRef, UserDetails userDetails, Date credentialExpiryDate)
        {
            this.nodeRef = nodeRef;
            this.userDetails = userDetails;
            this.credentialExpiryDate = credentialExpiryDate;
        }

        @Override
        public String toString()
        {
            return "CacheEntry [nodeRef=" + nodeRef + ", userDetails=" + userDetails + "]";
        }        
    }
}
