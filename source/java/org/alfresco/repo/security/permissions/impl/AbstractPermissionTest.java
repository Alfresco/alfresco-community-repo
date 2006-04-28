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
package org.alfresco.repo.security.permissions.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.repo.security.permissions.PermissionReference;
import org.alfresco.repo.security.permissions.PermissionServiceSPI;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseSpringTest;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

public class AbstractPermissionTest extends BaseSpringTest
{

    protected static final String ROLE_AUTHENTICATED = "ROLE_AUTHENTICATED";

    protected NodeService nodeService;

    protected DictionaryService dictionaryService;

    protected PermissionServiceSPI permissionService;

    protected AuthenticationService authenticationService;
    
    private MutableAuthenticationDao authenticationDAO;

    protected LocalSessionFactoryBean sessionFactory;

    protected NodeRef rootNodeRef;

    protected NamespacePrefixResolver namespacePrefixResolver;

    protected ServiceRegistry serviceRegistry;

    protected NodeRef systemNodeRef;

    protected AuthenticationComponent authenticationComponent;

    protected ModelDAO permissionModelDAO;
    
    protected PersonService personService;
    
    protected AuthorityService authorityService;

    public AbstractPermissionTest()
    {
        super();
        // TODO Auto-generated constructor stub
    }

    protected void onSetUpInTransaction() throws Exception
    {
        nodeService = (NodeService) applicationContext.getBean("nodeService");
        dictionaryService = (DictionaryService) applicationContext.getBean(ServiceRegistry.DICTIONARY_SERVICE
                .getLocalName());
        permissionService = (PermissionServiceSPI) applicationContext.getBean("permissionService");
        namespacePrefixResolver = (NamespacePrefixResolver) applicationContext
                .getBean(ServiceRegistry.NAMESPACE_SERVICE.getLocalName());
        authenticationService = (AuthenticationService) applicationContext.getBean("authenticationService");
        authenticationComponent = (AuthenticationComponent) applicationContext.getBean("authenticationComponent");
        serviceRegistry = (ServiceRegistry) applicationContext.getBean(ServiceRegistry.SERVICE_REGISTRY);
        permissionModelDAO = (ModelDAO) applicationContext.getBean("permissionsModelDAO");
        personService = (PersonService) applicationContext.getBean("personService");
        authorityService = (AuthorityService) applicationContext.getBean("authorityService");
        
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());
        authenticationDAO = (MutableAuthenticationDao) applicationContext.getBean("alfDaoImpl");
        
        
        StoreRef storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.nanoTime());
        rootNodeRef = nodeService.getRootNode(storeRef);

        QName children = ContentModel.ASSOC_CHILDREN;
        QName system = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "system");
        QName container = ContentModel.TYPE_CONTAINER;
        QName types = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "people");

        systemNodeRef = nodeService.createNode(rootNodeRef, children, system, container).getChildRef();
        NodeRef typesNodeRef = nodeService.createNode(systemNodeRef, children, types, container).getChildRef();
        Map<QName, Serializable> props = createPersonProperties("andy");
        nodeService.createNode(typesNodeRef, children, ContentModel.TYPE_PERSON, container, props).getChildRef();
        props = createPersonProperties("lemur");
        nodeService.createNode(typesNodeRef, children, ContentModel.TYPE_PERSON, container, props).getChildRef();

        // create an authentication object e.g. the user
        if(authenticationDAO.userExists("andy"))
        {
            authenticationService.deleteAuthentication("andy");
        }
        authenticationService.createAuthentication("andy", "andy".toCharArray());

        if(authenticationDAO.userExists("lemur"))
        {
            authenticationService.deleteAuthentication("lemur");
        }
        authenticationService.createAuthentication("lemur", "lemur".toCharArray());
        
        if(authenticationDAO.userExists("admin"))
        {
            authenticationService.deleteAuthentication("admin");
        }
        authenticationService.createAuthentication("admin", "admin".toCharArray());
        
        authenticationComponent.clearCurrentSecurityContext();
    }

    protected void onTearDownInTransaction() throws Exception
    {
        try
        {
            flushAndClear();
        }
        catch (Throwable e)
        {
            // don't absorb the exception
            e.printStackTrace();
        }
        super.onTearDownInTransaction();
    }

    protected void runAs(String userName)
    {
        authenticationService.authenticate(userName, userName.toCharArray());
        assertNotNull(authenticationService.getCurrentUserName());
        // for(GrantedAuthority authority : woof.getAuthorities())
        // {
        // System.out.println("Auth = "+authority.getAuthority());
        // }

    }

    private Map<QName, Serializable> createPersonProperties(String userName)
    {
        HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(ContentModel.PROP_USERNAME, userName);
        return properties;
    }

    protected PermissionReference getPermission(String permission)
    {
        return permissionModelDAO.getPermissionReference(null, permission);
    }

}
