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

import java.io.Serializable;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionImpl;
import org.alfresco.repo.cache.SimpleCache;
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
     * Set the simpleCache service
     *
     * @param simpleCache  the cache service
     */
    private SimpleCache<Serializable,Object> simpleCache;

    /**
     * Called at the begining of all tests.
     */
    @Before
    public void before() throws Exception
    {
        NodeService nodeService = (NodeService)this.applicationContext.getBean("nodeService");
        StoreRef testStoreRef;
        NodeRef rootNodeRef;

        AuthenticationComponent authenticationComponent = (AuthenticationComponent)applicationContext.getBean("authenticationComponent");
        authenticationComponent.setCurrentUser(authenticationComponent.getSystemUserName());

        // Create the store and get the root node
        testStoreRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_"
                        + System.currentTimeMillis());
        rootNodeRef = nodeService.getRootNode(testStoreRef);

        // Create the node used for tests
        this.nodeRef = nodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName("{test}testnode"),
                ContentModel.TYPE_CONTENT).getChildRef();

        // Get the executer instance.
        this.executer = (NodeSizeActionExecuter)this.applicationContext.getBean(NodeSizeActionExecuter.NAME);

        simpleCache = (SimpleCache<Serializable, Object>) this.applicationContext.getBean("folderSizeSharedCache");
    }

    /**
     * Test execution.
     */
    @Test
    public void testExecution()
    {
        int maxItems = 100;
        ActionImpl action = new ActionImpl(null, ID, NodeSizeActionExecuter.NAME, null);
        action.setParameterValue(NodeSizeActionExecuter.DEFAULT_SIZE, maxItems);
        this.executer.executeImpl(action, this.nodeRef);
        Object resultAction = simpleCache.get(this.nodeRef.getId());
        Map<String, Object> mapResult = (Map<String, Object>)resultAction;
        assertTrue(mapResult != null);
    }
}
