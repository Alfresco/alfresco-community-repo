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
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.simple.permission.AuthorityCapabilityRegistry;
import org.springframework.dao.DataAccessException;

public class RepositoryAuthenticationDao implements MutableAuthenticationDao
{
    private static final StoreRef STOREREF_USERS = new StoreRef("user", "alfrescoUserStore");

    private NodeService nodeService;
    private TenantService tenantService;

    private NamespacePrefixResolver namespacePrefixResolver;

    @SuppressWarnings("unused")
    private DictionaryService dictionaryService;

    private SearchService searchService;

    private PasswordEncoder passwordEncoder;
    
    private AuthorityCapabilityRegistry authorityCapabilityRegistry;

    private boolean userNamesAreCaseSensitive;

    public boolean getUserNamesAreCaseSensitive()
    {
        return userNamesAreCaseSensitive;
    }

    public void setUserNamesAreCaseSensitive(boolean userNamesAreCaseSensitive)
    {
        this.userNamesAreCaseSensitive = userNamesAreCaseSensitive;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public void setNamespaceService(NamespacePrefixResolver namespacePrefixResolver)
    {
        this.namespacePrefixResolver = namespacePrefixResolver;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    public void setPasswordEncoder(PasswordEncoder passwordEncoder)
    {
        this.passwordEncoder = passwordEncoder;
    }

    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }
    
    public void setAuthorityCapabilityRegistry(AuthorityCapabilityRegistry registry)
    {
        this.authorityCapabilityRegistry = registry;
    }

    public UserDetails loadUserByUsername(String incomingUserName) throws UsernameNotFoundException,
            DataAccessException
    {
        NodeRef userRef = getUserOrNull(incomingUserName);
        if (userRef == null)
        {
            throw new UsernameNotFoundException("Could not find user by userName: " + incomingUserName);
        }

        Map<QName, Serializable> properties = nodeService.getProperties(userRef);
        String password = DefaultTypeConverter.INSTANCE.convert(String.class, properties
                .get(ContentModel.PROP_PASSWORD));

        // Report back the user name as stored on the user
        String userName = DefaultTypeConverter.INSTANCE.convert(String.class, properties
                .get(ContentModel.PROP_USER_USERNAME));

        GrantedAuthority[] gas = new GrantedAuthority[1];
        gas[0] = new GrantedAuthorityImpl("ROLE_AUTHENTICATED");

        UserDetails ud = new User(userName, password, getEnabled(userRef), !getAccountHasExpired(userRef),
                !getCredentialsHaveExpired(userRef), !getAccountlocked(userRef), gas);
        return ud;
    }

    public NodeRef getUserOrNull(String searchUserName)
    {
        if(searchUserName == null)
        {
            return null;
        }
        if(searchUserName.length() == 0)
        {
            return null;
        }
        
        SearchParameters sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery("@usr\\:username:\"" + searchUserName + "\"");
       
        try
        {
            sp.addStore(tenantService.getName(searchUserName, STOREREF_USERS));
        }
        catch (AlfrescoRuntimeException e)
        {
            return null; // no such tenant or tenant not enabled
        }

        sp.excludeDataInTheCurrentTransaction(false);

        ResultSet rs = null;

        try
        {
            rs = searchService.query(sp);

            NodeRef returnRef = null;

            for (ResultSetRow row : rs)
            {

                NodeRef nodeRef = row.getNodeRef();
                if (nodeService.exists(nodeRef))
                {
                    String realUserName = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(
                            nodeRef, ContentModel.PROP_USER_USERNAME));

                    if (userNamesAreCaseSensitive)
                    {
                        if (realUserName.equals(searchUserName))
                        {
                            if(returnRef == null)
                            {
                               returnRef = nodeRef;
                            }
                            else
                            {
                                throw new AlfrescoRuntimeException("Found more than one user for "+searchUserName+ " (case sensitive)");
                            }
                        }
                    }
                    else
                    {
                        if (realUserName.equalsIgnoreCase(searchUserName))
                        {
                            if(returnRef == null)
                            {
                               returnRef = nodeRef;
                            }
                            else
                            {
                                throw new AlfrescoRuntimeException("Found more than one user for "+searchUserName+ " (case insensitive)");
                            }
                        }
                    }
                }
            }
            
            return returnRef;
        }
        finally
        {
            if (rs != null)
            {
                rs.close();
            }
        }
    }

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
        String salt = null; // GUID.generate();
        properties.put(ContentModel.PROP_SALT, salt);
        properties.put(ContentModel.PROP_PASSWORD, passwordEncoder.encodePassword(new String(rawPassword), salt));
        properties.put(ContentModel.PROP_ACCOUNT_EXPIRES, Boolean.valueOf(false));
        properties.put(ContentModel.PROP_CREDENTIALS_EXPIRE, Boolean.valueOf(false));
        properties.put(ContentModel.PROP_ENABLED, Boolean.valueOf(true));
        properties.put(ContentModel.PROP_ACCOUNT_LOCKED, Boolean.valueOf(false));
        nodeService.createNode(typesNode, ContentModel.ASSOC_CHILDREN, ContentModel.TYPE_USER, ContentModel.TYPE_USER,
                properties);
        authorityCapabilityRegistry.addAuthority(caseSensitiveUserName, null);
    }

    private NodeRef getUserFolderLocation(String caseSensitiveUserName)
    {
        QName qnameAssocSystem = QName.createQName("sys", "system", namespacePrefixResolver);
        QName qnameAssocUsers = QName.createQName("sys", "people", namespacePrefixResolver); // see

        StoreRef userStoreRef = tenantService.getName(caseSensitiveUserName, new StoreRef(STOREREF_USERS.getProtocol(), STOREREF_USERS.getIdentifier()));

        // AR-527
        NodeRef rootNode = nodeService.getRootNode(userStoreRef);
        List<ChildAssociationRef> results = nodeService.getChildAssocs(rootNode, RegexQNamePattern.MATCH_ALL,
                qnameAssocSystem);
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
        NodeRef userNodeRef = null;
        if (results.size() == 0)
        {
            throw new AlfrescoRuntimeException("Required user folder path not found: " + qnameAssocUsers);
        }
        else
        {
            userNodeRef = results.get(0).getChildRef();
        }
        return userNodeRef;
    }

    public void updateUser(String userName, char[] rawPassword) throws AuthenticationException
    {
        NodeRef userRef = getUserOrNull(userName);
        if (userRef == null)
        {
            throw new AuthenticationException("User name does not exist: " + userName);
        }
        Map<QName, Serializable> properties = nodeService.getProperties(userRef);
        String salt = null; // GUID.generate();
        properties.remove(ContentModel.PROP_SALT);
        properties.put(ContentModel.PROP_SALT, salt);
        properties.remove(ContentModel.PROP_PASSWORD);
        properties.put(ContentModel.PROP_PASSWORD, passwordEncoder.encodePassword(new String(rawPassword), salt));
        nodeService.setProperties(userRef, properties);
    }

    public void deleteUser(String userName) throws AuthenticationException
    {
        NodeRef userRef = getUserOrNull(userName);
        if (userRef == null)
        {
            throw new AuthenticationException("User name does not exist: " + userName);
        }
        nodeService.deleteNode(userRef);
        authorityCapabilityRegistry.removeAuthority(userName);
    }

    public Object getSalt(UserDetails userDetails)
    {
        // NodeRef userRef = getUserOrNull(userDetails.getUsername());
        // if (userRef == null)
        // {
        // throw new UsernameNotFoundException("Could not find user by userName:
        // " + userDetails.getUsername());
        // }
        //
        // Map<QName, Serializable> properties =
        // nodeService.getProperties(userRef);
        //
        // String salt = DefaultTypeConverter.INSTANCE.convert(String.class,
        // properties.get(QName.createQName("usr", "salt",
        // namespacePrefixResolver)));
        //
        // return salt;
        return null;
    }

    public boolean userExists(String userName)
    {
        return (getUserOrNull(userName) != null);
    }

    public boolean getAccountExpires(String userName)
    {
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

    public Date getAccountExpiryDate(String userName)
    {
        NodeRef userNode = getUserOrNull(userName);
        if (userNode == null)
        {
            return null;
        }
        if (DefaultTypeConverter.INSTANCE.booleanValue(nodeService.getProperty(userNode,
                ContentModel.PROP_ACCOUNT_EXPIRES)))
        {
            return DefaultTypeConverter.INSTANCE.convert(Date.class, nodeService.getProperty(userNode,
                    ContentModel.PROP_ACCOUNT_EXPIRY_DATE));
        }
        else
        {
            return null;
        }
    }

    public boolean getAccountHasExpired(String userName)
    {
        return getAccountHasExpired(getUserOrNull(userName));
    }

    private boolean getAccountHasExpired(NodeRef userNode)
    {
        if (userNode == null)
        {
            return false;
        }
        if (DefaultTypeConverter.INSTANCE.booleanValue(nodeService.getProperty(userNode,
                ContentModel.PROP_ACCOUNT_EXPIRES)))
        {
            Date date = DefaultTypeConverter.INSTANCE.convert(Date.class, nodeService.getProperty(userNode,
                    ContentModel.PROP_ACCOUNT_EXPIRY_DATE));
            if (date == null)
            {
                return false;
            }
            else
            {
                return (date.compareTo(new Date()) < 1);
            }
        }
        else
        {
            return false;
        }
    }

    public boolean getAccountlocked(String userName)
    {
        return getAccountlocked(getUserOrNull(userName));
    }

    private boolean getAccountlocked(NodeRef userNode)
    {
        if (userNode == null)
        {
            return false;
        }
        Serializable ser = nodeService.getProperty(userNode, ContentModel.PROP_ACCOUNT_LOCKED);
        if (ser == null)
        {
            return false;
        }
        else
        {
            return DefaultTypeConverter.INSTANCE.booleanValue(ser);
        }
    }

    public boolean getCredentialsExpire(String userName)
    {
        return getCredentialsExpired(getUserOrNull(userName));
    }

    private boolean getCredentialsExpired(NodeRef userNode)
    {
        if (userNode == null)
        {
            return false;
        }
        Serializable ser = nodeService.getProperty(userNode, ContentModel.PROP_CREDENTIALS_EXPIRE);
        if (ser == null)
        {
            return false;
        }
        else
        {
            return DefaultTypeConverter.INSTANCE.booleanValue(ser);
        }
    }

    public Date getCredentialsExpiryDate(String userName)
    {
        NodeRef userNode = getUserOrNull(userName);
        if (userNode == null)
        {
            return null;
        }
        if (DefaultTypeConverter.INSTANCE.booleanValue(nodeService.getProperty(userNode,
                ContentModel.PROP_CREDENTIALS_EXPIRE)))
        {
            return DefaultTypeConverter.INSTANCE.convert(Date.class, nodeService.getProperty(userNode,
                    ContentModel.PROP_CREDENTIALS_EXPIRY_DATE));
        }
        else
        {
            return null;
        }
    }

    public boolean getCredentialsHaveExpired(String userName)
    {
        return getCredentialsHaveExpired(getUserOrNull(userName));
    }

    private boolean getCredentialsHaveExpired(NodeRef userNode)
    {
        if (userNode == null)
        {
            return false;
        }
        if (DefaultTypeConverter.INSTANCE.booleanValue(nodeService.getProperty(userNode,
                ContentModel.PROP_CREDENTIALS_EXPIRE)))
        {
            Date date = DefaultTypeConverter.INSTANCE.convert(Date.class, nodeService.getProperty(userNode,
                    ContentModel.PROP_CREDENTIALS_EXPIRY_DATE));
            if (date == null)
            {
                return false;
            }
            else
            {
                return (date.compareTo(new Date()) < 1);
            }
        }
        else
        {
            return false;
        }
    }

    public boolean getEnabled(String userName)
    {
        return getEnabled(getUserOrNull(userName));
    }

    private boolean getEnabled(NodeRef userNode)
    {
        if (userNode == null)
        {
            return false;
        }
        Serializable ser = nodeService.getProperty(userNode, ContentModel.PROP_ENABLED);
        if (ser == null)
        {
            return true;
        }
        else
        {
            return DefaultTypeConverter.INSTANCE.booleanValue(ser);
        }
    }

    public void setAccountExpires(String userName, boolean expires)
    {
        NodeRef userNode = getUserOrNull(userName);
        if (userNode == null)
        {
            throw new AuthenticationException("User not found: " + userName);
        }
        nodeService.setProperty(userNode, ContentModel.PROP_ACCOUNT_EXPIRES, Boolean.valueOf(expires));
    }

    public void setAccountExpiryDate(String userName, Date exipryDate)
    {
        NodeRef userNode = getUserOrNull(userName);
        if (userNode == null)
        {
            throw new AuthenticationException("User not found: " + userName);
        }
        nodeService.setProperty(userNode, ContentModel.PROP_ACCOUNT_EXPIRY_DATE, exipryDate);

    }

    public void setCredentialsExpire(String userName, boolean expires)
    {
        NodeRef userNode = getUserOrNull(userName);
        if (userNode == null)
        {
            throw new AuthenticationException("User not found: " + userName);
        }
        nodeService.setProperty(userNode, ContentModel.PROP_CREDENTIALS_EXPIRE, Boolean.valueOf(expires));
    }

    public void setCredentialsExpiryDate(String userName, Date exipryDate)
    {
        NodeRef userNode = getUserOrNull(userName);
        if (userNode == null)
        {
            throw new AuthenticationException("User not found: " + userName);
        }
        nodeService.setProperty(userNode, ContentModel.PROP_CREDENTIALS_EXPIRY_DATE, exipryDate);

    }

    public void setEnabled(String userName, boolean enabled)
    {
        NodeRef userNode = getUserOrNull(userName);
        if (userNode == null)
        {
            throw new AuthenticationException("User not found: " + userName);
        }
        nodeService.setProperty(userNode, ContentModel.PROP_ENABLED, Boolean.valueOf(enabled));
    }

    public void setLocked(String userName, boolean locked)
    {
        NodeRef userNode = getUserOrNull(userName);
        if (userNode == null)
        {
            throw new AuthenticationException("User not found: " + userName);
        }
        nodeService.setProperty(userNode, ContentModel.PROP_ACCOUNT_LOCKED, Boolean.valueOf(locked));
    }

    public String getMD4HashedPassword(String userName)
    {
        NodeRef userNode = getUserOrNull(userName);
        if (userNode == null)
        {
            return null;
        }
        else
        {
            String password = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(userNode,
                    ContentModel.PROP_PASSWORD));
            return password;
        }
    }

}
