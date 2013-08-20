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
package org.alfresco.repo.node.integrity;
import java.io.InputStream;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.node.BaseNodeServiceTest;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.PropertyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Checks that tagging of <i>incomplete</i> nodes is done properly.
 * 
 * @author Derek Hulley
 */
@SuppressWarnings("unused")
public class IncompleteNodeTaggerTest extends TestCase
{
    private static Log logger = LogFactory.getLog(IncompleteNodeTaggerTest.class);
    
    private IncompleteNodeTagger tagger;
    private ServiceRegistry serviceRegistry;
    private NodeService nodeService;
    private NodeRef rootNodeRef;
    private PropertyMap properties;
    private UserTransaction txn;
    private AuthenticationComponent authenticationComponent;
    private MutableAuthenticationService authenticationService;
    private PermissionService permissionService;
    
    public void setUp() throws Exception
    {
        DictionaryDAO dictionaryDao = (DictionaryDAO) IntegrityTest.ctx.getBean("dictionaryDAO");
        ClassLoader cl = BaseNodeServiceTest.class.getClassLoader();
        // load the test model
        InputStream modelStream = cl.getResourceAsStream("org/alfresco/repo/node/integrity/IntegrityTest_model.xml");
        assertNotNull(modelStream);
        M2Model model = M2Model.createModel(modelStream);
        dictionaryDao.putModel(model);

        tagger = (IncompleteNodeTagger) IntegrityTest.ctx.getBean("incompleteNodeTagger");

        serviceRegistry = (ServiceRegistry) IntegrityTest.ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        nodeService = serviceRegistry.getNodeService();
        authenticationService = serviceRegistry.getAuthenticationService();
        permissionService = serviceRegistry.getPermissionService();
        this.authenticationComponent = (AuthenticationComponent)IntegrityTest.ctx.getBean("authenticationComponent");
        
        this.authenticationComponent.setSystemUserAsCurrentUser();
        
        String user = getName();
        if (!authenticationService.authenticationExists(user))
        {
            authenticationService.createAuthentication(user, user.toCharArray());
        }
        
        // begin a transaction
        TransactionService transactionService = serviceRegistry.getTransactionService();
        txn = transactionService.getUserTransaction();
        txn.begin();
        StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, getName());
        if (!nodeService.exists(storeRef))
        {
            nodeService.createStore(storeRef.getProtocol(), storeRef.getIdentifier());
            rootNodeRef = nodeService.getRootNode(storeRef);
            // Make sure our user can do everything
            permissionService.setPermission(rootNodeRef, user, PermissionService.ALL_PERMISSIONS, true);
        }
        else
        {
            rootNodeRef = nodeService.getRootNode(storeRef);
        }
        
        properties = new PropertyMap();
        properties.put(IntegrityTest.TEST_PROP_TEXT_C, "abc");
        
        // Authenticate as a test-specific user
        authenticationComponent.setCurrentUser(user);
    }
    
    public void tearDown() throws Exception
    {
        authenticationComponent.clearCurrentSecurityContext();
        txn.rollback();
    }

    /**
     * Create a node of the given type, and hanging off the root node
     */
    private NodeRef createNode(String name, QName type, PropertyMap properties)
    {
        return nodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(IntegrityTest.NAMESPACE, name),
                type,
                properties
                ).getChildRef();
    }
    
    public void testSetUp() throws Exception
    {
        assertNotNull("IncompleteNodeTagger not created", tagger);
    }
    
    private void checkTagging(final NodeRef nodeRef, final boolean mustBeTagged)
    {
        tagger.beforeCommit(false);
        RunAsWork<Void> checkWork = new RunAsWork<Void>()
        {
            public Void doWork() throws Exception
            {
                assertEquals(nodeService.hasAspect(nodeRef, ContentModel.ASPECT_INCOMPLETE), mustBeTagged);
                return null;
            }
        };
        AuthenticationUtil.runAs(checkWork, AuthenticationUtil.getSystemUserName());
    }

    public void testCreateWithoutProperties() throws Exception
    {
        NodeRef nodeRef = createNode("abc", IntegrityTest.TEST_TYPE_WITH_PROPERTIES, null);
        checkTagging(nodeRef, true);
    }
    
    /**
     * A test for MNT-7092
     */
    public void testChangeToTypeWithMandatoryProperties()
    {
        // Create plain content
        NodeRef file = createNode("abc", ContentModel.TYPE_CONTENT, null);
        // Change type to a one with mandatory properties and check the node to have sys:incomplete aspect
        nodeService.setType(file, IntegrityTest.TEST_TYPE_WITH_PROPERTIES);
        checkTagging(file, true);
    }

    public void testCreateWithProperties() throws Exception
    {
        NodeRef nodeRef = createNode("abc", IntegrityTest.TEST_TYPE_WITH_PROPERTIES, properties);
        checkTagging(nodeRef, false);
    }

    public void testCreateWithoutAssoc() throws Exception
    {
        NodeRef nodeRef = createNode("abc", IntegrityTest.TEST_TYPE_WITH_NON_ENFORCED_CHILD_ASSOCS, properties);
        checkTagging(nodeRef, true);
    }

    public void testCreateWithAssoc() throws Exception
    {
        NodeRef nodeRef = createNode("abc", IntegrityTest.TEST_TYPE_WITH_NON_ENFORCED_CHILD_ASSOCS, properties);
        nodeService.createNode(nodeRef, 
                IntegrityTest.TEST_ASSOC_CHILD_NON_ENFORCED,
                QName.createQName(IntegrityTest.NAMESPACE, "easyas"),
                IntegrityTest.TEST_TYPE_WITHOUT_ANYTHING,
                null
                );        
        checkTagging(nodeRef, false);
    }

    /**
     * <a href="http://issues.alfresco.com/jira/browse/ETHREEOH-3983">ETHREEOH-3983</a>
     */
    public void testIncompleteLockedNode() throws Exception
    {
        LockService lockService = serviceRegistry.getLockService();
        
        NodeRef nodeRef = createNode("abc", IntegrityTest.TEST_TYPE_WITH_PROPERTIES, null);
        checkTagging(nodeRef, true);
        // Now remove the aspect, lock the node and check again
        nodeService.removeAspect(nodeRef, ContentModel.ASPECT_INCOMPLETE);
        lockService.lock(nodeRef, LockType.READ_ONLY_LOCK);
        
        // Authenticate as someone else - someone not able to do anything
        final String user = "someuser";
        RunAsWork<Void> createUserWork = new RunAsWork<Void>()
        {
            public Void doWork() throws Exception
            {
                if (!authenticationService.authenticationExists(user))
                {
                    authenticationService.createAuthentication(user, user.toCharArray());
                }
                return null;
            }
        };
        AuthenticationUtil.runAs(createUserWork, AuthenticationUtil.getSystemUserName());
        authenticationComponent.setCurrentUser(user);
        
        // Tag
        checkTagging(nodeRef, true);
    }
}
