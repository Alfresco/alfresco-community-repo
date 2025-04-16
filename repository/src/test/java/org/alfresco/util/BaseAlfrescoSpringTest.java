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
package org.alfresco.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.springframework.transaction.annotation.Transactional;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;

/**
 * Base Alfresco test.
 * 
 * Creates a store and root node that can be used in the tests.
 * 
 * Runs all tests as the system user.
 * 
 * @author Roy Wetherall
 */
@Transactional
public abstract class BaseAlfrescoSpringTest extends BaseSpringTest
{
    /** The node service */
    protected NodeService nodeService;

    /** The content service */
    protected ContentService contentService;

    /** The authentication service */
    protected MutableAuthenticationService authenticationService;

    /** The store reference */
    protected StoreRef storeRef;

    /** The root node reference */
    protected NodeRef rootNodeRef;

    protected ActionService actionService;
    protected TransactionService transactionService;

    protected AuthenticationComponent authenticationComponent;

    @SuppressWarnings("deprecation")
    @Before
    public void before() throws Exception
    {
        // Get a reference to the node service
        this.nodeService = (NodeService) this.applicationContext.getBean("nodeService");
        this.contentService = (ContentService) this.applicationContext.getBean("contentService");
        this.authenticationService = (MutableAuthenticationService) this.applicationContext.getBean("authenticationService");
        this.actionService = (ActionService) this.applicationContext.getBean("actionService");
        this.transactionService = (TransactionService) this.applicationContext.getBean("transactionComponent");

        // Authenticate as the system user
        authenticationComponent = (AuthenticationComponent) this.applicationContext
                .getBean("authenticationComponent");
        authenticationComponent.setSystemUserAsCurrentUser();

        // Create the store and get the root node
        this.storeRef = this.nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
        this.rootNodeRef = this.nodeService.getRootNode(this.storeRef);
    }

    @After
    public void after() throws Exception
    {
        authenticationService.clearCurrentSecurityContext();
    }

    protected NodeRef createNode(NodeRef parentNode, String name, QName type)
    {
        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        String fullName = name + System.currentTimeMillis();
        props.put(ContentModel.PROP_NAME, fullName);
        QName childName = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, fullName);
        ChildAssociationRef childAssoc = nodeService.createNode(parentNode,
                ContentModel.ASSOC_CONTAINS,
                childName,
                type,
                props);
        return childAssoc.getChildRef();
    }

    protected void createUser(String userName)
    {
        createUser(userName, userName);
    }

    protected void createUser(String userName, String nameSuffix)
    {
        createUser(userName, userName, "PWD");
    }

    @SuppressWarnings("deprecation")
    protected void createUser(String userName, String nameSuffix, String password)
    {
        if (this.authenticationService.authenticationExists(userName) == false)
        {
            this.authenticationService.createAuthentication(userName, password.toCharArray());

            PropertyMap ppOne = new PropertyMap(4);
            ppOne.put(ContentModel.PROP_USERNAME, userName);
            ppOne.put(ContentModel.PROP_FIRSTNAME, "firstName" + nameSuffix);
            ppOne.put(ContentModel.PROP_LASTNAME, "lastName" + nameSuffix);
            ppOne.put(ContentModel.PROP_EMAIL, "email" + nameSuffix + "@email.com");
            ppOne.put(ContentModel.PROP_JOBTITLE, "jobTitle");

            PersonService personService = (PersonService) applicationContext.getBean("personService");
            personService.createPerson(ppOne);
        }
    }

    /**
     * We assume: Admin user is already authenticated and userName already exists.
     * 
     * @param userName
     */
    @SuppressWarnings("deprecation")
    protected void deleteUser(String userName)
    {
        PersonService personService = (PersonService) applicationContext.getBean("personService");
        personService.deletePerson(userName);
    }

}
