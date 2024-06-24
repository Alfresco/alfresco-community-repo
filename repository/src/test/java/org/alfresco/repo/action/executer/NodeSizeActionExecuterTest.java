/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
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
import org.alfresco.model.FolderSizeModel;
import org.alfresco.repo.action.ActionImpl;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.GUID;
import org.junit.Before;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class NodeSizeActionExecuterTest extends BaseSpringTest
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
     * The folder Size executer
     */
    private NodeSizeActionExecuter executer;

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
        this.testStoreRef = this.nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_"
                        + System.currentTimeMillis());
        this.rootNodeRef = this.nodeService.getRootNode(this.testStoreRef);

        // Create the node used for tests
        this.nodeRef = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName("{test}testnode"),
                ContentModel.TYPE_CONTENT).getChildRef();

        // Get the executer instance
        this.executer = (NodeSizeActionExecuter)this.applicationContext.getBean(NodeSizeActionExecuter.NAME);
    }

    /**
     * Test execution
     */
    @Test
    public void testExecution()
    {
        assertEquals(1,1);
        int maxItems = 100;
        // Execute the action
        ActionImpl action = new ActionImpl(null, ID, NodeSizeActionExecuter.NAME, null);
        action.setParameterValue(NodeSizeActionExecuter.PAGE_SIZE, maxItems);
        this.executer.executeImpl(action, this.nodeRef);
        String compareString = this.nodeService.getProperty(this.nodeRef, FolderSizeModel.PROP_STATUS).toString();
        assertTrue(compareString!=null?true:false);
    }
}
