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

import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.QueryParameterDefImpl;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.springframework.dao.DataAccessException;

public class RepositoryAuthenticationDao implements MutableAuthenticationDao
{

    private static final String SYSTEM_FOLDER = "/sys:system";

    private  static final String PEOPLE_FOLDER = SYSTEM_FOLDER + "/sys:people";

    private NodeService nodeService;

    private NamespacePrefixResolver namespacePrefixResolver;

    private DictionaryService dictionaryService;

    private SearchService searchService;

    private PasswordEncoder passwordEncoder;

    private StoreRef userStoreRef;
    
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

    public void setPasswordEncoder(PasswordEncoder passwordEncoder)
    {
        this.passwordEncoder = passwordEncoder;
    }

    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    public UserDetails loadUserByUsername(String caseSensitiveUserName) throws UsernameNotFoundException, DataAccessException
    {
        String userName = userNamesAreCaseSensitive ? caseSensitiveUserName: caseSensitiveUserName.toLowerCase();
        NodeRef userRef = getUserOrNull(userNamesAreCaseSensitive ? userName: userName.toLowerCase());
        if (userRef == null)
        {
            throw new UsernameNotFoundException("Could not find user by userName: " + caseSensitiveUserName);
        }

        Map<QName, Serializable> properties = nodeService.getProperties(userRef);
        String password = DefaultTypeConverter.INSTANCE.convert(String.class, properties
                .get(ContentModel.PROP_PASSWORD));

        GrantedAuthority[] gas = new GrantedAuthority[1];
        gas[0] = new GrantedAuthorityImpl("ROLE_AUTHENTICATED");

        UserDetails ud = new User(userName, password, getEnabled(userRef), !getAccountHasExpired(userRef),
                !getCredentialsHaveExpired(userRef), !getAccountlocked(userRef), gas);
        return ud;
    }

    public NodeRef getUserOrNull(String caseSensitiveUserName)
    {
        String userName = userNamesAreCaseSensitive ? caseSensitiveUserName: caseSensitiveUserName.toLowerCase();
        NodeRef rootNode = nodeService.getRootNode(getUserStoreRef());
        QueryParameterDefinition[] defs = new QueryParameterDefinition[1];
        DataTypeDefinition text = dictionaryService.getDataType(DataTypeDefinition.TEXT);
        defs[0] = new QueryParameterDefImpl(QName.createQName("usr", "var", namespacePrefixResolver), text, true,
                userName);
        List<NodeRef> results = searchService.selectNodes(rootNode, PEOPLE_FOLDER
                + "/usr:user[@usr:username = $usr:var ]", defs, namespacePrefixResolver, false);
        if (results.size() != 1)
        {
            return null;
        }
        return results.get(0);
    }

    public void createUser(String caseSensitiveUserName, char[] rawPassword) throws AuthenticationException
    {
        String userName = userNamesAreCaseSensitive ? caseSensitiveUserName: caseSensitiveUserName.toLowerCase();
        NodeRef userRef = getUserOrNull(userName);
        if (userRef != null)
        {
            throw new AuthenticationException("User already exists: " + userName);
        }
        NodeRef typesNode = getOrCreateTypeLocation();
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(ContentModel.PROP_USER_USERNAME, userName);
        String salt = null; // GUID.generate();
        properties.put(ContentModel.PROP_SALT, salt);
        properties.put(ContentModel.PROP_PASSWORD, passwordEncoder.encodePassword(new String(rawPassword), salt));
        properties.put(ContentModel.PROP_ACCOUNT_EXPIRES, Boolean.valueOf(false));
        properties.put(ContentModel.PROP_CREDENTIALS_EXPIRE, Boolean.valueOf(false));
        properties.put(ContentModel.PROP_ENABLED, Boolean.valueOf(true));
        properties.put(ContentModel.PROP_ACCOUNT_LOCKED, Boolean.valueOf(false));
        nodeService.createNode(typesNode, ContentModel.ASSOC_CHILDREN, ContentModel.TYPE_USER, ContentModel.TYPE_USER,
                properties);

    }

    private NodeRef getOrCreateTypeLocation()
    {
        NodeRef rootNode = nodeService.getRootNode(getUserStoreRef());
        List<ChildAssociationRef> results = nodeService.getChildAssocs(
                rootNode,
                RegexQNamePattern.MATCH_ALL,
                QName.createQName("sys", "system", namespacePrefixResolver));
        NodeRef sysNode = null;
        if (results.size() == 0)
        {
            sysNode = nodeService.createNode(rootNode, ContentModel.ASSOC_CHILDREN,
                    QName.createQName("sys", "system", namespacePrefixResolver), ContentModel.TYPE_CONTAINER)
                    .getChildRef();
        }
        else
        {
            sysNode = results.get(0).getChildRef();
        }
        results = nodeService.getChildAssocs(
                sysNode,
                RegexQNamePattern.MATCH_ALL,
                QName.createQName("sys", "people", namespacePrefixResolver));
        NodeRef typesNode = null;
        if (results.size() == 0)
        {
            typesNode = nodeService.createNode(sysNode, ContentModel.ASSOC_CHILDREN,
                    QName.createQName("sys", "people", namespacePrefixResolver), ContentModel.TYPE_CONTAINER)
                    .getChildRef();
        }
        else
        {
            typesNode = results.get(0).getChildRef();
        }
        return typesNode;
    }

    public void updateUser(String userName, char[] rawPassword) throws AuthenticationException
    {
        NodeRef userRef = getUserOrNull(userName);
        if (userRef == null)
        {
            throw new AuthenticationException("User does not exist: " + userName);
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
            throw new AuthenticationException("User does not exist: " + userName);
        }
        nodeService.deleteNode(userRef);
    }

    public synchronized StoreRef getUserStoreRef()
    {
        if (userStoreRef == null)
        {
            userStoreRef = new StoreRef("user", "alfrescoUserStore");
        }
        if (!nodeService.exists(userStoreRef))
        {
            nodeService.createStore(userStoreRef.getProtocol(), userStoreRef.getIdentifier());
        }

        return userStoreRef;
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
