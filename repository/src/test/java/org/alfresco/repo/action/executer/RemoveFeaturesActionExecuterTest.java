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
package org.alfresco.repo.action.executer;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionImpl;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.test_category.BaseSpringTestsCategory;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.transaction.annotation.Transactional;

/**
 * Remove features action execution test
 * 
 * @author Roy Wetherall
 */
@Category(BaseSpringTestsCategory.class)
@Transactional
public class RemoveFeaturesActionExecuterTest extends BaseSpringTest
{
    /**
     * The node service
     */
    private NodeService nodeService;
    
    /**
     * The store reference
     */
    private StoreRef testStoreRef;
    
    /**
     * The root node reference
     */
    private NodeRef rootNodeRef;
    
    /**
     * The test node reference
     */
    private NodeRef nodeRef;
    
    /**
     * The add features action executer
     */
    private RemoveFeaturesActionExecuter executer;
    
    /**
     * Id used to identify the test action created
     */
    private final static String ID = GUID.generate();
    
    /**
     * Called at the begining of all tests
     */
    @Before
    public void before() throws Exception
    {
        this.nodeService = (NodeService)this.applicationContext.getBean("nodeService");
        
        AuthenticationComponent authenticationComponent = (AuthenticationComponent)applicationContext.getBean("authenticationComponent");
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());
        
        // Create the store and get the root node
        this.testStoreRef = this.nodeService.createStore(
                StoreRef.PROTOCOL_WORKSPACE, "Test_"
                        + System.currentTimeMillis());
        this.rootNodeRef = this.nodeService.getRootNode(this.testStoreRef);

        // Create the node used for tests
        this.nodeRef = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName("{test}testnode"),
                ContentModel.TYPE_CONTENT).getChildRef();
        this.nodeService.addAspect(nodeRef, ContentModel.ASPECT_CLASSIFIABLE, null);
        
        // Get the executer instance 
        this.executer = (RemoveFeaturesActionExecuter)this.applicationContext.getBean(RemoveFeaturesActionExecuter.NAME);
    }
    
    /**
     * Test execution
     */
    @Test
    public void testExecution()
    {
        // Check that the node has the classifiable aspect
        assertTrue(this.nodeService.hasAspect(this.nodeRef, ContentModel.ASPECT_CLASSIFIABLE));
        
        // Execute the action
        ActionImpl action = new ActionImpl(null, ID, RemoveFeaturesActionExecuter.NAME, null);
        action.setParameterValue(RemoveFeaturesActionExecuter.PARAM_ASPECT_NAME, ContentModel.ASPECT_CLASSIFIABLE);
        this.executer.execute(action, this.nodeRef);
        
        // Check that the node now no longer has the classifiable aspect
        assertFalse(this.nodeService.hasAspect(this.nodeRef, ContentModel.ASPECT_CLASSIFIABLE));
        
        // Now try and remove an aspect that is not present 
        ActionImpl action2 = new ActionImpl(null, ID, RemoveFeaturesActionExecuter.NAME, null);
        action2.setParameterValue(RemoveFeaturesActionExecuter.PARAM_ASPECT_NAME, ContentModel.ASPECT_VERSIONABLE);
        this.executer.execute(action2, this.nodeRef);
    }

    /**
     * Test removing aspect properties
     */
    @Test
    public void testRemovingAspectPropertiesAfterExecution()
    {
        QName QNAME_PUBLISHER = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "publisher");
        QName QNAME_SUBJECT = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "subject");

        // Execute the action
        PropertyMap dublinCoreProperties = new PropertyMap(2);
        dublinCoreProperties.put(QNAME_PUBLISHER, "publisher");
        dublinCoreProperties.put(QNAME_SUBJECT, "subject");
        nodeService.addAspect(nodeRef, ContentModel.ASPECT_DUBLINCORE, dublinCoreProperties);

        // Check that the node has aspect properties
        assertTrue(this.nodeService.hasAspect(this.nodeRef, ContentModel.ASPECT_DUBLINCORE));
        assertTrue(this.nodeService.getProperties(this.nodeRef).containsKey(QNAME_PUBLISHER));
        assertTrue(this.nodeService.getProperties(this.nodeRef).containsKey(QNAME_SUBJECT));

        // Remove the aspect
        ActionImpl action = new ActionImpl(null, ID, RemoveFeaturesActionExecuter.NAME, null);
        action.setParameterValue(RemoveFeaturesActionExecuter.PARAM_ASPECT_NAME, ContentModel.ASPECT_DUBLINCORE);
        this.executer.execute(action, this.nodeRef);

        // Check that the node now no longer has aspect properties
        assertFalse(this.nodeService.getProperties(this.nodeRef).containsKey(QNAME_PUBLISHER));
        assertFalse(this.nodeService.getProperties(this.nodeRef).containsKey(QNAME_SUBJECT));
    }

    /**
     * Test removing not added child aspect
     */
    @Test
    public void testRemovingNotAddedChildAspect()
    {
        QName QNAME_TITLE = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "title");

        // Execute the action
        PropertyMap titledProperties = new PropertyMap(1);
        titledProperties.put(QNAME_TITLE, "title");
        nodeService.addAspect(nodeRef, ContentModel.ASPECT_TITLED, titledProperties);

        // Remove the child aspect which has not been added to the node
        ActionImpl action = new ActionImpl(null, ID, RemoveFeaturesActionExecuter.NAME, null);
        action.setParameterValue(RemoveFeaturesActionExecuter.PARAM_ASPECT_NAME, ContentModel.ASPECT_DUBLINCORE);
        this.executer.execute(action, this.nodeRef);

        // Now check that the node has parent aspect properties
        assertTrue(this.nodeService.getProperties(this.nodeRef).containsKey(QNAME_TITLE));
    }
}
