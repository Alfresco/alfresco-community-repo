/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.repo.audit;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.audit.model.AuditEntry;
import org.alfresco.repo.audit.model.TrueFalseUnset;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.MutableAuthenticationDao;
import org.alfresco.repo.security.permissions.PermissionServiceSPI;
import org.alfresco.repo.security.permissions.impl.ModelDAO;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.audit.AuditInfo;
import org.alfresco.service.cmr.audit.AuditService;
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

public class AuditServiceTest extends BaseSpringTest
{

    private NodeService nodeService;

    private DictionaryService dictionaryService;

    private PermissionServiceSPI permissionService;

    private NamespacePrefixResolver namespacePrefixResolver;

    private AuthenticationService authenticationService;

    private AuthenticationComponent authenticationComponent;

    private ServiceRegistry serviceRegistry;

    private ModelDAO permissionModelDAO;

    private PersonService personService;

    private AuthorityService authorityService;

    private MutableAuthenticationDao authenticationDAO;

    private NodeRef rootNodeRef;

    private NodeRef systemNodeRef;

    private AuditService auditService;

    private AuditEntry auditEntry;

    private NodeRef typesNodeRef;

    private QName children;

    private QName system;

    private QName container;

    private QName types;

    public AuditServiceTest()
    {
        super();
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

        auditService = (AuditService) applicationContext.getBean("AuditService");
        auditEntry = (AuditEntry) applicationContext.getBean("auditModel");

        StoreRef storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.nanoTime());
        rootNodeRef = nodeService.getRootNode(storeRef);

        children = ContentModel.ASSOC_CHILDREN;
        system = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "system");
        container = ContentModel.TYPE_CONTAINER;
        types = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "people");

        systemNodeRef = nodeService.createNode(rootNodeRef, children, system, container).getChildRef();
        typesNodeRef = nodeService.createNode(systemNodeRef, children, types, container).getChildRef();
        Map<QName, Serializable> props = createPersonProperties("andy");
        nodeService.createNode(typesNodeRef, children, ContentModel.TYPE_PERSON, container, props).getChildRef();
        props = createPersonProperties("lemur");
        nodeService.createNode(typesNodeRef, children, ContentModel.TYPE_PERSON, container, props).getChildRef();

        // create an authentication object e.g. the user
        if (authenticationDAO.userExists("andy"))
        {
            authenticationService.deleteAuthentication("andy");
        }
        authenticationService.createAuthentication("andy", "andy".toCharArray());

        if (authenticationDAO.userExists("lemur"))
        {
            authenticationService.deleteAuthentication("lemur");
        }
        authenticationService.createAuthentication("lemur", "lemur".toCharArray());

        if (authenticationDAO.userExists("admin"))
        {
            authenticationService.deleteAuthentication("admin");
        }
        authenticationService.createAuthentication("admin", "admin".toCharArray());

        authenticationComponent.clearCurrentSecurityContext();
    }

    public void testApplicationAudit()
    {
        AuthenticationUtil.setSystemUserAsCurrentUser();
        try
        {

            NodeRef nodeRef = new NodeRef(new StoreRef("test", "audit"), "id");
            int start = auditService.getAuditTrail(nodeRef).size();
            int increment = auditEntry.getEnabled() == TrueFalseUnset.TRUE ? 1 : 0;
            auditService.audit("AuditedApp", "First");
            assertEquals(start, auditService.getAuditTrail(nodeRef).size());
            auditService.audit("AuditedApp", "Second", nodeRef);
            assertEquals(start + (1 * increment), auditService.getAuditTrail(nodeRef).size());
            auditService.audit("AuditedApp", "Third", new Object[] { "one", "two", "three" });
            assertEquals(start + (1 * increment), auditService.getAuditTrail(nodeRef).size());
            auditService.audit("AuditedApp", "Fourth", nodeRef, new Object[] { "one", "two", "three" });
            assertEquals(start + (2 * increment), auditService.getAuditTrail(nodeRef).size());
            auditService.audit("UnAuditedApp", "First");
            assertEquals(start + (2 * increment), auditService.getAuditTrail(nodeRef).size());
            auditService.audit("UnAuditedApp", "Second", nodeRef);
            assertEquals(start + (3 * increment), auditService.getAuditTrail(nodeRef).size());
            auditService.audit("UnAuditedApp", "Third", new Object[] { "one", "two", "three" });
            assertEquals(start + (3 * increment), auditService.getAuditTrail(nodeRef).size());
            auditService.audit("UnAuditedApp", "Fourth", nodeRef, new Object[] { "one", "two", "three" });
            assertEquals(start + (4 * increment), auditService.getAuditTrail(nodeRef).size());
        }
        finally
        {
            AuthenticationUtil.clearCurrentSecurityContext();
        }
    }

    public void testNodeServiceAudit()
    {
        AuthenticationUtil.setSystemUserAsCurrentUser();
        try
        {
            int start = auditService.getAuditTrail(typesNodeRef).size();
            int increment = auditEntry.getEnabled() == TrueFalseUnset.TRUE ? 1 : 0;

            // Create

            Map<QName, Serializable> props = createPersonProperties("woof");
            NodeRef created = serviceRegistry.getNodeService().createNode(typesNodeRef, children,
                    ContentModel.TYPE_PERSON, container, props).getChildRef();
            assertEquals(start + (1 * increment), auditService.getAuditTrail(typesNodeRef).size());
            List<AuditInfo> list = auditService.getAuditTrail(typesNodeRef);
            assertEquals((1 * increment), auditService.getAuditTrail(created).size());

            // Update

            serviceRegistry.getNodeService().setProperty(created, ContentModel.PROP_FIRSTNAME, "New First Name");
            assertEquals((2 * increment), auditService.getAuditTrail(created).size());

            // Update

            serviceRegistry.getNodeService().setProperty(created, ContentModel.PROP_FIRSTNAME, "Next First Name");
            assertEquals((3 * increment), auditService.getAuditTrail(created).size());

            // Delete

            serviceRegistry.getNodeService().deleteNode(created);
            assertEquals((4 * increment), auditService.getAuditTrail(created).size());

            list = auditService.getAuditTrail(created);
            assertNotNull(list);
        }
        finally
        {
            AuthenticationUtil.clearCurrentSecurityContext();
        }
    }

    public void xtestCreateStore()
    {

        AuthenticationUtil.setSystemUserAsCurrentUser();
        try
        {
            serviceRegistry.getNodeService()
                    .createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_Audit_" + System.nanoTime());
            // Should have a query to support this - check direct in the DB
        }
        finally
        {
            AuthenticationUtil.clearCurrentSecurityContext();
        }
        setComplete();
    }

    public void xtestAuthenticartionDoesNotReportPasswords()
    {
        // Should have a query to support this - check direct in the DB
        AuthenticationUtil.setSystemUserAsCurrentUser();
        try
        {
            serviceRegistry.getAuthenticationService().createAuthentication("cabbage", "cabbage".toCharArray());
            serviceRegistry.getAuthenticationService().updateAuthentication("cabbage", "cabbage".toCharArray(),
                    "red".toCharArray());
        }
        finally
        {
            AuthenticationUtil.clearCurrentSecurityContext();
        }

        try
        {
            serviceRegistry.getAuthenticationService().authenticate("cabbage", "red".toCharArray());
        }
        finally
        {
            serviceRegistry.getAuthenticationService().clearCurrentSecurityContext();
        }
        setComplete();
    }

    public void xtestAuthenticartionFailure()
    {
        // Should have a query to support this - check direct in the DB
        AuthenticationUtil.setSystemUserAsCurrentUser();

        serviceRegistry.getAuthenticationService().createAuthentication("woof", "cabbage".toCharArray());
        serviceRegistry.getAuthenticationService().authenticate("woof", "red".toCharArray());

    }

    public void testThereIsAnAuditService()
    {
        assertNotNull(serviceRegistry.getAuditService());
    }

    private Map<QName, Serializable> createPersonProperties(String userName)
    {
        HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(ContentModel.PROP_USERNAME, userName);
        return properties;
    }

}
