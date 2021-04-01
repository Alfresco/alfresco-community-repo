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
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.test_category.BaseSpringTestsCategory;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.GUID;
import org.alfresco.util.testing.category.NeverRunsTests;
import org.junit.Before;
import org.junit.experimental.categories.Category;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests checkout using action executer
 */
@Category({BaseSpringTestsCategory.class, NeverRunsTests.class})
@Transactional
public class CheckOutActionExecuterTest extends BaseSpringTest
{
    private NodeService nodeService;
    private CheckOutCheckInService checkOutCheckInService;

    /**
     * The add features action executer
     */
    private CheckOutActionExecuter executer;

    private StoreRef testStoreRef;
    private NodeRef rootNodeRef;
    private NodeRef nodeRefContent;
    private NodeRef nodeRefFolder;

    /**
     * Id used to identify the test action created
     */
    private final static String ID = GUID.generate();

    @Before
    public void before() throws Exception
    {
        this.nodeService = (NodeService) this.applicationContext.getBean("nodeService");

        this.checkOutCheckInService = (CheckOutCheckInService) this.applicationContext.getBean("checkOutCheckInService");

        AuthenticationComponent authenticationComponent = (AuthenticationComponent) applicationContext.getBean("authenticationComponent");
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());

        // Create the store and get the root node
        this.testStoreRef = this.nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
        this.rootNodeRef = this.nodeService.getRootNode(this.testStoreRef);

        // Create 'content' the node used for tests
        this.nodeRefContent = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName("{test}contenttestnode"),
                ContentModel.TYPE_CONTENT).getChildRef();

        // Create 'folder' the node used for tests
        this.nodeRefFolder = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName("{test}foldertestnode"),
                ContentModel.TYPE_FOLDER).getChildRef();

        // Get the executer instance
        this.executer = (CheckOutActionExecuter) this.applicationContext.getBean(CheckOutActionExecuter.NAME);
    }

    /**
     * Test execution
     */
    public void testExecution()
    {
        // Execute the action
        ActionImpl action = new ActionImpl(null, ID, CheckOutActionExecuter.NAME, null);

        // Execute check out action for 'content' node
        this.executer.execute(action, this.nodeRefContent);
        // Execute check out action for 'folder' node
        this.executer.execute(action, this.nodeRefFolder);

        assertNotNull(this.checkOutCheckInService.getWorkingCopy(this.nodeRefContent));
        assertNull(this.checkOutCheckInService.getWorkingCopy(this.nodeRefFolder));
    }
}
